package medianotetaker;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.util.Duration;


/*
this's the note progress that will saved in a .DAT file, which
the user can open if he/she wishes to continue the work from last time
*/

public class NoteProgress implements Serializable {

    private ArrayList<NoteMarkerInfo> infoList;
    private boolean forLocalMedia, audioOnly;
    private String mediaSource, mediaName;
    private Duration totalDuration;

    // constructor
    public NoteProgress() {
        infoList = null;
        forLocalMedia = false;
        mediaSource = "";
    }

    /*
    constructor: creates the progress with infoList, mediaSource,
    forLocalMedia, and totalDuration; this's so that when the user stores the
    progress and opens it again at a later time, the correct media can
    be automatically played frmo the mediaSource with all previous note markers
    displayed in their proper positions along the note marking area
    */
    public NoteProgress(final ArrayList<NoteMarkerInfo> infoList,
            final boolean forLocalMedia, final boolean audioOnly,
            final String mediaSource, final String mediaName,
            final Duration totalDuration) {
        this.infoList = infoList;
        this.forLocalMedia = forLocalMedia;
        this.audioOnly = audioOnly;
        this.mediaSource = mediaSource;
        this.mediaName = mediaName;
        this.totalDuration = totalDuration;
    }

    // return the list of note marker info of the progress
    ArrayList<NoteMarkerInfo> getInfoList() {
        return infoList;
    }

    // return the media source for which the progress was made
    String getMediaSource() {
        return mediaSource;
    }
    
    // return the media name
    String getMediaName(){ 
        return mediaName;
    }
    
    // return whetehr the media source was from local or from YouTube
    boolean isForLocalMedia() {
        return forLocalMedia;
    }
    
    boolean isAudioOnly() {
        return audioOnly;
    }

    // return the total duraiton of the media
    Duration getTotalDuration() {
        return totalDuration;
    }
}
