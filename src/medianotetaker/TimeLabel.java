package medianotetaker;

import javafx.scene.control.Label;
import javafx.util.Duration;

public class TimeLabel extends Label {

    boolean isOverallLabel;
    String totalTime;

    // constructor
    TimeLabel(final Duration totalTime) {
        this.totalTime = TimeFormatter.formatTime(totalTime);
        isOverallLabel = true;
    }

    /* 
    return string that contains the formatted time and also
    update the text of the time label (the implicit object) with currentTime
    
    requires: currentTime != null
     */
    void update(final Duration currentTime) {
        if (isOverallLabel) {
            setText(TimeFormatter.formatTime(currentTime) + " / " + totalTime);
        } else {
            setText(TimeFormatter.formatTime(currentTime));
        }
    }

    /* 
    returns true if the time label is meant to be "overall"--i.e. the one
    displayed in the control bar-- or if the time label is meant to be sectional
    --i.e. one that only shows to display the time of a part of the slider or
    a note marker where the user moves the mouse onto
     */
    void setOverallTimeLabel(final boolean b) {
        isOverallLabel = b;
    }
}
