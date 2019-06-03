/**
 * ZuluMillis.java
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
