package smartthings.dw.oauth.scope

import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.Authenticator
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.testing.junit.ResourceTestRule
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory
import org.junit.Rule
import smartthings.dw.oauth.OAuthToken
import smartthings.dw.oauth.User
import smartthings.dw.oauth.TokenAuthorizer
import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.lang.reflect.Method

class ScopesAllowedDynamicFeatureSpec extends Specification {

	Authenticator<String, OAuthToken> authenticator = Mock()

	@Rule
	public ResourceTestRule rule = ResourceTestRule
		.builder()
		.setTestContainerFactory(new GrizzlyWebTestContainerFactory())
		.addProvider(new ExtendedAuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<OAuthToken>()
		.setAuthenticator(authenticator)
		.setAuthorizer(new TokenAuthorizer())
		.setPrefix("Bearer")
		.buildAuthFilter()))
		.addProvider(ScopesAllowedDynamicFeature.class)
		.addProvider(RolesAllowedDynamicFeature.class)
		.addProvider(new AuthValueFactoryProvider.Binder<OAuthToken>(OAuthToken.class))
		.addResource(new TestScopeResource())
		.build();

	@Unroll
	def 'scopes only tests - scopes #scopes - path #path - status #status'() {
		given:
		OAuthToken token = new OAuthToken(Optional.empty(), scopes, "", "TOKEN", [:])

		when:
		Response response = rule.getJerseyTest().target(path)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.header("Authorization", "Bearer TOKEN")
			.get();

		then:
		1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
		0 * _

		response.status == status

		where:
		scopes        | path               || status
		['admin']     | "/classProtected"  || 200
		['superuser'] | "/classProtected"  || 403
		['superuser'] | "/methodProtected" || 200
		['admin']     | "/methodProtected" || 403
	}

	@Unroll
	def 'role tests - scopes #scopes - path #path - status #status'() {
		given:
		User user = new User(null, "charliek", "", "", roles)
		OAuthToken token = new OAuthToken(Optional.of(user), scopes, "", "TOKEN", [:])

		when:
		Response response = rule.getJerseyTest().target(path)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.header("Authorization", "Bearer TOKEN")
				.get();

		then:
		1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
		0 * _

		response.status == status

		where:
		roles         | scopes        | path                             || status
		['DEVELOPER'] | ['admin']     | "/methodProtectedWithRole"       || 200
		['DEVELOPER'] | ['superuser'] | "/methodProtectedWithRole"       || 403
		['DEVELOPER'] | ['superuser'] | "/methodProtectedWithRoleScope"  || 200
		['DEVELOPER'] | ['admin']     | "/methodProtectedWithRoleScope"  || 403
	}

    @Unroll
    def 'GET #path with #roles, #scopes, #queryParams, #headerParams returns #status'() {
        given:
        User user = new User(null, "charliek", "", "", roles)
        OAuthToken token = new OAuthToken(Optional.of(user), scopes, "", "TOKEN", [:])

        when:
        Response response = rule.getJerseyTest().target(path).with { target ->
            queryParams.each { kv -> target = target.queryParam(kv.key, kv.value) }
            target
        }.request(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Bearer TOKEN").with { builder ->
            headerParams.each{ kv -> builder = builder.header(kv.key, kv.value) }
            builder
        }.get();

        then:
        1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
        0 * _

        and:
        response.status == status

        where:
        roles         | scopes                                           | path                        | queryParams              | headerParams             || status
        ['DEVELOPER'] | ['r:hubs:123456']                                | "/fineGrained/hubId/123456" | [:]                      | [:]                      || 200
        ['DEVELOPER'] | ['w:hubs:123456']                                | "/fineGrained/hubId/123456" | [:]                      | [:]                      || 200
        ['DEVELOPER'] | ['superuser']                                    | "/fineGrained/hubId/999999" | [:]                      | [:]                      || 200
        ['DEVELOPER'] | ['r:hubs:654321']                                | "/fineGrained/hubId/123456" | [:]                      | [:]                      || 403
        ['USER']      | ['r:hubs:123456']                                | "/fineGrained/hubId/123456" | [:]                      | [:]                      || 403
        ['DEVELOPER'] | ["r:hubs:1:${UUID.randomUUID()}:a".toString()]   | "/fineGrained/hubId/1"      | [:]                      | [:]                      || 200
        ['DEVELOPER'] | ["r:hubs:2:${UUID.randomUUID()}:a".toString()]   | "/fineGrained/hubId/1"      | [:]                      | [:]                      || 403
        ['DEVELOPER'] | ["r:hubs:1:${UUID.randomUUID()}:b".toString()]   | "/fineGrained/hubId/1"      | [:]                      | [:]                      || 403
        ['DEVELOPER'] | ['r:query:123456']                               | "/fineGrained/queryParam"   | [myQueryParam:'123456']  | [:]                      || 200
        ['DEVELOPER'] | ['r:query:123456']                               | "/fineGrained/queryParam"   | [myQueryParam:'654321']  | [:]                      || 403
        ['USER']      | ['r:query:123456']                               | "/fineGrained/queryParam"   | [myQueryParam:'123456']  | [:]                      || 403
        ['DEVELOPER'] | ["r:query:1:${UUID.randomUUID()}:b".toString()]  |  "/fineGrained/queryParam"  | [myQueryParam:'1']       | [:]                      || 200
        ['DEVELOPER'] | ["r:query:2:${UUID.randomUUID()}:b".toString()]  |  "/fineGrained/queryParam"  | [myQueryParam:'1']       | [:]                      || 403
        ['DEVELOPER'] | ["r:query:1:${UUID.randomUUID()}:a".toString()]  |  "/fineGrained/queryParam"  | [myQueryParam:'1']       | [:]                      || 403
        ['DEVELOPER'] | ['r:header:123456']                              | "/fineGrained/headerParam"  | [:]                      | [myHeaderParam:'123456'] || 200
        ['DEVELOPER'] | ['r:header:123456']                              | "/fineGrained/headerParam"  | [:]                      | [myHeaderParam:'654321'] || 403
        ['USER']      | ['r:header:123456']                              | "/fineGrained/headerParam"  | [:]                      | [myHeaderParam:'123456'] || 403
        ['DEVELOPER'] | ["r:header:1:${UUID.randomUUID()}:c".toString()] | "/fineGrained/headerParam"  | [:]                      | [myHeaderParam:'1']      || 200
        ['DEVELOPER'] | ["r:header:2:${UUID.randomUUID()}:c".toString()] | "/fineGrained/headerParam"  | [:]                      | [myHeaderParam:'1']      || 403
        ['DEVELOPER'] | ["r:header:1:${UUID.randomUUID()}:b".toString()] | "/fineGrained/headerParam"  | [:]                      | [myHeaderParam:'1']      || 403
    }

    def 'principal with null scopes does not throw exception or register filter'() {
        given:
        User user = new User(null, "charliek", "", "", ['DEVELOPER'])
        OAuthToken token = new OAuthToken(Optional.of(user), null, "", "TOKEN", [:])

        when:
        Response response = rule.getJerseyTest().target("/fineGrained/hubId/123456").
            request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer TOKEN").get()

        then:
        1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
        0 * _

        and:
        response.status != 500
    }

    def 'token with no principal rejects access'() {
        given:
        OAuthToken token = new OAuthToken(Optional.EMPTY, null, "", "TOKEN", [:])

        when:
        Response response = rule.getJerseyTest().target("/fineGrained/hubId/123456").
            request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer TOKEN").get()

        then:
        1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
        0 * _

        and:
        response.status == 403
    }

    @Unroll
    def 'when invalid fine grained specification #scopeVal - #varName throws exception'() {
        given:
        FineGrainedScopeAllowed scope = Mock()
        VarInfo varInfo = Mock()

        when:
        new ScopesAllowedDynamicFeature.ScopesAllowedRequestFilter(null,
            (FineGrainedScopeAllowed[])[scope].toArray())

        then:
        thrown(IllegalArgumentException)

        and:
        1 * scope.varInfo() >> varInfo
        1 * varInfo.name() >> varName
        1 * scope.scope() >> scopeVal
        0 * _

        where:
        scopeVal        | varName
        'a:b:c:d:e:f'   | 'a'
        '{a}:b:c:d:e:f' | 'b'
    }

    @Unroll
    def 'When method has #resourceMethod annotations and class has #resourceClass.simpleName annotations then register #doRegister'() {
        given:
        ScopesAllowedDynamicFeature feature = new ScopesAllowedDynamicFeature()
        ResourceInfo resourceInfo = Mock()
        FeatureContext featureContext = Mock()

        when:
        feature.configure(resourceInfo, featureContext)

        then:
        1 * resourceInfo.getResourceMethod() >> NoScope.class.getDeclaredMethod(resourceMethod)
        numGetClass * resourceInfo.getResourceClass() >> resourceClass
        if (doRegister) {
            1 * featureContext.register(_) >> {
                if([resourceMethod, resourceClass.name].any { it.toLowerCase().contains('fine') }) {
                    assert it[0].fineGrainedScopesAllowed[0].scope() == '{fine}'
                }

                if([resourceMethod, resourceClass.name].any { it.toLowerCase().contains('static') }) {
                    assert it[0].scopesAllowed[0] == 'static'
                }


                null
            }
        }
        0 * _

        where:
        resourceMethod       | resourceClass      | doRegister | numGetClass | numScopes | numFineScopes
        'fineAndStaticScope' | FineAndStaticScope | true       | 0           | 1         | 0
        'fineAndStaticScope' | FineScope          | true       | 0           | 1         | 0
        'fineAndStaticScope' | NoScope            | true       | 0           | 1         | 0
        'fineAndStaticScope' | StaticScope        | true       | 0           | 1         | 0
        'fineScope'          | FineAndStaticScope | true       | 1           | 1         | 0
        'fineScope'          | FineScope          | true       | 1           | 1         | 0
        'fineScope'          | NoScope            | true       | 1           | 1         | 0
        'fineScope'          | StaticScope        | true       | 1           | 1         | 0
        'noScope'            | FineAndStaticScope | true       | 2           | 0         | 0
        'noScope'            | FineScope          | true       | 2           | 0         | 0
        'noScope'            | NoScope            | false      | 2           | 0         | 0
        'noScope'            | StaticScope        | true       | 2           | 0         | 0
        'staticScope'        | FineAndStaticScope | true       | 1           | 1         | 0
        'staticScope'        | FineScope          | true       | 1           | 1         | 0
        'staticScope'        | NoScope            | true       | 1           | 1         | 0
        'staticScope'        | StaticScope        | true       | 1           | 1         | 0
    }

    private static class NoScope {
        @ScopesAllowed('static')
        @FineGrainedScopesAllowed([@FineGrainedScopeAllowed(scope='{fine}', varInfo=@VarInfo(name='fine', type=HttpRequestVarType.PATH))])
        void fineAndStaticScope() {}

        @ScopesAllowed('static')
        void staticScope() {}

        @FineGrainedScopesAllowed([@FineGrainedScopeAllowed(scope='{fine}', varInfo=@VarInfo(name='fine', type=HttpRequestVarType.PATH))])
        void fineScope() {}

        void noScope() {}
    }

    @ScopesAllowed('static')
    private static class StaticScope {}

    @ScopesAllowed('static')
    @FineGrainedScopesAllowed([@FineGrainedScopeAllowed(scope='{fine}', varInfo=@VarInfo(name='fine', type=HttpRequestVarType.PATH))])
    private static class FineAndStaticScope {}

    @FineGrainedScopesAllowed([@FineGrainedScopeAllowed(scope='{fine}', varInfo=@VarInfo(name='fine', type=HttpRequestVarType.PATH))])
    private static class FineScope {}
}
