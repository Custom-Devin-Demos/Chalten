package org.chalten.measure;

import java.io.Serial;

/**
 * Port of Smalltalk's {@code InstanceCreationFailed}: an object could not be created from the given arguments. The
 * message text is identical to the one signalled by the Smalltalk implementation.
 */
public class InstanceCreationFailedException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InstanceCreationFailedException(String message) {
        super(message);
    }
}
