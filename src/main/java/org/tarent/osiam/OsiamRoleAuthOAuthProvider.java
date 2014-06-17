package org.tarent.osiam;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.osiam.client.exception.OsiamRequestException;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tarent.osiam.annotation.RestrictedTo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * Dropwizard authentication/authorization provider. This provider is called on all REST Resources annotated with the
 *
 * @RestrictedTo annotiation. It looks for the access token in the Request header, checks its validity and checks if
 * the user is in the apropriate group to access the resource.
 */
public class OsiamRoleAuthOAuthProvider implements InjectableProvider<RestrictedTo, Parameter> {


    private static final Logger LOGGER = LoggerFactory.getLogger(OsiamRoleAuthOAuthProvider.class);
    private final OsiamDropwizard osiam;
    private final String realm;

    /**
     * Creates a new OsiamRoleAuthBasicProvider
     *
     * @param osiam
     * @param realm
     */
    public OsiamRoleAuthOAuthProvider(OsiamDropwizard osiam, String realm) {
        this.osiam = osiam;
        this.realm = realm;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, RestrictedTo rt, Parameter c) {
        return new OsiamRoleAuthInjectable(osiam, rt.value(), realm);
    }

    /**
     * OsiamRoleAuthIncectable class.
     */
    private static class OsiamRoleAuthInjectable extends AbstractHttpContextInjectable {
        private static final String HEADER_NAME = "WWW-Authenticate";
        private static final String HEADER_VALUE = "Bearer realm=\"%s\"";
        private static final String PREFIX = "bearer";
        private final OsiamDropwizard odw;
        private final String realm;
        private final String[] allowedGroups;

        /**
         * Creates a new OsiamRoleAuthInjectable
         *
         * @param odw   OsiamDropwizard object
         * @param ag    array of allowed groups
         * @param realm protected realm
         */
        public OsiamRoleAuthInjectable(OsiamDropwizard odw, String[] ag, String realm) {
            this.odw = odw;
            String[] myAllowedGroups = new String[0];
            if (ag != null) {
                myAllowedGroups = Arrays.copyOf(ag, ag.length);
                // the array of allowed groups must be sorted, so Arrays.binarySearch works properly
                Arrays.sort(myAllowedGroups);
            }
            this.allowedGroups = myAllowedGroups;
            this.realm = realm;
        }

        /**
         * Checks if user has at least one the allowed roles.
         *
         * @param u            User
         * @param allowedRoles Array of allowed roles.
         * @return true if user is member in at least one of the allowed roles, else false.
         */
        private boolean userHasAllowedRole(User u, String[] allowedRoles) {

            for (GroupRef group : u.getGroups()) {

                if (Arrays.binarySearch(allowedRoles, group.getDisplay()) >= 0) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public OsiamContext getValue(HttpContext c) {
            final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
            if (header != null) {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (PREFIX.equalsIgnoreCase(method)) {
                        final String accessToken = header.substring(space + 1);
                        OsiamContext oc = odw.getContextWithAccessToken(accessToken);
                        try {
                            User u = oc.getCurrentUser();

                            //if no roles/groups are specified, user just has to login
                            if (allowedGroups.length == 0 || userHasAllowedRole(u, allowedGroups)) {
                                return oc;
                            }
                        } catch (OsiamRequestException ore) {
                            LOGGER.error("user not authorized: " + ore.getMessage());
                        }
                    }
                }
            }


            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HEADER_NAME,
                            String.format(HEADER_VALUE,
                                    realm)
                    )
                    .entity("Credentials are required to access this resource.")
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build());


        }

    }

}