/**
 * SBSSend.java
 */
package connect;

import java.io.BufferedOutputStream;
import jd2xx.JD2XX;

public final class SBSSend {

    private CRC16 crc16;
    private JD2XX jd;
    private BufferedOutputStream bos;
    private boolean network;

    public SBSSend(JD2XX j) {
        this.jd = j;
        this.crc16 = new CRC16();
        this.network = false;
    }

    public SBSSend(BufferedOutputStream o) {
        this.bos = o;
        this.crc16 = new CRC16();
        this.network = true;
    }

    /**
     * Kinetic scramble routine
     *
     * @param msg a byte array of unscrambled data
     * @param k1 an int representing 16-bit key1
     * @param k2 and int representing 16-bit key2
     * @return a byte array containing scrambled data
     */
    public byte[] tx_encode(byte[] msg, int k1, int k2) {
        byte[] output = new byte[msg.length];
        int enc1 = 0;
        int enc2 = 0;
        int key1 = k1 & 0xFFFF;
        int key2 = k2 & 0xFFFF;

        for (int i = 0; i < msg.length; i++) {
            crc16.reset(key1, 0x1021);
            crc16.compute(0xFF);

            key1 = crc16.getCRC();

            crc16.reset(key2, 0x8005);
            crc16.compute(0xFF);

            key2 = crc16.getCRC();

            // Move the 3 LSB to 3 MSB

            enc2 = ((enc1 >>> 3) | (enc1 << 5)) & 0xFF;

            enc1 = (msg[i] ^ enc2) & 0xFF;
            output[i] = (byte) ((enc1 ^ key1 ^ key2) & 0xFF);
        }

        return output;
    }

    public void send(int msgType, byte[] msg, int key1, int key2) {
        byte[] buffer = new byte[msg.length + 7];
        int i, j, q;

        // Scramble packet
        byte[] enc_login = tx_encode(msg, key1, key2);

        // Compute checksum for unscrambled data

        crc16.reset(0, 0x1021);
        crc16.compute(msgType);

        for (i = 0; i < msg.length; i++) {
            crc16.compute(msg[i]);
        }

        // Add the header and footer bytes

        buffer[0] = (byte) 0x10;
        buffer[1] = (byte) 0x02;
        buffer[2] = (byte) msgType;

        for (i = 3, j = 0; j < msg.length; i++, j++) {
            buffer[i] = enc_login[j];
        }

        q = msg.length + 3;

        buffer[q++] = (byte) 0x10;
        buffer[q++] = (byte) 0x03;
        buffer[q++] = (byte) crc16.getMSB();
        buffer[q] = (byte) crc16.getLSB();

        try {
            if (network) {
                bos.write(buffer, 0, buffer.length);
                bos.flush();
            } else {
                jd.write(buffer);         // send data to USB
            }
        } catch (Exception e) {
            System.out.println("VRSBS1Feeder::SBSSend Exception: " + e.getMessage());
        }
    }
}
