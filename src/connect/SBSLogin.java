/**
 * SBSLogin.java
 */
package connect;

/**
 * Send the magic keys to the SBS-1 device and login. The green light will come
 * on, and the receive data will flow.
 */
public final class SBSLogin {

    private SBSSend sbs_send;
    private ProcessSBSData process;

    public SBSLogin(SBSSend s, ProcessSBSData p) {
        this.sbs_send = s;
        this.process = p;
    }

    /*
     * Login to start data flow
     */
    public SBSDevice sbs_login() {
        byte[] LOGIN1 = {(byte) 0x01, (byte) 0xE9, (byte) 0x5D, (byte) 0xC5,
            (byte) 0x53, (byte) 0xB1, (byte) 0xD7, (byte) 0xB7, (byte) 0x4E};
        DataBlock db;

        System.out.println("Sending Login String");

        sbs_send.send(0x17, LOGIN1, 0x4857, 0x5958);        // Type 17
        db = process.receive(0x27, 0x4431, 0x474A);         // Type 27
        byte[] rx = db.getUnScrambledData();

        System.out.println("Challenge message received");

        /*
         * The unit sends back the keys above in the first 8 bytes along with
         * two more keys in the second 8 bytes
         */

        int tx_key1 = ((rx[2] << 8) | (rx[1] & 0xFF)) & 0xFFFF;
        int tx_key2 = ((rx[4] << 8) | (rx[3] & 0xFF)) & 0xFFFF;

        int rx_key1 = ((rx[10] << 8) | (rx[9] & 0xFF)) & 0xFFFF;
        int rx_key2 = ((rx[12] << 8) | (rx[11] & 0xFF)) & 0xFFFF;

        System.out.println("Sending sent challenge string back");

        byte[] LOGIN2 = {(byte) 0x02, rx[9], rx[10], rx[11], rx[12],
            rx[13], rx[14], rx[15], rx[16]};

        sbs_send.send(0x18, LOGIN2, tx_key1, tx_key2);    // Type 18
        db = process.receive(0x28, rx_key1, rx_key2);           // Type 28
        rx = db.getUnScrambledData();

        char[] data1 = {(char) rx[1], (char) rx[2], (char) rx[3],
            (char) rx[4], (char) rx[5], (char) rx[6], (char) rx[7], (char) rx[8]};

        char[] data2 = {(char) rx[9], (char) rx[10], (char) rx[11], (char) rx[12],
            (char) rx[13], (char) rx[14], (char) rx[15], (char) rx[16],
            (char) rx[17], (char) rx[18], (char) rx[19], (char) rx[20],
            (char) rx[21], (char) rx[22], (char) rx[23], (char) rx[24]};

        System.out.println("Login complete message received");

        String sn = new String(data1).trim();                                       // 8 Character String
        String fw = new String(data2).trim();                                       // 16 Character String
        String bu = String.format("%d", (rx[26] << 8 | (rx[25] & 0xFF) & 0xFFFF));  // Unsigned Short
        String fp = String.format("%d.%02d", rx[27], rx[28]);                       // 8 bits, 8 bits
        String de = String.format("%d.%02d", rx[29], rx[30]);                       // 8 bits, 8 bits
        String us = String.format("%d.%02d", rx[31], rx[32]);                       // 8 bits, 8 bits

        SBSDevice device = new SBSDevice(sn, fw, bu, fp, de, us, rx_key1, rx_key2);

        return device;
    }
}
