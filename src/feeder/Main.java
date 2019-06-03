/**
 * Main.java
 */
package feeder;

import connect.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Locale;
import jd2xx.JD2XX;

/**
 * VRSBS1Feeder
 *
 * <p>Virtual Radar Network Feeder Program
 *
 * <p>This program connects to an SBS-1 Mode-S detector, and sends the data to a
 * Multicast UDP port in a common ASCII format.
 *
 * @author coupaydeville at gmail dot com
 * @version 1.03
 * @date March 2012
 */
public final class Main {

    private static ProcessSBSData procData;
    //
    private static String configFile = "vrsbs1feeder.conf";
    private static Config config;
    private static CircularByteBuffer cb;
    private static BufferedInputStream bis;
    private static BufferedOutputStream bos;
    private static MulticastSocket ms;
    private static Socket connection;
    private static SBSSend send;
    private static SBSReceive recv;

    public static void main(String[] args) {

        /*
         * The user may have a commandline option as to which config file to use
         */

        try {
            if (args[0].equals("-c") || args[0].equals("/c")) {
                configFile = args[1];
            }
        } catch (Exception e) {
        }

        Locale.setDefault(Locale.US);

        config = new Config(configFile);
        System.out.println("Using config file: " + config.getOSConfPath());

        /*
         * Create a Ring Buffer and have the receive thread fill it. Then the
         * process thread will access it.
         *
         * The data send is independant of receive
         */

        cb = new CircularByteBuffer();

        if (!config.getDetector().equals("E")) {
            // USB Interface needed

            JD2XX jd = new JD2XX();

            try {
                switch (config.getDetector()) {
                    case "A":   // SBS-1
                        jd.openByDescription("Kinetic SBS-1 Beavis 8A");
                        jd.setBaudRate(JD2XX.BAUD_921600);
                        jd.setFlowControl(JD2XX.FLOW_NONE, 0, 0);
                        break;
                    case "B":   // SBS-1
                        jd.openByDescription("Kinetic SBS-1 Beavis 3A B");
                        jd.setBaudRate(JD2XX.BAUD_921600);
                        jd.setFlowControl(JD2XX.FLOW_NONE, 0, 0);
                        break;
                    case "C":   // SBS-1
                        jd.openByDescription("Kinetic SBS-1 Beavis 3A A");
                        jd.setBaudRate(JD2XX.BAUD_921600);
                        jd.setFlowControl(JD2XX.FLOW_NONE, 0, 0);
                        break;
                    default:
                        // If not one of the above, then give up
                        System.out.println("VRSBS1Feeder::Main unknown input device port: " + config.getDetector());
                        System.exit(-1);
                        break;
                }

                jd.setDataCharacteristics(JD2XX.BITS_8, JD2XX.STOP_BITS_1, JD2XX.PARITY_NONE);
                jd.setTimeouts(JD2XX.DEFAULT_RX_TIMEOUT, JD2XX.DEFAULT_TX_TIMEOUT);
                jd.purge(JD2XX.PURGE_RX | JD2XX.PURGE_TX);
            } catch (Exception e) {
                System.err.println("VRSBS1Feeder::Main No Mode-S USB Detector found - " + e.getMessage());
                System.exit(-1);
            }

            send = new SBSSend(jd);
            recv = new SBSReceive(jd, cb);
        } else {
            // Ethernet Interface needed

            try {
                connection = new Socket(config.getDetectorAddress(), config.getDetectorPort());
                bis = new BufferedInputStream(connection.getInputStream());
                bos = new BufferedOutputStream(connection.getOutputStream());
            } catch (Exception e) {
                System.err.println("VRSBS1Feeder::Main No Mode-S network device found - " + e.getMessage());
                System.exit(-1);
            }

            send = new SBSSend(bos);
            recv = new SBSReceive(bis, cb);
        }

        procData = new ProcessSBSData(cb);
        SBSLogin login = new SBSLogin(send, procData);
        SBSDevice dev = login.sbs_login();

        /*
         * Start the network multicast broadcast port
         */

        try {
            ms = new MulticastSocket(config.getMulticastPort());
            ms.setInterface(InetAddress.getByName(config.getMulticastNIC()));
            ms.joinGroup(InetAddress.getByName(config.getMulticastHost()));
            ms.setSoTimeout(800);
            ms.setTimeToLive(3);   // I chose three in case you have a couple routers in your LAN/TUNNEL
        } catch (Exception e) {
            System.err.println("VRSBS1Feeder::Main Fatal: unable to open and set multicast interface: " + e.toString());
            System.exit(-1);
        }

        PacketServer ps = new PacketServer(procData, ms, config, dev);
    }
}
