package org.tarent.osiam;

import com.yammer.metrics.annotation.Timed;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.OsiamRequestException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.tarent.osiam.config.OsiamConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mley on 24.03.14.
 */
@Path("/osiam")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OsiamResource {

    private final OsiamDropwizard osiam;
    private final OsiamConfiguration config;

    /**
     * Creates a new OSIAM resource.
     *
     * @param config configuration
     * @param od     OSIAM Dropwizard object
     */
    public OsiamResource(OsiamConfiguration config, OsiamDropwizard od) {
        this.config = config;
        this.osiam = od;
    }

    /**
     * Perform the login. This method checks if the client passed an access token from a short lived cookie and if the
     * access token is valid. If no valid access token was given, it responds with a redirect URI for the client
     * application to redirect the user to login. If the access token is valid, it is returned in the response.
     *
     * @param accessToken accesstoken from cookie
     * @param state       state from client application
     * @return Response
     */
    @Timed
    @GET
    @Path("/login")
    public Response login(@CookieParam("accessToken") String accessToken, @QueryParam("state") String state) {
        Response.ResponseBuilder rb = Response.ok();

        if (state == null) {
            throw new IllegalStateException("no state given");
        }

        boolean accessTokenValid = false;
        if (accessToken != null) {
            try {
                OsiamContext context = osiam.getContextWithAccessToken(accessToken);

                if (context.getCurrentUser() != null) {
                    accessTokenValid = true;
                }
            } catch (OsiamRequestException ue) {

            }
        }

        Map<String, String> result = new HashMap<String, String>();

        if (!accessTokenValid) {
            String uri = osiam.getRedirectLoginUri(state);
            uri = uri.replace("server//oauth", "server/oauth");
            result.put("redirectUri", uri);
        } else {
            result.put("accessToken", accessToken);
        }

        rb.entity(result);
        return rb.build();
    }

    /**
     * This method handles the redirect from the OSIAM login back to the client application. The auth code is used to
     * retrieve the access token, which is then saved in short lived cookie. The state parameter is passed to the
     * RedirectUriProvider of the client application, which returns an URI where client application continues. The
     * access token is saved in a short lived cookie, to be accessed by the login method.
     *
     * @param authCode authorization code
     * @param state    state
     * @return the access token
     */
    @Timed
    @GET
    @Path("/oauth2")
    public Response oauth2(@QueryParam("code") String authCode, @QueryParam("state") String state) {
        OsiamConnector oc = osiam.getBuilder()
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setScope(Scope.ALL)
                .setClientRedirectUri(config.getClientRedirectUri())
                .build();
        AccessToken at = oc.retrieveAccessToken(authCode);

        if (state == null) {
            throw new IllegalStateException("no state given in request");
        }


        URI uri = osiam.getRedirectUriProvider().getRedirectUri(state);
        Response.ResponseBuilder rb = null;
        rb = Response.temporaryRedirect(uri);

        //fixme this cookie should only live some seconds
        NewCookie cookie = new NewCookie("accessToken", at.getToken());
        rb.cookie(cookie);

        return rb.build();
    }


}
