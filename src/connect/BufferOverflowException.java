/**
 * Buffer Overflow Exception Copyright (C) 2002-2010 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * <p>This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * <p>You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
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