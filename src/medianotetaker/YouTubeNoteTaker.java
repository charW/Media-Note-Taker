package medianotetaker;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class YouTubeNoteTaker extends MediaNoteTaker {

    private BorderPane mainPane;
    private WebView browser;
    private WebEngine webEngine;
    private Duration totalDuration;
    private String videoID;
    private NoteProgress previousProgress;

    // constructor: with dimension width * height
    public YouTubeNoteTaker(final int width, final int height) {
        super(width, height);
        mainPane = new BorderPane();
    }

    /*
    load the video in the web engine with videoID, which should have
    been specified earlier; also if withPreviousProgress is true, import
    previousProgress
    
    requires: --> previousProgress == null if and only if
              withPreviousProgress == false
              --> should call this after videoID has been set and web engine
              has been initialized
     */
    private void loadVideo(final boolean withPreviousProgress,
            final NoteProgress previousProgress) {
        String url = getClass().
                getResource("onlinevideonotetaker.html").toExternalForm();
        webEngine.load(url);

        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends State> observable,
                        State oldValue, State newValue) -> {
                    if (newValue == State.SUCCEEDED) {
                        webEngine.executeScript("setVideoID('" + videoID + "')");
                        if (withPreviousProgress) {
                            importPreviousProgress(previousProgress);
                        }
                    }
                }
        );

    }

    /*
    initialize the browser (WebView) and ensure the note marker positions
    can be updated if the browser's width changes in the future
     */
    private void setupBrowser() {
        browser = new WebView();
        browser.widthProperty().addListener(
                (ObservableValue<? extends Number> observable,
                        Number oldValue, Number newValue) -> {
                    updateNoteMarkerPosition();
                }
        );
    }

    /*
    add the browser and note taking tools to the main pane, and 
    add the main pane to the application
    requires: main pane should have already been initialized
     */
    private void showBrowserAndNoteTakingTools() {
        mainPane.setCenter(browser);
        BorderPane noteTakingTools = assembleNoteTakingTools();
        mainPane.setBottom(noteTakingTools);
        setCenter(mainPane);
    }

    /*
    load the embedded url of YouTube video with videoID
    
    requires: videoID != null and should be a valid YouTube video ID
     */
    void startPlaying(final String videoID) {
        setupBrowser();

        this.videoID = videoID;
        webEngine = browser.getEngine();
        loadVideo(false, null);

        showBrowserAndNoteTakingTools();
    }

    /*
    load the embedded url of YouTube video with videoID and also
    import previousProgress
    
    requires: videoID, previousProgress != null
              videoID should be a valid YouTube video ID
     */
    protected void startPlaying(String videoID, NoteProgress previousProgress) {
        this.previousProgress = previousProgress;
        setupBrowser();

        this.videoID = videoID;
        webEngine = browser.getEngine();
        loadVideo(true, previousProgress);

        showBrowserAndNoteTakingTools();
    }

    /*
    assemble the components necessary for note taking--the 
    note marking area and the make note button
    
    requires: noteMarkingArea and makeNote button are already set before
              calling this
     */
    private BorderPane assembleNoteTakingTools() {
        BorderPane tools = new BorderPane();
        tools.setCenter(noteMarkingArea);
        tools.setTop(makeNote);
        BorderPane.setAlignment(makeNote, Pos.TOP_RIGHT);
        return tools;
    }

    /*
    return the video ID
     */
    String getVideoID() {
        return videoID;
    }

    //get the current time in the media
    @Override
    protected Duration getCurrentTimeInVideo() {
        double seconds = (double) webEngine.executeScript("getCurrentTime()");
        return new Duration(seconds * 1000);
    }

    /*
    get the appropriate horizontal position along the note marking area
    based on the time in the media associated with that marker
    
    requires: time != null
              0 <= time <= total duration of media
     */
    @Override
    protected double getMarkerAreaXPos(final Duration time) {
        double seconds = time.toSeconds();
        if (totalDuration == null) {
            totalDuration = getTotalDuration();
        }
        return seconds / totalDuration.toSeconds() * getWidth() + 2;
    }

    /*
    go to the specified time in the media; if the media was paused before
    this's called, the media will also be paused at the new time, and if
    the media was playing before, the media will also be playing at the
    new time
    
    requires: time != null
              0 <= time <= total duration of media
     */
    @Override
    protected void seekInVideo(final Duration time) {
        double seconds = time.toSeconds();
        webEngine.executeScript("seekInVideo(" + seconds + ")");
    }

    // pause the media
    @Override
    protected void pauseVideo() {
        webEngine.executeScript("pauseVideo()");
    }

    // play the media
    @Override
    protected void playVideo() {
        webEngine.executeScript("playVideo()");
    }

    // get the total duration of the current media
    @Override
    protected Duration getTotalDuration() {
        Duration total = null;
        try {
            double seconds = (double) (webEngine.executeScript("getTotalDuration()"));
            total = new Duration(seconds * 1000);
        } catch (Exception e) {
            if (previousProgress != null)
            total = previousProgress.getTotalDuration();
        }
        return total;
    }
    
    // stop the media
    @Override
    protected void stopVideo() {
        webEngine.executeScript("stopVideo()");
    }
}
