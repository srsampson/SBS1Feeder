/**
 * CRC16.java
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
