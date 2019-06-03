/**
 * CRC16.java
 */
package connect;

public final class CRC16 {

    private int value;
    private int poly;

    public CRC16() {
        this.value = this.poly = 0;
    }

    public void reset(int init, int p) {
        this.value = init & 0xFFFF;
        this.poly = p & 0xFFFF;
    }

    public int getMSB() {
        return (this.value >>> 8) & 0xFF;
    }

    public int getLSB() {
        return this.value & 0xFF;
    }

    public int getCRC() {
        return this.value & 0xFFFF;
    }

    /**
     * Method to process the current 8-bit value against the
     * polynomial and previous 16-bit values.
     *
     * <p>It leaves the result in this.value
     *
     * @param v an 8-bit unsigned integer value
     */
    public void compute(int v) {
        int val = v & 0xFF;
        int var;

        for (int i = 0; i < 8; i++) {
            var = ((val << 8) ^ this.value) & 0xFFFF;
            val <<= 1;
            this.value <<= 1;

            if ((var & 0x8000) != 0) {
                this.value ^= this.poly;
            }
        }
    }
}
