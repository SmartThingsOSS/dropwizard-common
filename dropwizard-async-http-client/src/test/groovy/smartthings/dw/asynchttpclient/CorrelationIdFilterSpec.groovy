package smartthings.dw.asynchttpclient

import io.netty.handler.codec.http.HttpHeaders
import org.asynchttpclient.AsyncHandler
import org.asynchttpclient.HttpResponseBodyPart
import org.asynchttpclient.HttpResponseStatus
import org.asynchttpclient.Request
import org.asynchttpclient.filter.FilterContext
import org.slf4j.MDC
import smartthings.dw.logging.LoggingContext
import spock.lang.Specification

class CorrelationIdFilterSpec extends Specification {

	CorrelationIdFilter correlationIdFilter = new CorrelationIdFilter()
	Request request = Mock()

	HttpHeaders requestHeaders = Mock()
	static final String FOO = 'foo'

	def setup() {
		MDC.put(FOO, 'bar')
		MDC.put('loggingId', 'log-id')
		0 * _
	}

	def cleanup() {
		MDC.clear()
	}

	def 'wrapped async handler adds and removes MDC'() {
		given:
		def callResults = [:]

		def ctx = new FilterContext.FilterContextBuilder().asyncHandler(new AsyncHandler() {
			@Override
			void onThrowable(Throwable t) {
				callResults.throwable = MDC.get(FOO)
			}

			@Override
			AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
				callResults.onBodyPart = MDC.get(FOO)
				return AsyncHandler.State.CONTINUE
			}

			@Override
			AsyncHandler.State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
				callResults.onStatus = MDC.get(FOO)
				return AsyncHandler.State.ABORT
			}

			@Override
			AsyncHandler.State onHeadersReceived(HttpHeaders headers) throws Exception {
				callResults.onHeaders = MDC.get(FOO)
				return AsyncHandler.State.CONTINUE
			}

			@Override
			Object onCompleted() throws Exception {
				callResults.onComplete = MDC.get(FOO)
				return 'result'
			}
		}).request(request).build()

		when:
		def updated = correlationIdFilter.filter(ctx)

		then:
		!updated.is(ctx)
		_ * request.headers >> requestHeaders
		1 * requestHeaders.contains(LoggingContext.CORRELATION_ID_HEADER) >> false
		1 * requestHeaders.add(LoggingContext.CORRELATION_ID_HEADER, 'log-id')

		when:
		String fooBefore
		String fooAfter
		Thread.start {
			MDC.clear()
			fooBefore = MDC.get(FOO)
			updated.asyncHandler.onThrowable(new Exception())
			fooAfter = MDC.get(FOO)
		}.join()

		then:
		callResults.throwable == 'bar'
		fooBefore == null
		fooAfter == null

		when:
		def result
		Thread.start {
			MDC.clear()
			fooBefore = MDC.get(FOO)
			result = updated.asyncHandler.onBodyPartReceived(null)
			fooAfter = MDC.get(FOO)
		}.join()

		then:
		callResults.onBodyPart == 'bar'
		fooBefore == null
		fooAfter == null
		result == AsyncHandler.State.CONTINUE

		when:
		Thread.start {
			MDC.clear()
			fooBefore = MDC.get(FOO)
			result = updated.asyncHandler.onStatusReceived(null)
			fooAfter = MDC.get(FOO)
		}.join()

		then:
		callResults.onStatus == 'bar'
		fooBefore == null
		fooAfter == null
		result == AsyncHandler.State.ABORT

		when:
		Thread.start {
			MDC.clear()
			fooBefore = MDC.get(FOO)
			result = updated.asyncHandler.onHeadersReceived(null)
			fooAfter = MDC.get(FOO)
		}.join()

		then:
		callResults.onHeaders == 'bar'
		fooBefore == null
		fooAfter == null
		result == AsyncHandler.State.CONTINUE

		when:
		Thread.start {
			MDC.clear()
			fooBefore = MDC.get(FOO)
			result = updated.asyncHandler.onCompleted()
			fooAfter = MDC.get(FOO)
		}.join()

		then:
		callResults.onComplete == 'bar'
		fooBefore == null
		fooAfter == null
		result == 'result'
	}
}
