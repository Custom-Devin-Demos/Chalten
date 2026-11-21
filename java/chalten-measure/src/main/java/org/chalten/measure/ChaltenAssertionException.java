package org.chalten.measure;

import java.io.Serial;

/**
 * Port of Smalltalk's {@code AssertionFailed}: a domain rule was violated. The message text is identical to the one
 * signalled by the Smalltalk implementation.
 */
public class ChaltenAssertionException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ChaltenAssertionException(String message) {
        super(message);
    }
}
