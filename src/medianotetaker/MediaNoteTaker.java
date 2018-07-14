package medianotetaker;

import java.util.ArrayList;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;


public abstract class MediaNoteTaker extends BorderPane {

    final int width, height;
    ArrayList<NoteMarker> noteMarkers;
    Button makeNote;
    HBox noteMarkingArea;
    boolean isTakingNote, hasImportedProgress;
    NoteProgress previousProgress;
    VBox navItemContainer;

    final static String RED_TYPE = "I barely understood anything!";
    final static String DARKVIOLET_TYPE = "I understood some of it.";
    final static String BLUE_TYPE = "I understood the majority of it.";
    final static String GREEN_TYPE = "I noticed something extra...";

    
    // constructor: create media note taker with width and height
    public MediaNoteTaker(final int width, final int height) {
        this.width = width;
        this.height = height;

        setStyle("-fx-background-color: Black");

        noteMarkers = new ArrayList();
        isTakingNote = false;
        hasImportedProgress = false;
        setNoteMarkingArea();
        setMakeNote();
    }

    // initialize, stylize, and add action to the make note button
    private void setMakeNote() {
        makeNote = createButton("add_note.png");
        makeNote.setStyle("-fx-background-color:BLACK");
        makeNote.setOnAction((ActionEvent e) -> {
            cueNoteMaking();
        });
    }

    /* 
    initialize and configure the appearance--effect, style, and preferred
    size --of the note marking area
     */
    private void setNoteMarkingArea() {
        noteMarkingArea = new HBox();
        //noteMarkingArea.setPrefSize(width, 0.07 * height);
        noteMarkingArea.setPrefHeight(0.07 * height);
        InnerShadow is = new InnerShadow(15.0, Color.BROWN);
        noteMarkingArea.setEffect(is);
        noteMarkingArea.setStyle("-fx-background-color:IVORY");
        noteMarkingArea.setVisible(true);
    }

    /*
    enable the user to enter the note (a process during which the user
    enters in noteStage the type, topic, and body of the note they
    want to associate with the note marker they're adding) so the note can be
    stored and displayed in a note marker
    */
    private void beginNoteTaking() {
        Duration currentTime = getCurrentTimeInVideo();
        NoteMarker noteMarker = new NoteMarker(5, height * 0.07, currentTime);
        makeNoteMarkerActive(noteMarker);
        noteMarkers.add(noteMarker);   
        placeMarkerInMarkingArea(noteMarker);
        Stage noteStage = createNoteStage(noteMarker);
        noteStage.show();
    }

    /*
    make and return the combox box in the note pane, through which user
    selects the type of the note he/she is adding
    requires: marker != null
    */
    private ComboBox createNotePaneComboBox(final NoteMarker marker) {
        ObservableList<String> types = FXCollections.observableArrayList(
                RED_TYPE, DARKVIOLET_TYPE, BLUE_TYPE, GREEN_TYPE);
        ComboBox typeSelect = new ComboBox(types);
        typeSelect.setPrefWidth(400);
        typeSelect.setPromptText("Choose the type of your marker");

        typeSelect.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item);
                            setTextFill(decideMarkerColor(item));
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        if (marker.hasBeenSet()) {
            typeSelect.setValue(marker.info.getType());
        }

        return typeSelect;
    }

    /*
    take the information the user entered in noteStage--the note's type,
    topic, the note itself, and associated moment (if the marker is by itself)
    or section in media--and stores that in marker, and then finalizes the 
    display of marker in the note marking area according to that information
    
    requires: marker, noteStage != null
    */
    private void processNoteInformation(final NoteMarker marker, 
            final Stage noteStage, final String type, String topic, 
            final String note, final boolean markerByItself) {
        if (type == null) {
            alertNoTypeSelected();
            return;
        }
        marker.info.setType(type);
        
        if (topic != null) {
            topic = topic.trim();
        }
        if (topic == null || topic.equals("")) {
            alertNoTopic();
            return;
        }
        marker.info.setTopic(topic);
        marker.info.setNote(note);
        marker.info.setByItself(markerByItself);
        Color markerColor = decideMarkerColor(type);
        marker.setFill(markerColor);

        if (marker.hasBeenSet()) {
             noteMarkingArea.getChildren().remove(marker.getSection());
        }
        
        if (!marker.info.isByItself()) {
            marker.info.setEndTime(getCurrentTimeInVideo());
            setAndDisplayMarkerSection(marker, markerColor);
        }

        marker.setNoteStage(noteStage);
        marker.setReady(true);
        isTakingNote = false;
        noteStage.hide();
    }
    
    /*
    make checkPartOfSection true by default and make sure it and checkByItself
    cannot be both checked
    
    requires: checkByItself, checkPartOfSection != null
    */
    private void configureNotePaneCheckBoxes(final CheckBox checkByItself,
            final CheckBox checkPartOfSection) {
        checkByItself.setText("By itself");
        checkByItself.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) -> {
                    checkPartOfSection.setSelected(!newValue);
                }
        );

        checkPartOfSection.setText("Start of a section");
        checkPartOfSection.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) -> {
                    checkByItself.setSelected(!newValue);
                }
        );

        checkPartOfSection.setSelected(true);
        
    }
    
    /* 
    create a pane and add the checkboxes and the enterNote button to it,
    and then setting their layouts; eventually return the pane
    
    requires: the checkboxses and the button != null
    */
    private GridPane createNotePaneBottom(final CheckBox checkByItself,
            final CheckBox checkPartOfSection, final Button enterNote) {
        GridPane bottomPane = new GridPane();
        bottomPane.setHgap(20.0);
        bottomPane.setPadding(new Insets(10.0));

        bottomPane.setAlignment(Pos.BOTTOM_RIGHT);
        bottomPane.add(checkByItself, 1, 0);
        bottomPane.add(checkPartOfSection, 2, 0);
        GridPane.setHalignment(checkByItself, HPos.RIGHT);
        GridPane.setHalignment(checkPartOfSection, HPos.LEFT);
        bottomPane.add(enterNote, 3, 3);
        return bottomPane;
    }
    
    /*
    create a pane and add noteArea and topicField to it, and then setting their
    layouts; eventually return the pane
    
    requires: noteArea, topicField != null
    */
    private BorderPane createNoteTextEnterPane(final TextField topicField,
            final TextArea noteArea) {
        ScrollPane noteTextPane = new ScrollPane();
        noteTextPane.setContent(noteArea);
        noteTextPane.setFitToHeight(true);
        noteTextPane.setFitToWidth(true);

        BorderPane noteTextEnterPane = new BorderPane();
        noteTextEnterPane.setTop(topicField);
        noteTextEnterPane.setCenter(noteArea);
        BorderPane.setMargin(topicField, new Insets(20, 10, 10, 10));
        
        return noteTextEnterPane;
    }

    /*
    make and return the note pane in which the user enters the relevant
    info of the marker they're adding--i.e. the topic, type, body of the
    note, and whether that marker is by itself (i.e. associated with one
    single moment of the media), or the start of a section
    
    requires: noteStage, marker != null
    */
    private BorderPane createNotePane(final Stage noteStage, 
            final NoteMarker marker) {
        // Top component ------------------------------------------------
        ComboBox typeSelect = createNotePaneComboBox(marker);
        
        // Middle component --------------------------------------------
        TextField topicField = new TextField();
        if (marker.hasBeenSet()) {
            topicField.setText(marker.info.getTopic());
        } else {
            topicField.setPromptText("The topic of your note");
        }

        TextArea noteArea = new TextArea();
        if (marker.hasBeenSet() && marker.info.getNote() != null) {
            noteArea.setText(marker.info.getNote());
        } else {
            noteArea.setPromptText("Please enter your note (optional)");
        }
        noteArea.setWrapText(true);
        
        BorderPane noteTextEnterPane = createNoteTextEnterPane(topicField, 
                noteArea);
        
        // Bottom component --------------------------------------------
         // set the check boxes
        CheckBox checkByItself = new CheckBox();
        CheckBox checkPartOfSection = new CheckBox();
        configureNotePaneCheckBoxes(checkByItself, checkPartOfSection);
        if (marker.hasBeenSet() && marker.info.isByItself()) {
            checkByItself.setSelected(true);
        }

        /* initialize and add action to the button to finish adding the note
            marker */
        Button enterNote = createButton("add.png");
        enterNote.setOnAction((ActionEvent e) -> {
            processNoteInformation(marker, noteStage, 
                    (String)typeSelect.getValue(), topicField.getText(), 
                    noteArea.getText(), checkByItself.isSelected());
        });
        
        GridPane bottomPane = createNotePaneBottom(checkByItself,
                checkPartOfSection, enterNote);

        // init the note pane and add all components to it -------------
        BorderPane notePane = new BorderPane();
        notePane.setTop(typeSelect);
        notePane.setCenter(noteTextEnterPane);
        notePane.setBottom(bottomPane);

        return notePane;
    }
    
    
    /*
    creates the note stage for the user to enter in the info for the marker
    that he/she is currently adding
    
    requires: marker != null
    */
    protected Stage createNoteStage(final NoteMarker marker) {
        Stage noteStage = new Stage();
        noteStage.setTitle("Adding a marker");
        Pane notePane = createNotePane(noteStage, marker);
        noteStage.setScene(new Scene(notePane, 400, 430));
        noteStage.setResizable(false);
        noteStage.setOnCloseRequest((WindowEvent event) -> {
            isTakingNote = false;
            if (!marker.hasBeenSet()) {
                noteMarkingArea.getChildren().remove(marker);
            }
        });
        return noteStage;
    }

    /*
    place marker in its appropriate horizontal position along
    the note marking area; the position depends on the marker's starting time
    
    requires: marker != null
    */
    private void placeMarkerInMarkingArea(final NoteMarker marker) {
        // put the marker in the right position
        marker.setManaged(false);
        double xPos = getMarkerAreaXPos(marker.info.getStartTime());
        marker.setX(xPos);
        noteMarkingArea.getChildren().add(marker);
    }

    /* 
    when a note marker is clicked, display the note if it's not
    null or empty; otherwise the marker is irresponsive 
    
    requires: noteMarker != null
    */
    private void makeNoteMarkerActive(final NoteMarker noteMarker) {
        noteMarker.setOnMouseClicked((MouseEvent event) -> {
            if (!noteMarker.hasBeenSet()) {
                return;
            }
            isTakingNote = true;
            Duration markedTime = noteMarker.info.getStartTime();
            seekInVideo(markedTime);
            playVideo();

            noteMarker.getNoteStage().show();
        });
    }

    /*
    alert the user that he/she hasn't finished adding the previous note
    when he/she tries to add a new one
    */
    private void alertIsTakingNote() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("You are in the middle of adding another mark!");
        alert.setContentText("Please finish that one first.");
        alert.showAndWait();
    }

    /*
    alert the user to finish that note if he/she is currently adding one,
    otherwise enable the user to add a note
    */
    private void cueNoteMaking() {
        if (isTakingNote) {
            alertIsTakingNote();
            return;
        }
        isTakingNote = true;
        beginNoteTaking();
    }


    // alerts the user to select the note type before finish adding the note
    private void alertNoTypeSelected() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("You haven't chosen a type yet!");
        alert.setContentText("Please choose a type for this marker.");
        alert.showAndWait();
    }

    // alert the user to enter a note topic before finish adding the note
    private void alertNoTopic() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("You haven't entered a topic yet!");
        alert.setContentText("To better organize your markers and notes"
                + ", please enter a topic.");
        alert.showAndWait();
    }

    /*
    return the color of the marker based on the markertype
    requires: markerType != null and is one of 
             {RED_TYPE, DARKVIOLET_TYPE, BLUE_TYPE, GREEN_TYPE}
    */
    private Color decideMarkerColor(String markerType) {
        switch (markerType) {
            case RED_TYPE:
                return Color.RED;
            case DARKVIOLET_TYPE:
                return Color.DARKVIOLET;
            case BLUE_TYPE:
                return Color.BLUE;
            case GREEN_TYPE:
                return Color.GREEN;
            default:
                return Color.BLACK;
        }
    }

    /*
    init, stylize, and add the section of a marker to the note marking area
    
    requires: note marking area != null
              marker != null
              markerColor != null and is one of {red, green, dark violet, blue}
    */
    private void setAndDisplayMarkerSection(final NoteMarker marker, 
            final Color markerColor) {
        double startX = getMarkerAreaXPos(marker.info.getStartTime());
        double endX = getMarkerAreaXPos(marker.info.getEndTime());
        Rectangle section = new Rectangle(endX - startX - 5, height * 0.06);
        section.setOpacity(0.7);
        section.setFill(pickSectionColor(markerColor));
        noteMarkingArea.getChildren().add(section);
        section.setManaged(false);
        section.setX(startX + 5);
        section.setOnMouseClicked((MouseEvent event) -> {
            Duration markedTime = marker.info.getStartTime();
            seekInVideo(markedTime);
            playVideo();
            marker.getNoteStage().show();
        });

        marker.setSection(section);
    }
    
    /*
    pick the marker's section color based on the marker's color
    
    requires:  markerColor != null and is one of
               {RED, DARKVIOLET, BLUE, GREEN}
    */
    protected Color pickSectionColor(final Color markerColor) {
        if (markerColor == Color.RED) {
            return Color.SALMON;
        } else if (markerColor == Color.DARKVIOLET) {
            return Color.PLUM;
        } else if (markerColor == Color.BLUE) {
            return Color.DEEPSKYBLUE;
        } else {
            return Color.LIGHTGREEN;
        }
    }

    /*
    return the note markers already added for the current media
    */
    protected ArrayList<NoteMarker> getNoteMarkers() {
        return noteMarkers;
    }

    /*
    create a button with the icon from iconSource
    
    requires: iconSource != null and represents a valid name for an
              image file in the project folder    
    */
    protected final Button createButton(final String iconSource) {
        Button btn = new Button();
        Image icon = new Image(getClass().getResourceAsStream(iconSource));
        ImageView iconView = new ImageView(icon);
        btn.setGraphic(iconView);
        return btn;
    }

    /*
    update the horizontal position of each marker along the note
    marking area; this's usually called after the dimension of the
    application has changed
    */
    protected void updateNoteMarkerPosition() {
        for (NoteMarker marker : noteMarkers) {
            noteMarkingArea.getChildren().remove(marker.getSection());
            double startX = getMarkerAreaXPos(marker.info.getStartTime());
            marker.setX(startX);
            if (marker.hasBeenSet() && !marker.info.isByItself()) {
                double endX = getMarkerAreaXPos(marker.info.getEndTime());
                Rectangle section = marker.getSection();
                noteMarkingArea.getChildren().remove(section);
                Color markerColor = decideMarkerColor(marker.info.getType());
                section.setFill(pickSectionColor(markerColor));
                section.setX(startX + 5.0);
                noteMarkingArea.getChildren().add(section);
                section.setWidth(endX - startX + 5.0);
            }
        }
    }

    /*
    add all the notes taken previously (on the same media), whose info
    is stored in progress, to the current session of the application
    
    requires: progress != null
    */
    protected void importPreviousProgress(final NoteProgress progress) {
        previousProgress = progress;
        importExistingMarkerInfo(progress.getInfoList());
    }

    /*
    wrap each info in infoList in a marker and display it (and its section,
    if it's associated with a section in the media) in its proper position
    along the note marking area
    
    requires: infoList != null
    */
    private void importExistingMarkerInfo(
            final ArrayList<NoteMarkerInfo> infoList) {
        for (NoteMarkerInfo info : infoList) {
            NoteMarker marker = new NoteMarker(5, height * 0.07, info);
            makeNoteMarkerActive(marker);
            noteMarkers.add(marker);
            marker.setNoteStage(createNoteStage(marker));
            placeMarkerInMarkingArea(marker);
            Color markerColor = decideMarkerColor(marker.info.getType());
            marker.setFill(markerColor);
            if (!marker.info.isByItself()) {
                setAndDisplayMarkerSection(marker, markerColor);
            }
        }
    }

    //get the current time in the media
    abstract protected Duration getCurrentTimeInVideo();

    /*
    get the appropriate horizontal position along the note marking area
    based on the time in the media associated with that marker
    
    requires: time != null
              0 <= time <= total duration of media
    */
    abstract protected double getMarkerAreaXPos(final Duration time);

    /*
    go to the specified time in the media; if the media was paused before
    this's called, the media will also be paused at the new time, and if
    the media was playing before, the media will also be playing at the
    new time
    
    requires: time != null
              0 <= time <= total duration of media
    */
    abstract protected void seekInVideo(final Duration time);

    //pause the media
    abstract protected void pauseVideo();

    // play the media
    abstract protected void playVideo();
    
    // stop the media
    abstract protected void stopVideo();
    
    // return the total duration of the current media
    abstract protected Duration getTotalDuration();
    
}
