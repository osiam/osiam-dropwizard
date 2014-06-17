package org.tarent.osiam;


import org.osiam.client.oauth.AccessToken;

import java.util.Objects;

/**
 * Simple AccesToken implementation.
 */
public class SimpleAccessToken extends AccessToken {

    private String token;

    /**
     * Creates a new SimpleAccessToken.
     *
     * @param token access token
     */
    public SimpleAccessToken(String token) {
        this.token = token;
    }

    /**
     * Retrieve the string value of the access token used to authenticate against the provider.
     *
     * @return The access token string
     */
    @Override
    public String getToken() {
        return token;
    }

    /**
     * use the basic class {@link AccessToken} to retrieve type
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public String getType() {
        throw new UnsupportedOperationException();
    }

    /**
     * use the basic class {@link AccessToken} to retrieve experiseIn
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public int getExpiresIn() {
        throw new UnsupportedOperationException();
    }

    /**
     * use the basic class {@link AccessToken} to retrieve expired
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean isExpired() {
        throw new UnsupportedOperationException();
    }

    /**
     * use the basic class {@link AccessToken} to retrieve scope
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public String getScope() {
        throw new UnsupportedOperationException();
    }

    /**
     * use the basic class {@link AccessToken} to retrieve refreshToken
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public String getRefreshToken() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessToken that = (AccessToken) o;

        return token.equals(that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.token);
    }
}
