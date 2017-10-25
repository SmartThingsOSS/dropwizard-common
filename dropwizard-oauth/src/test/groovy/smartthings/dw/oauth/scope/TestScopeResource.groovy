package smartthings.dw.oauth.scope

import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam

@ScopesAllowed("admin")
@Path("/")
class TestScopeResource {

	@GET
	@Path("/classProtected")
	String classProtected() {
		"OK"
	}

	@GET
	@Path("/methodProtected")
	@ScopesAllowed("superuser")
	String methodProtected() {
        "OK"
	}

	@GET
	@Path("/methodProtectedWithRole")
	@RolesAllowed("DEVELOPER")
	String methodProtectedWithRole() {
        "OK"
	}

	@GET
	@Path("/methodProtectedWithRoleScope")
	@RolesAllowed("DEVELOPER")
	@ScopesAllowed("superuser")
	String methodProtectedWithRoleScope() {
        "OK"
	}

    @GET
    @Path("/fineGrained/hubId/{hubId}")
    @RolesAllowed("DEVELOPER")
    @ScopesAllowed("superuser")
    @FineGrainedScopesAllowed([
        @FineGrainedScopeAllowed(scope="r:hubs:{hubId}", varInfo=@VarInfo(name="hubId", type=HttpRequestVarType.PATH)),
        @FineGrainedScopeAllowed(scope="w:hubs:{hubId}", varInfo=@VarInfo(name="hubId", type=HttpRequestVarType.PATH)),
        @FineGrainedScopeAllowed(scope="r:hubs:{hubId}:*:a", varInfo=@VarInfo(name="hubId", type=HttpRequestVarType.PATH)),
    ])
    String protectedHubId(@PathParam("hubId") String hubId) {
        "OK"
    }

    @GET
    @Path("/fineGrained/queryParam")
    @RolesAllowed("DEVELOPER")
    @FineGrainedScopesAllowed([
        @FineGrainedScopeAllowed(scope="r:query:{myQueryParam}", varInfo=@VarInfo(name="myQueryParam", type=HttpRequestVarType.QUERY)),
        @FineGrainedScopeAllowed(scope="r:query:{myQueryParam}:*:b", varInfo=@VarInfo(name="myQueryParam", type=HttpRequestVarType.QUERY)),
    ])
    String protectedQuery(@QueryParam("myQueryParam") String myQueryParam) {
        "OK"
    }

    @GET
    @Path("/fineGrained/headerParam")
    @RolesAllowed("DEVELOPER")
    @FineGrainedScopesAllowed([
        @FineGrainedScopeAllowed(scope="r:header:{myHeaderParam}", varInfo=@VarInfo(name="myHeaderParam", type=HttpRequestVarType.HEADER)),
        @FineGrainedScopeAllowed(scope="r:header:{myHeaderParam}:*:c", varInfo=@VarInfo(name="myHeaderParam", type=HttpRequestVarType.HEADER))
    ])
    String protectedHeader(@HeaderParam("myHeaderParam") String myHeaderParam) {
        "OK"
    }
}
