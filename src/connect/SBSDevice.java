/**
 * SBSDevice.java
 */
package connect;

/**
 * An object to hold the returned firmware information
 *
 * <p>This data is returned after you connect with the device
 */
public final class SBSDevice {

    private String serial_number;   // Serial Number 8 Character String
    private String firmware;        // Firmware Version name 16 Character String
    private String build;           // Build Number Unsigned Short
    private String fpga;            // FPGA Build Number 16 bits
    private String decoder;         // Decoder Version 16 bits
    private String userfw;          // User Version 16 bits
    private int key1;               // Secret key1 returned by firmware 16 bits
    private int key2;               // Secret key2 returned by firmware 16 bits

    public SBSDevice(String dev, String fw, String b, String fp, String dec, String us, int k1, int k2) {
        this.serial_number = dev;
        this.firmware = fw;
        this.build = b;
        this.fpga = fp;
        this.decoder = dec;
        this.userfw = us;
        this.key1 = k1;
        this.key2 = k2;
    }

    public String getSN() {
        return this.serial_number;
    }

    public String getFW() {
        return this.firmware;
    }

    public String getBUILD() {
        return this.build;
    }

    public String getFPGA() {
        return this.fpga;
    }

    public String getDEC() {
        return this.decoder;
    }

    public String getUSER() {
        return this.userfw;
    }

    public int getKey1() {
        return this.key1 & 0xFFFF;
    }

    public int getKey2() {
        return this.key2 & 0xFFFF;
    }
}
