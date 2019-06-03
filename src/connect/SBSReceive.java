/**
 * SBSReceive.java
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import jd2xx.JD2XX;

public final class SBSReceive implements Runnable {

    private Thread dataReceive;
    private JD2XX jd;
    private CircularByteBuffer cb;
    private OutputStream bout;
    private BufferedInputStream bis;
    private boolean network;

    /*
     * USB Constructor
     */
    public SBSReceive(JD2XX j, CircularByteBuffer c) {
        this.jd = j;
        this.cb = c;
        this.bout = cb.getOutputStream();
        this.network = false;

        dataReceive = new Thread(this);
        dataReceive.setName("SBSReceive USB");
        dataReceive.setPriority(Thread.NORM_PRIORITY);
        dataReceive.start();
    }

    /*
     * Network Constructor
     */
    public SBSReceive(BufferedInputStream is, CircularByteBuffer c) {
        this.bis = is;
        this.cb = c;
        this.bout = cb.getOutputStream();
        this.network = true;

        dataReceive = new Thread(this);
        dataReceive.setName("SBSReceive NET");
        dataReceive.setPriority(Thread.NORM_PRIORITY);
        dataReceive.start();
    }

    /*
     * Thread to read the data from the USB/Network port and buffer it
     */
    @Override
    public void run() {
        byte[] dataByte;
        int available, val;

        while (true) {
            /*
             * Check for new data
             */

            if (network) {
                try {
                    while ((available = bis.available()) > 0) {
                        dataByte = new byte[available];
                        val = bis.read(dataByte, 0, available);

                        if (val == -1) {
                            break;
                        }

                        /*
                         * Write data to Buffer Might be zero on error
                         */

                        if (val > 0) {
                            bout.write(dataByte, 0, val);
                        }
                    }

                    Thread.sleep(0, 1);
                } catch (IOException | InterruptedException e1) {
                }
            } else {
                try {
                    while ((available = jd.getQueueStatus()) > 0) {
                        dataByte = new byte[available];

                        /*
                         * This will wait until USB device setTimeouts setting
                         */
                        val = jd.read(dataByte, 0, available);

                        /*
                         * Write the data to Buffer Might be zero if read()
                         * timeout
                         */


                        if (val > 0) {
                            bout.write(dataByte, 0, val);
                        }
                    }

                    Thread.sleep(0, 1);
                } catch (IOException | InterruptedException e2) {
                }
            }
        }
    }
}
