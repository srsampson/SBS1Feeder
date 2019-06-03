/**
 * Config.java
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
package feeder;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Configuration class
 */
public final class Config {

    private final static String MULTICAST_ADDRESS = "239.192.10.90";    // I made it up (has 1090 in it)
    private final static int MULTICAST_PORT = 31090;                    // I made it up (has 1090 in it)
    //
    private Properties Props;
    private String OSConfPath;
    private String userDir;
    private String fileSeparator;
    private FileInputStream in;
    //
    private String sbsinput;
    private String sbsaddress;
    private String multicastIP;
    private int multicastPort;
    private String multicastNIC;
    private int sbsport;
    private int radarID;

    Config(String conf) {
        sbsinput = "";
        sbsaddress = "192.168.1.170";
        sbsport = 10001;
        multicastIP = MULTICAST_ADDRESS;
        multicastPort = MULTICAST_PORT;
        multicastNIC = "127.0.0.1";
        //
        userDir = System.getProperty("user.dir");
        fileSeparator = System.getProperty("file.separator");
        Props = new Properties();
        OSConfPath = userDir + fileSeparator + conf;
        radarID = 0;

        initProperties(OSConfPath);
    }

    public void initProperties(String filename) {
        String temp;

        try {
            in = new FileInputStream(filename);
            Props.load(in);
        } catch (Exception e1) {
            System.err.println("Config::getProperties exception Loading Properties " + e1.toString());
        }

        temp = Props.getProperty("radar.id");
        if (temp == null) {
            radarID = 0;
            System.out.println("radar.id not specified, using radar ID 0");
        } else {
            try {
                radarID = Integer.parseInt(temp.trim());
            } catch (NumberFormatException e) {
                radarID = 0;
            }
        }

        temp = Props.getProperty("multicast.nicaddress");
        if (temp == null) {
            multicastNIC = "127.0.0.1";
            System.out.println("multicast NIC not specified, using Loopback 127.0.0.1");
        } else {
            multicastNIC = temp.trim();
        }

        temp = Props.getProperty("multicast.address");
        if (temp == null) {
            multicastIP = MULTICAST_ADDRESS;
            System.out.println("socket address not set, set to " + MULTICAST_ADDRESS);
        } else {
            multicastIP = temp.trim();
        }

        temp = Props.getProperty("multicast.port");
        if (temp == null) {
            multicastPort = MULTICAST_PORT;
            System.out.println("socket port not set, set to " + String.valueOf(multicastPort));
        } else {
            try {
                multicastPort = Integer.parseInt(temp.trim());
            } catch (NumberFormatException e) {
                multicastPort = MULTICAST_PORT;
            }
        }

        temp = Props.getProperty("sbs.input");
        if (temp == null) {
            sbsinput = "B";
            System.out.println("sbs.input not set, set to B");
        } else {
            sbsinput = temp.trim();
        }

        temp = Props.getProperty("sbs.address");
        if (temp == null) {
            sbsaddress = "192.168.1.170";
            System.out.println("sbs.address not set, set to 192.168.1.170");
        } else {
            sbsaddress = temp.trim();
        }

        temp = Props.getProperty("sbs.port");
        if (temp == null) {
            sbsport = 10001;
            System.out.println("sbs.port not set, set to 10001");
        } else {
            try {
                sbsport = Integer.parseInt(temp.trim());
            } catch (NumberFormatException e) {
                sbsport = 10001;
            }
        }
    }

    public Properties getProperties() {
        return Props;
    }

    public int getRadarID() {
        return radarID;
    }

    /**
     * Method to return the Mode-S input device
     *
     * @return a string Representing the Mode-S input device
     */
    public String getDetector() {
        return sbsinput;
    }

    /**
     * Method to return the SBS network port
     *
     * @return a string Representing the SBS network port
     */
    public int getDetectorPort() {
        return sbsport;
    }

    public String getMulticastNIC() {
        return multicastNIC;
    }

    /**
     * Method to return the configuration multicast UDP port
     *
     * @return an integer Representing the multicast UDP port to transmit on
     */
    public int getMulticastPort() {
        return multicastPort;
    }

    /**
     * Method to return the configuration multicast host IP
     *
     * @return a string Representing the multicast host IP
     */
    public String getMulticastHost() {
        return multicastIP;
    }

    /**
     * Method to return the Mode-S detector network address
     *
     * @return a string Representing the Mode-S network address
     */
    public String getDetectorAddress() {
        return sbsaddress;
    }

    public String getOSConfPath() {
        return OSConfPath;
    }
}
