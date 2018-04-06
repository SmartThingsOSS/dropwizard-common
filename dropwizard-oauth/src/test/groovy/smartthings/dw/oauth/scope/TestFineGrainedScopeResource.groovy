package smartthings.dw.oauth.scope

import smartthings.dw.guice.WebResource

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/")
class TestFineGrainedScopeResource implements WebResource {

    @GET
    @Path("/fineGrained/standalone/{param}")
    @FineGrainedScopesAllowed([
        @FineGrainedScopeAllowed(scope="r:path:{param}", varInfo=@VarInfo(name="param", type=HttpRequestVarType.PATH)),
    ])
    String protectedPath(@PathParam("param") String myPathParam) {
        "OK"
    }
}
