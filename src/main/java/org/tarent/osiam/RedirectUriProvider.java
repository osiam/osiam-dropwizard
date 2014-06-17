package org.tarent.osiam;

import java.net.URI;

/**
 * RedirectUriProvider. The Dropwizard application implements this interface to navigate the browser back to application
 * after the user has logged in.
 */
public interface RedirectUriProvider {

    /**
     * Returns the redirect URI after login
     *
     * @param state client state of the client. The client application can add the state parameter to the login redirect
     *              URI, which is then passed back here, to determine the application state.
     * @return URI redirect URI
     */
    URI getRedirectUri(String state);
}
