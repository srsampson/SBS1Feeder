/**
 * ZuluMillis.java
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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Algorithm to give us Zulu Time.
 */
public class ZuluMillis {

    private final Calendar cal;

    public ZuluMillis() {
        this.cal = new GregorianCalendar();
    }

    /**
     * Method to return the current UTC time in milliseconds
     *
     * @return a long Representing the time in UTC milliseconds
     */
    public long getUTCTime() {
        cal.setTimeInMillis(System.currentTimeMillis());

        return cal.getTimeInMillis()
                - cal.get(Calendar.ZONE_OFFSET)
                - cal.get(Calendar.DST_OFFSET);
    }
}
