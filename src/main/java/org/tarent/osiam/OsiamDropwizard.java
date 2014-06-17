package org.tarent.osiam;

import com.yammer.dropwizard.config.Environment;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.tarent.osiam.config.OsiamConfiguration;

import java.net.URI;

/**
 * OSIAM Dropwizard integration.
 */
public class OsiamDropwizard {

    private final OsiamConfiguration config;
    private RedirectUriProvider redirectUriProvider;

    /**
     * Creates a new OSIAM Dropwizard instance.
     *
     * @param config OSIAM configuration
     * @param rup    RedirectUriProvider
     */
    public OsiamDropwizard(OsiamConfiguration config, RedirectUriProvider rup) {
        this.config = config;
        this.redirectUriProvider = rup;
    }

    /**
     * Configures Dropwizard to use OSIAM by adding a custom Authorization Provider.
     *
     * @param env Dropwizard Environment
     */
    public void configure(Environment env) {
        env.addProvider(new OsiamRoleAuthOAuthProvider(this, "OSIAM protected"));
        env.addResource(new OsiamResource(config, this));
    }

    /**
     * Gets an OsiamConnector.Builder with clientId, clientSecret and endpoint set
     *
     * @return OsiamConnector.Builder object
     */
    public OsiamConnector.Builder getBuilder() {
        return new OsiamConnector.Builder()
                .setEndpoint(config.getEndpoint())
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret());

    }

    /**
     * Gets an OSIAM context.
     *
     * @param username username
     * @param password password
     * @return OsiamContext object.
     */
    public OsiamContext getContextWithUsernamePassword(String username, String password) {
        OsiamConnector con = getBuilder()
                .setScope(Scope.ALL)
                .setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS)
                .setUserName(username)
                .setPassword(password)
                .build();

        return new OsiamContext(con, con.retrieveAccessToken());
    }

    /**
     * Gets an OSIAM context
     *
     * @param accessToken access token
     * @return OsiamContext object
     */
    public OsiamContext getContextWithAccessToken(String accessToken) {
        OsiamConnector con = getBuilder()
                .setScope(Scope.ALL)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .build();

        return new OsiamContext(con, new SimpleAccessToken(accessToken));
    }

    /**
     * Gets an OSIAM context with the client credentials.
     *
     * @return OsiamContext object
     */
    public OsiamContext getClientContext() {
        OsiamConnector con = getBuilder()
                .setScope(Scope.ALL)
                .setGrantType(GrantType.CLIENT_CREDENTIALS)
                .build();

        return new OsiamContext(con, con.retrieveAccessToken());
    }

    /**
     * Returns the login redirect URI.
     *
     * @param state state to be appended to the URI
     * @return URI as String
     */
    public String getRedirectLoginUri(String state) {
        OsiamConnector oConnector = getBuilder()
                .setScope(Scope.ALL)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientRedirectUri(config.getClientRedirectUri())
                .build();
        URI uri = oConnector.getRedirectLoginUri();
        return uri.toString() + "&state=" + state;
    }

    /**
     * Get the RedirectUriProvider.
     *
     * @return RedirectUriProvider
     */
    public RedirectUriProvider getRedirectUriProvider() {
        return redirectUriProvider;
    }


}
