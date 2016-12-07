package smartthings.dw.oauth

import spock.lang.Specification
import spock.lang.Unroll

class TokenAuthorizerSpec extends Specification {

	TokenAuthorizer userAuthorizer

	def setup() {
		userAuthorizer = new TokenAuthorizer()
	}

	def 'missing user will always be false'() {
		given:
		OAuthToken token = new OAuthToken(Optional.empty(), [], "", "token", [:])

		when:
		boolean allowed = userAuthorizer.authorize(token, null)

		then:
		! allowed
	}

	@Unroll
	def '#requestedRole role on user with #userRoles is #allowed'() {
		given:
		User user = new User(UUID.randomUUID().toString(), "Santa", "", "", userRoles)
		OAuthToken token = new OAuthToken(Optional.of(user), [], "", "token", [:])

		when:
		boolean actual = userAuthorizer.authorize(token, requestedRole)

		then:
		actual == allowed

		where:
		userRoles              | requestedRole   || allowed
		['admin']              | 'superuser'     || false
		[]                     | 'superuser'     || false
		['superuser']          | 'superuser'     || true
		['superuser', 'admin'] | 'superuser'     || true
		['superuser']          | null            || false
	}
}
