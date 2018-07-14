package medianotetaker;

import java.io.Serializable;
import javafx.util.Duration;
import static medianotetaker.TimeFormatter.formatTime;


/*
The note marker info is the info that associates
with and stores within each note marker.
The note marker info objects are the ones that will be
saved as progresses; their note marker container class are
created for them to aid the visual display of the info
 */
public class NoteMarkerInfo implements Serializable {

    private Duration startTime, endTime;
    private boolean byItself;
    private String type; // indicates the user's comfort level with the material
    private String topic, note;

    // constructs a note marker info for a given time in the media
    // requires: time != null
    //           0 <= time <= total duration
    public NoteMarkerInfo(final Duration time) {
        startTime = time;
    }
    
    // set the note stored in the note info
    void setNote(final String note) {
        this.note = note;
    }
    
    // get the note stored in note info
    String getNote() {
        return note;
    }

    // set the topic of the note
    // requires: topic != null
    void setTopic(final String topic) {
        this.topic = topic;
    }

    // return the topic of the note stored in the note info
    String getTopic() {
        return topic;
    }

    /*
    return the starting time of the section/moment of the media the
    note info is for
    */
    Duration getStartTime() {
        return startTime;
    }

    /*
    set the starting time of the section/moment of the media the
    note info is for
    */
    void setStartTime(final Duration time) {
        startTime = time;
    }

    /*
    return the end time of the section of the media the note info is
    for; is the note info is not associated with a section but a single
    moment of the media--i.e. byItSelf == true --then the end time
    wouldn't be set, and the method shouldn't be called
    */
    Duration getEndTime() {
        return endTime;
    }

    /*
    return the end time of the section of the media the note info is
    for; is the note info is not associated with a section but a single
    moment of the media--i.e. byItSelf == true --then the method shouldn't
    be called
    */
    void setEndTime(final Duration time) {
        endTime = time;
    }

    
    /*
    set true if the note marker is associated with a 
    a single moment in the media, and false if it's associated 
    with a section in the media
    */
    void setByItself(final boolean b) {
        byItself = b;
    }

    /*
    returns true if the note marker is associated with a 
    a single moment in the media, and false if it's associated 
    with a section in the media
    */
    Boolean isByItself() {
        return byItself;
    }

    // set the type of the note info to be type
    // requires: type != null
    void setType(final String type) {
        this.type = type;
    }
    
    // return the type of the note info
    String getType() {
        return type;
    }

    /*
     format how each info is printed (make it readble when users
     extract and save notes separately in a text file)
     */
    @Override
    public String toString() {
        String noteToPrint = formatTime(startTime);
        if (!byItself) {
            noteToPrint += " - " + formatTime(endTime);
        }
        noteToPrint += "\n";
        noteToPrint += "Topic: " + topic + "\n";
        noteToPrint += "Type: " + type + "\n";
        noteToPrint += this.note + "\n";
        noteToPrint += "\n\n";
        return noteToPrint;
    }
}
