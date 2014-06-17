package org.tarent.osiam;

import java.io.UnsupportedEncodingException;

/**
 * Created by mley on 01.04.14.
 */
public class OsiamDropwizardRuntimeException extends RuntimeException {

    /**
     * Creates a new OsiamDropwizardRuntimeException
     *
     * @param s message
     * @param e originating exception
     */
    public OsiamDropwizardRuntimeException(String s, UnsupportedEncodingException e) {
        super(s, e);
    }

    /**
     * Creates a new OsiamDropwizardRuntimeException
     *
     * @param e originating exception.
     */
    public OsiamDropwizardRuntimeException(Exception e) {
        super(e);
    }
}
