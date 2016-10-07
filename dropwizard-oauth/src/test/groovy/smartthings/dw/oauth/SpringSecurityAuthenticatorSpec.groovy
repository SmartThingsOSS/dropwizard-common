package smartthings.dw.oauth

import com.google.common.base.Optional
import io.dropwizard.auth.AuthenticationException
import io.dropwizard.util.Duration
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import smartthings.dw.logging.LoggingContext
import spock.lang.Specification

class SpringSecurityAuthenticatorSpec extends Specification {

	SpringSecurityAuthenticator springSecurityAuthenticator
	AuthConfiguration config
	AsyncHttpClient client

	String token = 'abcd'
	BoundRequestBuilder requestBuilder = Mock()
	Response response = Mock()
	ListenableFuture<Response> future = Mock()

	def setup() {
		config = new AuthConfiguration()
		client = Mock()
		config.host = 'http://localhost:4242'
		config.user = 'me'
		config.password = 'BahBahBlackSheep'
		config.requestTimeout = Duration.seconds(1)
		springSecurityAuthenticator = new SpringSecurityAuthenticator(config, client)
	}

	def '400 oauth response returns empty option'() {
		when:
		Optional<OAuthToken> actual = springSecurityAuthenticator.authenticate(token)

		then:
		1 * client.prepareGet("${config.host}/oauth/check_token?token=${token}") >> requestBuilder
		1 * requestBuilder.setRequestTimeout(1000) >> requestBuilder
		1 * requestBuilder.setRealm({ it.principal == config.user && it.password == config.password }) >> requestBuilder
		1 * requestBuilder.addHeader('Accept', 'application/json') >> requestBuilder
		1 * requestBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.loggingId) >> requestBuilder
		1 * requestBuilder.execute() >> future
		1 * future.get() >> response
		1 * response.getStatusCode() >> 401
		0 * _

		!actual.isPresent()
	}

	def 'non-200 oauth response throws Auth exception'() {
		when:
		springSecurityAuthenticator.authenticate(token)

		then:
		1 * client.prepareGet("${config.host}/oauth/check_token?token=${token}") >> requestBuilder
		1 * requestBuilder.setRequestTimeout(1000) >> requestBuilder
		1 * requestBuilder.setRealm({ it.principal == config.user && it.password == config.password }) >> requestBuilder
		1 * requestBuilder.addHeader('Accept', 'application/json') >> requestBuilder
		1 * requestBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.loggingId) >> requestBuilder
		1 * requestBuilder.execute() >> future
		1 * future.get() >> response
		2 * response.getStatusCode() >> 500
		0 * _

		thrown(AuthenticationException)
	}

	def 'exceptional oauth response throws Auth exception'() {
		when:
		springSecurityAuthenticator.authenticate(token)

		then:
		1 * client.prepareGet("${config.host}/oauth/check_token?token=${token}") >> requestBuilder
		1 * requestBuilder.setRequestTimeout(1000) >> requestBuilder
		1 * requestBuilder.setRealm({ it.principal == config.user && it.password == config.password }) >> requestBuilder
		1 * requestBuilder.addHeader('Accept', 'application/json') >> requestBuilder
		1 * requestBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.loggingId) >> requestBuilder
		1 * requestBuilder.execute() >> future
		1 * future.get() >> { throw new Exception("bah") }
		0 * _

		thrown(AuthenticationException)
	}

	def 'response with no user included is handled as excepted'() {
		given:
		String json = """
		{
		  "scope": ["service"],
		  "exp": 3018633301,
		  "authorities": ["ROLE_TRUSTED_CLIENT", "ROLE_USER"],
		  "client_id": "abcd"
		}
		"""

		when:
		Optional<OAuthToken> actual = springSecurityAuthenticator.authenticate(token)

		then:
		1 * client.prepareGet("${config.host}/oauth/check_token?token=${token}") >> requestBuilder
		1 * requestBuilder.setRequestTimeout(1000) >> requestBuilder
		1 * requestBuilder.setRealm({ it.principal == config.user && it.password == config.password }) >> requestBuilder
		1 * requestBuilder.addHeader('Accept', 'application/json') >> requestBuilder
		1 * requestBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.loggingId) >> requestBuilder
		1 * requestBuilder.execute() >> future
		1 * future.get() >> response
		1 * response.getStatusCode() >> 200
		1 * response.getResponseBodyAsStream() >> new ByteArrayInputStream(json.bytes)
		0 * _

		actual.isPresent()
		!actual.get().user.isPresent()
		actual.get().scopes == ['service'].toSet()
		actual.get().clientId == 'abcd'
	}

	def 'response with user is handled as expected'() {
		given:
		String json = """
		{
		  "user_name": "charliek",
		  "scope": ["app"],
		  "fullName": "charliek",
		  "exp": 3025890541,
		  "uuid": "83540011-a053-499c-9f64-4de40df39013",
		  "authorities": ["ROLE_SUPPORT", "ROLE_SUPERUSER", "ROLE_APPROVER"],
		  "email": "charlie.knudsen@smartthings.com",
		  "client_id": "abcd"
		}
		"""

		when:
		Optional<OAuthToken> actual = springSecurityAuthenticator.authenticate(token)

		then:
		1 * client.prepareGet("${config.host}/oauth/check_token?token=${token}") >> requestBuilder
		1 * requestBuilder.setRequestTimeout(1000) >> requestBuilder
		1 * requestBuilder.setRealm({ it.principal == config.user && it.password == config.password }) >> requestBuilder
		1 * requestBuilder.addHeader('Accept', 'application/json') >> requestBuilder
		1 * requestBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.loggingId) >> requestBuilder
		1 * requestBuilder.execute() >> future
		1 * future.get() >> response
		1 * response.getStatusCode() >> 200
		1 * response.getResponseBodyAsStream() >> new ByteArrayInputStream(json.bytes)
		0 * _

		actual.isPresent()
		actual.get().user.isPresent()
		actual.get().user.get().userName == 'charliek'
		actual.get().user.get().authorities == ["ROLE_SUPPORT", "ROLE_SUPERUSER", "ROLE_APPROVER"].toSet()
		actual.get().user.get().uuid == '83540011-a053-499c-9f64-4de40df39013'
		actual.get().user.get().email == 'charlie.knudsen@smartthings.com'
		actual.get().user.get().fullName == 'charliek'
		actual.get().scopes == ['app'].toSet()
		actual.get().clientId == 'abcd'
	}

	def 'response with null roles is populated with empty lists'() {
		given:
		String json = """
		{
		  "user_name": "charliek",
		  "fullName": "charliek",
		  "exp": 3025890541,
		  "uuid": "83540011-a053-499c-9f64-4de40df39013",
		  "email": "charlie.knudsen@smartthings.com",
		  "client_id": "abcd"
		}
		"""

		when:
		Optional<OAuthToken> actual = springSecurityAuthenticator.authenticate(token)

		then:
		1 * client.prepareGet("${config.host}/oauth/check_token?token=${token}") >> requestBuilder
		1 * requestBuilder.setRequestTimeout(1000) >> requestBuilder
		1 * requestBuilder.setRealm({ it.principal == config.user && it.password == config.password }) >> requestBuilder
		1 * requestBuilder.addHeader('Accept', 'application/json') >> requestBuilder
		1 * requestBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.loggingId) >> requestBuilder
		1 * requestBuilder.execute() >> future
		1 * future.get() >> response
		1 * response.getStatusCode() >> 200
		1 * response.getResponseBodyAsStream() >> new ByteArrayInputStream(json.bytes)
		0 * _

		actual.isPresent()
		actual.get().user.isPresent()
		actual.get().user.get().userName == 'charliek'
		actual.get().user.get().authorities == [].toSet()
		actual.get().user.get().uuid == '83540011-a053-499c-9f64-4de40df39013'
		actual.get().user.get().email == 'charlie.knudsen@smartthings.com'
		actual.get().user.get().fullName == 'charliek'
		actual.get().scopes == [].toSet()
		actual.get().clientId == 'abcd'
	}

}
