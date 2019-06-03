/**
 * ProcessSBSData.java
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
 * with this program. If not, see <http://www.gnu.org/licenses/>..
 */
package connect;

import feeder.ZuluMillis;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * This class scans the SBS-1 raw data and after finding the start and end of
 * each packet, pushes the packets onto the Target Queue.
 */
public final class ProcessSBSData implements Runnable {

    private static final byte DLE = 0x10;
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    //
    private Thread process;
    private CRC16 crc16;
    private CircularByteBuffer cb;
    private InputStream bin;
    private Vector<DataBlock> targetQueue;
    private ZuluMillis zulu;

    public ProcessSBSData(CircularByteBuffer c) {
        this.zulu = new ZuluMillis();
        this.cb = c;
        this.bin = this.cb.getInputStream();
        this.crc16 = new CRC16();
        this.targetQueue = new Vector<DataBlock>();

        process = new Thread(this);
        process.setName("ProcessSBSData");
        process.setPriority(Thread.NORM_PRIORITY);
        process.start();
    }

    /**
     * Method to functionally shutdown the program by setting EOF to true
     */
    public void close() {
        resetQueue();
    }

    /**
     * Method to determine how many packets are on the queue
     *
     * @return int a value representing how many packets are on the queue
     */
    public int getQueueSize() {
        return targetQueue.size();
    }

    /**
     * Method to clear the queue and start over
     */
    public void resetQueue() {
        try {
            targetQueue.clear();
        } catch (Exception e) {
        }
    }

    /**
     * Kinetic unscramble routine
     *
     * @param msg a byte array of scrambled data
     * @param k1 an int representing key1
     * @param k2 and int representing key2
     * @return a byte array containing unscrambled data
     */
    private byte[] rx_decode(byte[] msg, int k1, int k2) {
        byte[] output = new byte[msg.length];
        int enc1 = 0;
        int enc2 = 0;
        int enc3 = 0;
        int key1 = k1 & 0xFFFF;
        int key2 = k2 & 0xFFFF;

        // Take each message byte in turn and descramble:

        for (int i = 0; i < msg.length; i++) {
            crc16.reset(key1, 0x1021);
            crc16.compute(0xFF);

            key1 = crc16.getCRC();

            crc16.reset(key2, 0x8005);
            crc16.compute(0xFF);

            key2 = crc16.getCRC();

            // Move the 3 LSB to 3 MSB

            enc2 = ((enc1 >>> 3) | (enc1 << 5)) & 0xFF;

            enc3 = (msg[i] ^ enc2 ^ key1 ^ key2) & 0xFF;
            output[i] = (byte) enc3;
            enc1 = (enc2 ^ enc3) & 0xFF;
        }

        return output;
    }

    public DataBlock receive(int expected, int key1, int key2) {
        DataBlock db;

        if (expected == 0) {        // take anything
            db = popScrambledData();
        } else {
            do {
                db = popScrambledData();
            } while (db.getType() != expected);
        }

        // Descramble received message:

        byte[] rx = rx_decode(db.getScrambledData(), key1, key2);
        db.setUnScrambledData(rx);

        // Verify received checksum

        crc16.reset(0, 0x1021);
        crc16.compute(db.getType());

        for (int i = 0; i < rx.length; i++) {
            crc16.compute(rx[i]);
        }

        db.setCalcChecksum(crc16.getCRC());

        db.removeCounter();     // remove counter after checksum

        return db;
    }

    /*
     * Push the scrambled receive packet onto the queue
     */
    private void pushScrambledData(int type, int cksum, byte[] a, int n) {
        DataBlock obj = null;

        obj = new DataBlock(zulu.getUTCTime(), type, cksum, a, n);

        if (obj != null) {
            boolean val = targetQueue.add(obj);

            if (val != true) {
                System.out.println("VRSBS1Feeder::ProcessSBSData could not add DataBlock to queue");
            }
        }
    }

    /*
     * Pop a scrambled packet off the queue
     */
    private DataBlock popScrambledData() {
        while (targetQueue.isEmpty()) {
            try {
                Thread.sleep(0, 1);
            } catch (Exception e) {
            }
        }

        return targetQueue.remove(0);
    }

    /**
     * Thread to read the data from the buffer and parse it
     * into individual Mode-S packets
     */
    @Override
    public void run() {
        byte tmp, val, crch, crcl;
        byte[] modes;
        int type, n, cksum;
        boolean done;

        while (true) {
            try {

                if (bin.available() == 0) {
                    try {
                        Thread.sleep(0, 1);
                    } catch (Exception z) {
                    }

                    continue;
                }

                /*
                 * Start off by finding the start of the frame
                 */

                while (true) {
                    if (bin.read() == DLE) {      // <DLE> possible start of frame
                        break;
                    }
                }

                /*
                 * Found the <DLE> byte, we are looking for the <STX>
                 */

                while (true) {
                    if (bin.read() != STX) {       // <STX> start of frame byte
                        break;                    // oops, we lose, back to top
                    }

                    /*
                     * We have a <DLE><STX> now comes the type code
                     */

                    type = (byte) bin.read();
                    done = false;

                    // Data starts here

                    n = 0;
                    modes = new byte[256];

                    while (true) {
                        if ((tmp = (byte) bin.read()) != DLE) {            // <DLE> byte?
                            modes[n++] = tmp;                      // no, so it must be data
                        } else {
                            if ((tmp = (byte) bin.read()) == ETX) {        // <ETX> byte? (end of data)

                                /*
                                 * The CRC bytes are also DLE escaped
                                 */

                                val = (byte) bin.read();

                                if (val == DLE) {
                                    crch = (byte) bin.read();        // read CRC MSB
                                } else {
                                    crch = val;
                                }

                                val = (byte) bin.read();

                                if (tmp == DLE) {
                                    crcl = (byte) bin.read();        // read CRC LSB
                                } else {
                                    crcl = val;
                                }

                                cksum = (((crch & 0xFF) << 8) ^ ((crcl & 0xFF) & 0xFFFF));

                                switch (type) {
                                    case 1:     // ADS-B DF17 (112 bits + 4-Byte counter)
                                    case 5:     // Long Mode-S (112 bits + 4-Byte counter)
                                        if (n == 18) {
                                            pushScrambledData(type, cksum, modes, n);
                                        }
                                        break;
                                    case 7:     // Short Mode-S (56 bits + 4-Byte counter)
                                        if (n == 11) {
                                            pushScrambledData(type, cksum, modes, n);
                                        }
                                        break;
                                    case 2:     // SBS-1 commands (not used)
                                        break;
                                    default:    // Login sequence probably
                                        pushScrambledData(type, cksum, modes, n);
                                }

                                modes = null;       // force garbage collection
                                done = true;
                                break;
                            } else if (tmp == DLE) {   // escape <DLE><DLE> pair
                                modes[n++] = DLE;              // replace with one <DLE>
                            }
                        }
                    }

                    if (done == true) {
                        break;
                    }
                }

                Thread.sleep(0, 1);
            } catch (IOException | InterruptedException e) {
            }
        }
    }
}
