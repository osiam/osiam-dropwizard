package org.tarent.osiam;

import org.junit.Before;
import org.junit.Test;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.OsiamRequestException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.resources.scim.User;
import org.tarent.osiam.config.OsiamConfiguration;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: mley
 * Date: 03.04.14
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public class OsiamResourceTest {

    OsiamResource or;
    OsiamConfiguration config;
    OsiamDropwizard od;
    RedirectUriProvider rup;

    @Before
    public void setup() {
        config = mock(OsiamConfiguration.class);
        od = mock(OsiamDropwizard.class);

        rup = new RedirectUriProvider() {
            @Override
            public URI getRedirectUri(String state) {
                try {
                    return new URI("http://" + state);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        when(od.getRedirectUriProvider()).thenReturn(rup);
        when(od.getRedirectLoginUri(anyString())).thenReturn("redirecturi");
        or = new OsiamResource(config, od);

    }

    @Test
    public void testLogin() {
        OsiamContext oc = mock(OsiamContext.class);
        User user = new User.Builder("user").build();
        when(oc.getCurrentUser()).thenReturn(user);

        when(od.getContextWithAccessToken(eq("accesstoken"))).thenReturn(oc);

        Response resp = or.login("accesstoken", "state");

        Map<String, String> ent = (Map<String, String>) resp.getEntity();
        assertEquals(ent.get("accessToken"), "accesstoken");

        when(oc.getCurrentUser()).thenThrow(new OsiamRequestException(401, "invalid token"));
        resp = or.login("accesstoken", "state");
        ent = (Map<String, String>) resp.getEntity();
        assertEquals(ent.get("redirectUri"), "redirecturi");

        resp = or.login(null, "state");
        ent = (Map<String, String>) resp.getEntity();
        assertEquals(ent.get("redirectUri"), "redirecturi");

    }

    @Test
    public void testOAuth() {
        OsiamConnector.Builder bldr = mock(OsiamConnector.Builder.class);
        when(bldr.setGrantType(any(GrantType.class))).thenReturn(bldr);
        when(bldr.setScope(any(Scope.class))).thenReturn(bldr);
        when(bldr.setClientRedirectUri(anyString())).thenReturn(bldr);

        OsiamConnector con = mock(OsiamConnector.class);
        when(bldr.build()).thenReturn(con);

        AccessToken at = new SimpleAccessToken("accesstoken");
        when(con.retrieveAccessToken(eq("authcode"))).thenReturn(at);

        when(od.getBuilder()).thenReturn(bldr);

        Response r = or.oauth2("authcode", "state");
        NewCookie c = (NewCookie)r.getMetadata().getFirst("Set-Cookie");
        assertEquals(c.getValue(), "accesstoken");
        URI u = (URI)r.getMetadata().getFirst("Location");
        assertEquals(u.toString(), "http://state");
    }
}
