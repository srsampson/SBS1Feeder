/**
 * Buffer Overflow Exception Copyright (C) 2002-2010 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 */
package connect;

import java.io.IOException;

/**
 * An indication that there was a buffer overflow.
 *
 * @author Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class BufferOverflowException extends IOException {

    /**
     * Serial version ID
     */
    private static final long serialVersionUID = -322401823167626048L;

    /**
     * Create a new Exception
     *
     * @since ostermillerutils 1.00.00
     */
    public BufferOverflowException() {
        super();
    }

    /**
     * Create a new Exception with the given message.
     *
     * @param msg Error message.
     *
     * @since ostermillerutils 1.00.00
     */
    public BufferOverflowException(String msg) {
        super(msg);
    }
}
