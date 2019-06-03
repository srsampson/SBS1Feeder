/**
 * DataBlock.java
 */
package connect;

import java.sql.Timestamp;

/**
 * The DataBlock structure is a formatted representation of the raw target data
 * detected by the receiver.
 */
public final class DataBlock {

    private Timestamp sqlTime;
    private long timestamp;
    private String counter;
    private int msgType;
    private int rx_cksum;
    private int calc_cksum;
    private byte[] sdata;           // scrambled packet
    private byte[] udata;           // unscrambled packet

    public DataBlock() {
        this.sqlTime = new Timestamp(0L);
        this.timestamp = 0L;
        this.msgType = 0;
        this.rx_cksum = 0;
        this.calc_cksum = 0;
        this.sdata = null;
        this.udata = null;
        this.counter = "";
    }

    public DataBlock(long t, int m, int c, byte[] a, int n) {
        this.sqlTime = new Timestamp(0L);
        this.timestamp = t;
        this.msgType = m;
        this.rx_cksum = c & 0xFFFF;
        this.calc_cksum = 0;
        this.sdata = new byte[n];
        this.udata = null;
        this.counter = "";

        System.arraycopy(a, 0, sdata, 0, n);    // copy to scrambled packet
    }

    public byte[] getUnScrambledData() {
        return this.udata;
    }

    public byte[] getScrambledData() {
        return this.sdata;
    }

    public void setScrambledData(byte[] a) {
        sdata = new byte[a.length];
        System.arraycopy(a, 0, sdata, 0, a.length);    // copy to scrambled packet
    }

    /**
     * Method to load the unscrambled data into the class
     * after decoding with the bit shifter/crc routine.
     *
     * <p>This method is called after the "type" is decoded.
     *
     * @param a is a byte array containing a decoded Mode-S or ADS-B packet
     */
    public void setUnScrambledData(byte[] a) {
        /*
         * The counter is only relevant on data packets
         */

        udata = new byte[a.length];
        System.arraycopy(a, 0, udata, 0, a.length);     // copy to unscrambled packet

        if (this.msgType == 5 || this.msgType == 7 || this.msgType == 1) {
            this.counter = String.format("%02X%02X%02X%02X", a[0], a[3], a[2], a[1]);
        }
    }

    public void removeCounter() {
        if (this.msgType == 5 || this.msgType == 7 || this.msgType == 1) {
            byte[] tmp = new byte[udata.length - 4];
            System.arraycopy(udata, 4, tmp, 0, udata.length - 4);
            udata = tmp;
            tmp = null;
        }
    }

    public int getType() {
        return this.msgType;
    }

    public int getRXChecksum() {
        return this.rx_cksum;
    }

    public int getCalcChecksum() {
        return this.calc_cksum;
    }

    public void setCalcChecksum(int val) {
        this.calc_cksum = val;
    }

    /**
     * Return the 48 bit MLAT counter
     *
     * @return a String representing the 48-bit MLAT counter
     */
    public String getCounter() {
        return "0000" + this.counter;
    }

    public long getTime() {
        return this.timestamp;
    }

    public String getTimestamp() {
        sqlTime.setTime(this.timestamp);
        return sqlTime.toString();
    }
}
