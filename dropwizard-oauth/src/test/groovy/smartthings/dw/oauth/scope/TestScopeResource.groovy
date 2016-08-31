package smartthings.dw.oauth.scope

import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.Path

@ScopesAllowed("admin")
@Path("/")
class TestScopeResource {

	@GET
	@Path("/classProtected")
	public String classProtected() {
		return "OK";
	}

	@GET
	@Path("/methodProtected")
	@ScopesAllowed("superuser")
	public String methodProtected() {
		return "OK";
	}

	@GET
	@Path("/methodProtectedWithRole")
	@RolesAllowed("DEVELOPER")
	public String methodProtectedWithRole() {
		return "OK";
	}

	@GET
	@Path("/methodProtectedWithRoleScope")
	@RolesAllowed("DEVELOPER")
	@ScopesAllowed("superuser")
	public String methodProtectedWithRoleScope() {
		return "OK";
	}

}
