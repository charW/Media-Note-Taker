package medianotetaker;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class LocalMediaNoteTaker extends MediaNoteTaker {

    private BorderPane mainPane, controlBar, toolBox;
    private MediaPlayer player;
    private MediaView view;
    private Slider progressBar, volumeControl;
    private TimeLabel timeLabel;
    private Duration totalDuration;
    private PlayPauseRepeatButton ppr;
    private Boolean hasImportedProgress;

    // constructor: construct a local media note taker with width and height
    public LocalMediaNoteTaker(final int width, final int height) {
        super(width, height);
        mainPane = new BorderPane();
    }

    /* 
    starts playing media from fileSource, and displays spectrum
    visualization if the file is audio only
    requires: fileSource != null and points to a valid local media file
     */
    void startPlaying(final String fileSource, final boolean audioOnly) {
        Media media = new Media(fileSource);

        hasImportedProgress = false;

        player = new MediaPlayer(media);
        view = new MediaView(player);
        view.setFitWidth(width);
        view.setFitHeight(height);
        mainPane.setCenter(view);
        setCenter(mainPane);

        /* let the player play and init/configure all components that're
           associated with an active player */
        player.setOnReady(() -> {
            initConfigureAndAddControlBar();
        });
        player.play();
        player.setOnPlaying(() -> {
            if (audioOnly) {
                addSpectrumVisualization();
            }
            if (!hasImportedProgress && previousProgress != null) {
                importPreviousProgress(previousProgress);
                hasImportedProgress = true;
            }
            setUpdateFromPlayerTime();
        });
        player.setOnEndOfMedia(() -> {
            ppr.setMode(PPRMode.REPEAT);
            ppr.setIcon("repeat.png");
        });
    }

    /* 
    start playing media from fileSource and import previous progresses
    of note taking associated with that file
    
    requires:  -> file from fileSource should have been opened and worked on
                 before in this application
               -> fileSource != null and rpoints to a valid local media file
               -> previousProgress != null     
     */
    protected void startPlaying(final String fileSource,
            final NoteProgress previousProgress) {
        this.previousProgress = previousProgress;
        startPlaying(fileSource, previousProgress.isAudioOnly());
    }

    /* 
    add actions associated with the progressBar
    
    requires: called after initializing the progressBar
     */
    private void makeProgressBarActive() {

        /* the following two actions implemented through anonymous classes
           enable the user to smoothly transition to different parts of the
           video by moving the progressBar */
        progressBar.setOnMousePressed((MouseEvent event) -> {
            player.pause();
        });

        progressBar.setOnMouseReleased((MouseEvent event) -> {
            Duration newTime = new Duration(progressBar.getValue() * 1000);
            if (newTime.greaterThanOrEqualTo(totalDuration)) {
                newTime= new Duration(totalDuration.toSeconds() - 0.05);
            }
            player.seek(newTime);
            if (ppr.getMode().equals(PPRMode.REPEAT)) {
                ppr.setMode(PPRMode.PAUSE);
                ppr.setIcon("pause.png");
                player.play();
            } else if (!ppr.getMode().equals(PPRMode.PLAY)) {
                player.play();
            }
        });

        /* change the positions of existing note markers along the progressBar
           accordingly as the progressBar's length changes */
        progressBar.widthProperty().addListener(
                (ObservableValue<? extends Number> observable,
                        Number oldValue, Number newValue) -> {
                    updateNoteMarkerPosition();
                }
        );
    }

    /* 
    updates the progressBar and the time label as the player keeps playing the
    file
    
    requires: called after setting up the player, progressBar, and timeLabel
     */
    private void setUpdateFromPlayerTime() {
        player.currentTimeProperty().addListener(
                (ObservableValue<? extends Duration> observable,
                        Duration oldValue, Duration newValue) -> {
                    progressBar.adjustValue(newValue.toSeconds());
                    timeLabel.update(newValue);
                }
        );
    }

    // initialize timeLabel and makes it display the current time in white
    private void initAndConfigureTimeLabel() {
        totalDuration = getTotalDuration();
        timeLabel = new TimeLabel(totalDuration);
        timeLabel.setTextFill(Color.WHITE);
        timeLabel.update(player.getCurrentTime());
    }

    /* 
    initialize and add action to volume control
    
    requires: called after the player is ready
     */
    private void initAndConfigureVolumeControl() {
        volumeControl = new Slider();
        volumeControl.setPrefWidth(100);
        volumeControl.setValue(50);
        player.setVolume(volumeControl.getValue() * 0.01);
        volumeControl.valueProperty().addListener((Observable observable) -> {
            player.setVolume(volumeControl.getValue() * 0.01);
        });
    }

    /* 
    initialize progressBar, set its values to correspond to the length of
    the file played, and add to it its associated actions 
    requires: called after the player is ready
     */
    private void initAndConfigureProgressBar() {
        progressBar = new Slider();
        progressBar.setMin(0.0);
        progressBar.setValue(0.0);
        progressBar.setMax(totalDuration.toSeconds());
        makeProgressBarActive();
    }

    /* 
    create controlBar, add to it its components, and make it show or
    or disappear depending on whether the mouse enters the screen,
    and add the controlBar to the bottom of the main pane
    requires: called after initializing the main pane
     */
    private void initConfigureAndAddControlBar() {
        // create the components of the control bar
        initAndConfigureToolBox();
        initAndConfigureProgressBar();

        controlBar = new BorderPane();
        controlBar.setTop(noteMarkingArea);
        controlBar.setCenter(progressBar);
        controlBar.setBottom(toolBox);

        mainPane.setBottom(controlBar);
        mainPane.setOnMouseEntered((MouseEvent event) -> {
            controlBar.setVisible(true);
        });

        mainPane.setOnMouseExited((MouseEvent event) -> {
            if (!isTakingNote) {
                controlBar.setVisible(false);
            }
        });
    }

    /* 
    initialize and add different components (including the play, fast 
    forward, rewind buttons, time label, volume control, progressBar, and the
    make note button) to the tool box 
     */
    private void initAndConfigureToolBox() {
        // CREATE DIFFERENT COMPONENTS OF TOOL BOX-----------------------

        // make the buttons with different icons
        ppr = new PlayPauseRepeatButton("pause.png");
        ppr.setMode(PPRMode.PAUSE);
        Button fastForward = createToolBoxButton("fast_forward.png");
        Button rewind = createToolBoxButton("rewind.png");

        // add actions to buttons
        ppr.setOnAction((ActionEvent e) -> {
            cuePPRAction();
        });

        fastForward.setOnAction((ActionEvent e) -> {
            cueFastForward();
        });

        rewind.setOnAction((ActionEvent e) -> {
            cueRewind();
        });

        initAndConfigureTimeLabel();
        initAndConfigureVolumeControl();

        // ADD ABOVE COMPONENTS TO TOOLBOX-------------------------
        toolBox = new BorderPane();

        HBox box1 = new HBox();
        box1.setStyle("-fx-background-color:BLACK");
        box1.setAlignment(Pos.CENTER_LEFT);
        box1.setSpacing(5);
        box1.getChildren().addAll(rewind, ppr,
                fastForward, volumeControl, timeLabel);

        HBox box2 = new HBox();
        box2.setStyle("-fx-background-color:BLACK");
        box2.setAlignment(Pos.CENTER_RIGHT);
        box2.getChildren().add(makeNote);

        toolBox.setLeft(box1);
        toolBox.setRight(box2);
        setToolBoxLook(toolBox);
    }

    /* 
    configure the effect and style of the toolbox
    
    requires: toolBox != null
     */
    private void setToolBoxLook(BorderPane toolBox) {
        InnerShadow is2 = new InnerShadow(20.0, Color.DARKVIOLET);
        toolBox.setEffect(is2);
        toolBox.setStyle("-fx-background-color:BLACK");
        toolBox.setPrefHeight(height * 0.07);
    }

    /*
    create button with icon sourced from iconSource and with
    black background color 
    
    requires: iconSource != null and is valid name for an image file
    in the project folder
     */
    private Button createToolBoxButton(final String iconSource) {
        Button btn = createButton(iconSource);
        btn.setStyle("-fx-background-color:BLACK");
        return btn;
    }

    /*
    rewind the current media and change the icon of the ppr button
    accordingly
    
    requires: ppr != null
     */
    private void cueRewind() {
        Duration newTime = player.getCurrentTime().divide(1.5);
        if (ppr.getMode() == PPRMode.REPEAT) {
            ppr.setMode(PPRMode.PAUSE);
            player.pause();
            player.seek(newTime);
            player.play();
            ppr.setIcon("pause.png");
        } else {
            player.seek(newTime);
        }
    }

    /*
    fast foward the current media and change the icon of the ppr
    button accordingly
    
    requires: ppr != null
     */
    private void cueFastForward() {
        Duration newTime = player.getCurrentTime().multiply(1.5);
        player.play();
        if (newTime.greaterThanOrEqualTo(totalDuration)) {
            newTime = totalDuration;
            player.seek(newTime);
            ppr.setMode(PPRMode.REPEAT);
            ppr.setIcon("repeat.png");
            return;
        }
        player.seek(newTime);
        ppr.setMode(PPRMode.PAUSE);
        ppr.setIcon("pause.png");
        player.play();
    }

    /*
    either play, pause or repeat the current media depending on the
    current mode of the ppr button; change the icon of ppr accordingly
    
    requires: ppr != null
     */
    private void cuePPRAction() {
        switch (ppr.getMode()) {
            case PLAY:
                ppr.setIcon("pause.png");
                ppr.setMode(PPRMode.PAUSE);
                player.play();
                break;
            case PAUSE:
                ppr.setIcon("play.png");
                ppr.setMode(PPRMode.PLAY);
                player.pause();
                break;
            case REPEAT:
                ppr.setIcon("pause.png");
                ppr.setMode(PPRMode.PAUSE);
                player.pause();
                player.seek(player.getStartTime());
                player.play();
                break;
            default:
                break;
        }
    }

    /*
    add dynamic spectrum visualization for audio media played
    
    requires: current media is audio only
     */
    private void addSpectrumVisualization() {
        HBox bandsBox = new HBox();
        bandsBox.setStyle("-fx-background-color:BLACK");
        bandsBox.setAlignment(Pos.CENTER);
        bandsBox.setSpacing(3);
        bandsBox.setPrefHeight(0.07 * height);

        int numOfBands = (int) (player.getAudioSpectrumNumBands() * 0.4);
        Rectangle[] bands = new Rectangle[numOfBands];
        for (int i = 0; i < numOfBands; ++i) {
            bands[i] = new Rectangle(2, 0.07 * height);
            bands[i].setFill(Color.RED);
            bandsBox.getChildren().add(bands[i]);
        }

        int bandWidth = (int) (width / bands.length);
        for (Rectangle band : bands) {
            band.setWidth(bandWidth);
        }

        player.setAudioSpectrumThreshold(-100);
        player.setAudioSpectrumListener((double timestamp,
                double duration, float[] magnitudes, float[] phases) -> {
            for (int i = 0; i < bands.length; ++i) {
                double m = Math.abs(magnitudes[i]);
                bands[i].setHeight(m);
                decideAudioSpectrumBandColor(bands[i], m);
            }
        });
        mainPane.setCenter(bandsBox);
    }

    /*
    decide the color of the audio spectrum band based on the 
    frequency magnitude m
    
    requires: band != null
     */
    private void decideAudioSpectrumBandColor(
            final Rectangle band, final double m) {
        if (m <= 40) {
            band.setFill(Color.DARKRED);
        } else if (m > 40 && m <= 45) {
            band.setFill(Color.RED);
        } else if (m > 45 && m <= 50) {
            band.setFill(Color.PURPLE);
        } else if (m > 50 && m <= 55) {
            band.setFill(Color.BLUE);
        } else if (m > 55 && m <= 60) {
            band.setFill(Color.PURPLE);
        } else if (m > 60 && m <= 65) {
            band.setFill(Color.DEEPPINK);
        } else if (m > 65 && m < 70) {
            band.setFill(Color.TOMATO);
        } else if (m > 70 && m <= 75) {
            band.setFill(Color.LIGHTCORAL);
        } else if (m > 75 && m < 80) {
            band.setFill(Color.PINK);
        } else {
            band.setFill(Color.BISQUE);
        }
    }

    // implemented methods dictated by the abstract superclass --------------
    /*
    get the appropriate horizontal position along the note marking area
    based on the time in the media associated with that marker
    
    requires: time != null
              0 <= time <= total duration of media
     */
    @Override
    protected double getMarkerAreaXPos(final Duration time) {
        double x = time.toSeconds();
        x /= getTotalDuration().toSeconds();
        x *= getWidth();
        return x;
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
        player.seek(time);
    }

    // get the current time in the media
    @Override
    protected Duration getCurrentTimeInVideo() {
        return player.getCurrentTime();
    }

    // play the media
    @Override
    protected void playVideo() {
        player.play();
        ppr.setMode(PPRMode.PAUSE);
        ppr.setIcon("pause.png");
    }

    // pause the media
    @Override
    protected void pauseVideo() {
        player.pause();
        ppr.setMode(PPRMode.PLAY);
        ppr.setIcon("play.png");
    }

    // return the total duration of the current media
    @Override
    protected Duration getTotalDuration() {
        Duration total = null;
        try {
            total = player.getTotalDuration();
        } catch (Exception e) {
            if (previousProgress != null) {
                total = previousProgress.getTotalDuration();
            }
        }
        return total;
    }

    // stop the media
    @Override
    protected void stopVideo() {
        player.stop();
    }
}
