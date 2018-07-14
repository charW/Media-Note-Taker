package medianotetaker;

import static java.lang.Math.floor;
import javafx.util.Duration;

public class TimeFormatter {

    /* 
    format the argument time in the form of
    hours : minutes : seconds 
    
    requires: time != null
     */
    public static String formatTime(final Duration time) {
        Double timeInMin = time.toMinutes();
        int numOfMin = (int) (floor(timeInMin));
        int numOfHours = 0;
        if (numOfMin >= 60) {
            numOfHours = (int) (timeInMin / 60);
        }
        int numOfSec = (int) ((timeInMin - numOfMin) * 60);

        String formattedTime = "";
        if (numOfHours != 0) {
            formattedTime += formatTimeDigits(numOfHours) + ":";
        }
        formattedTime += formatTimeDigits(numOfMin) + ":";
        formattedTime += formatTimeDigits(numOfSec);
        return formattedTime;
    }

    /* 
    return a string that contains the formatted digits, meant to
    be a subsection in the overall formatted time
    example: 10 -> "10", 8 -> "08", 0 -> "00"
    requires: 0 < digits < 100 (i.e. digit represents a two-digit #)
     */
    public static String formatTimeDigits(final int digits) {
        if (digits < 10) {
            return "0" + digits;
        }
        return "" + digits;
    }
}
