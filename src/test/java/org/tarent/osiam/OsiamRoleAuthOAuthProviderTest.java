package org.tarent.osiam;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import org.junit.Test;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.User;
import org.tarent.osiam.annotation.RestrictedTo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: mley
 * Date: 03.04.14
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class OsiamRoleAuthOAuthProviderTest {

    @Test
    public void testSuccessfulAuthentication() {
        OsiamContext oc = testAuthenticate("restricted", "restricted");
        assertNotNull(oc);
    }

    @Test
    public void testNoRestriction() {
        OsiamContext oc = testAuthenticate("restricted", null);
        assertNotNull(oc);
    }

    @Test(expected = WebApplicationException.class)
    public void testAuthFail() {
        testAuthenticate(null, "restricted");
    }

    public OsiamContext testAuthenticate(String usersGroup, String restrictedGroup) {
        OsiamDropwizard od = mock(OsiamDropwizard.class);

        OsiamContext octx = mock(OsiamContext.class);

        // return osiam context mock, when supplied with user and password
        when(od.getContextWithAccessToken(eq("myAccessToken"))).thenReturn(octx);
        List<GroupRef> groups = new ArrayList<GroupRef>();


        if(usersGroup != null) {
            // if group is supplied, we add this group to the users groups
            groups.add(new GroupRef.Builder().setDisplay(restrictedGroup).build());
        }

        // mock getCurrentUser
        when(octx.getCurrentUser()).thenReturn(new User.Builder("marissa").setGroups(groups).build());

        OsiamRoleAuthOAuthProvider orap = new OsiamRoleAuthOAuthProvider(od, "realm");

        RestrictedTo rt = mock(RestrictedTo.class);
        if(restrictedGroup != null) {
            // if restrictedTo group is supplied, we mock this
            when(rt.value()).thenReturn(new String[]{restrictedGroup});
        }

        AbstractHttpContextInjectable<OsiamContext> inj = (AbstractHttpContextInjectable) orap.getInjectable(null, rt, null);

        HttpContext ctx = mock(HttpContext.class);
        HttpRequestContext rctx = mock(HttpRequestContext.class);
        when(ctx.getRequest()).thenReturn(rctx);
        // mock header: username/password: marissa/koala
        when(rctx.getHeaderValue(HttpHeaders.AUTHORIZATION)).thenReturn("bearer myAccessToken");

        return inj.getValue(ctx);
    }
}
