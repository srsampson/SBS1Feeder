/**
 * PacketServer.java
 */
package feeder;

import connect.DataBlock;
import connect.ProcessSBSData;
import connect.SBSDevice;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * A class to send the SBS received packets to the multicast network listeners
 */
public final class PacketServer implements Runnable {

    private static final long POLY = 0xFFFA0480;        // Polynomial
    //
    private Config config;
    private ProcessSBSData procData;
    private MulticastSocket msocket;
    private Thread parser;
    private int key1;
    private int key2;
    private int rid;

    public PacketServer(ProcessSBSData pd, MulticastSocket ms, Config c, SBSDevice dev) {
        this.msocket = ms;
        this.config = c;
        this.procData = pd;         // raw data queue
        this.key1 = dev.getKey1();
        this.key2 = dev.getKey2();
        this.rid = c.getRadarID();

        parser = new Thread(this);
        parser.setName("PacketServer");
        parser.setPriority(Thread.NORM_PRIORITY);
        parser.start();
    }

    @Override
    public void run() {
        DataBlock dbk;
        String pkt;
        byte[] rx;
        int type;

        while (true) {
            while (procData.getQueueSize() > 0) {
                dbk = procData.receive(0, this.key1, this.key2);
                type = dbk.getType();

                /*
                 * Make sure it is a data type we are interested in
                 */

                if (type == 5 || type == 7 || type == 1) {
                    rx = dbk.getUnScrambledData();

                    /*
                     * Throw out the packets with bad checksums
                     */


                    if (dbk.getCalcChecksum() == dbk.getRXChecksum()) {
                        /*
                         * Convert it to a string
                         */

                        pkt = "";
                        for (int n = 0; n < rx.length; n++) {
                            pkt += String.format("%02X", rx[n]);
                        }

                        servePacket(pkt, dbk.getCounter(), dbk.getTimestamp(), rid, (rx[0] >>> 3) & 0x1F);
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }

    /*
     * Send the packet to Multicast listeners
     */
    private void servePacket(String p, String counter, String ts, int rid, int df) {
        /*
         * The SBS-1 does the parity in firmware, so to masquerade as an AVR
         * receiver, we have to put the parity back in.
         */

        String packet = "";
        String tmp = "";
        String parity = "";

        if (p.length() > 14) {          // ADS-B Long Packet
            tmp = p.substring(0, 22);   // 88 bits
            parity = parity112bit(tmp);

            if (df == 17) { // "PI" field
                packet = String.format("SBS1,%d,%s,%s,%s\r\n", rid, ts, counter, tmp + parity);
            } else {        // "AP" field
                /*
                 * The AP field must have the real ICAO ID XOR with Parity
                 */
                long val = (Long.parseLong(p.substring(22), 16) ^ Long.parseLong(parity, 16)) & 0xFFFFFF;
                packet = String.format("SBS1,%d,%s,%s,%s\r\n", rid, ts, counter, tmp + String.format("%06X", val));
            }
        } else {                        // ADS-B Short Packet
            tmp = p.substring(0, 8);    // 32 bits
            parity = parity56bit(tmp);

            if (df == 11) {
                packet = String.format("SBS1,%d,%s,%s,%s\r\n", rid, ts, counter, tmp + parity);
            } else {
                long val = (Long.parseLong(p.substring(8), 16) ^ Long.parseLong(parity, 16)) & 0xFFFFFF;
                packet = String.format("SBS1,%d,%s,%s,%s\r\n", rid, ts, counter, tmp + String.format("%06X", val));
            }
        }
        
        try {
            byte[] buffer = packet.getBytes("US-ASCII");
            msocket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(config.getMulticastHost()), config.getMulticastPort()));
            buffer = null;
        } catch (Exception e) {
            // Punt
        }
    }
    
    /**
     * A polynomial CRC algorithm for short packets
     *
     * @param val a string containing the first 32 bits
     * @return a string Representing the parity
     */
    public String parity56bit(String val) {

        /*
         * We should have received 4 bytes, or 8 hex characters
         */

        if (val.length() != 8) {
            return "000000";
        }

        /*
         * Convert the 8 hex character frame into a 32 bit result
         */

        int data = Integer.parseInt(val, 16);

        /*
         * Run the data through the polynomial
         */

        for (int i = 0; i < 32; i++) {
            if ((data & 0x80000000) != 0) {
                data ^= POLY;
            }

            data <<= 1;
        }

        return String.format("%06X", data >>> 8);
    }

    /**
     * A polynomial CRC algorithm for long packets
     *
     * @param val a string containing the first 88 bits
     * @return a string Representing the parity
     */
    public static String parity112bit(String val) {
        /*
         * We should have received 11 bytes, or 22 hex characters
         */

        if (val.length() != 22) {
            return "000000";
        }

        /*
         * Convert the 22 hex characters frame into a 88 bit result Java barfs
         * if the msb is 1 in a 32-bit number during parse so I use an (int) cast
         * on the msb numbers
         */

        int data = (int) Long.parseLong(val.substring(0, 8), 16);// Bytes 1 - 4
        int data1 = (int) Long.parseLong(val.substring(8, 16), 16);// Bytes 5 - 8
        int data2 = Integer.parseInt(val.substring(16), 16) << 8;// Bytes 9 - 11

        /*
         * Run the data through the polynomial
         */

        for (int i = 0; i < 88; i++) {
            if ((data & 0x80000000) != 0) {
                data ^= POLY;
            }

            data <<= 1;

            if ((data1 & 0x80000000) != 0) {
                data |= 1;
            }

            data1 <<= 1;

            if ((data2 & 0x80000000) != 0) {
                data1 = data1 | 1;
            }

            data2 <<= 1;
        }

        return String.format("%06X", data >>> 8);
    }
}
