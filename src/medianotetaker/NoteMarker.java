package medianotetaker;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NoteMarker extends Rectangle {

    NoteMarkerInfo info;
    private Stage noteStage;
    private Rectangle section;
    private boolean isSet;
    static final Color DEFAULT_MARKER_COLOR = Color.ORANGE;

    /*
    constructs a new oranged note marker with dimension width * height
     */
    public NoteMarker(final double width, final double height) {
        super(width, height);
        setFill(DEFAULT_MARKER_COLOR);
        setOpacity(0.7);
    }

    /*
    constructs a new orange note marker with width and height that is
    associated with a specified time (in the media)
     */
    public NoteMarker(final double width, final double height,
            final Duration time) {
        this(width, height);
        info = new NoteMarkerInfo(time);
        isSet = false;
    }

    /*
    constructs a new orange note marker with width and height that contains
    info, most likely from a previous session
     */
    public NoteMarker(final double width, final double height,
            NoteMarkerInfo info) {
        this(width, height);
        this.info = info;
        isSet = true;
    }

    /*
    set the note stage of the note marker
     */
    void setNoteStage(final Stage noteStage) {
        this.noteStage = noteStage;
    }

    /*
    return the note stage of the note marker
     */
    Stage getNoteStage() {
        return noteStage;
    }

    /*
    set the rectangle representation of the section in the media
    associated with this note marker
    requires: section != null
     */
    void setSection(final Rectangle section) {
        this.section = section;
    }

    /*
    return the rectangle representation of the section in the media
    associated with this note marker
     */
    Rectangle getSection() {
        return section;
    }

    /*
    return true if the marker has been set at least once before (i.e.
    the user has successfully added it through the note stage) and false
    if otherwise
     */
    Boolean hasBeenSet() {
        return isSet;
    }

    /*
    set if the marker has been set at least once before (i.e.
    the user has successfully added it through the note stage)
     */
    void setReady(final boolean b) {
        isSet = b;
    }
}
