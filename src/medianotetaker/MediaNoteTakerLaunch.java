package medianotetaker;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Optional;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MediaNoteTakerLaunch extends Application {

    private static MenuBar menuBar;
    private static BorderPane root;
    private static String mediaSource;
    private static String mediaName;
    private static MediaNoteTaker noteTaker;
    private boolean forLocalMedia, isAudioOnly;
    private Stage stage;
    private int width, height;


    // set the scene of the primaryStage and show the stage
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        menuBar = makeMenuBar();
        root = new BorderPane();
        root.setStyle("-fx-background-color: Black");
        root.setTop(menuBar);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        width = (int)(screenBounds.getWidth() * 0.7);
        height = (int)(screenBounds.getHeight() * 0.93);
        Scene scene = new Scene(root, width, height);
        scene.setFill(Color.BLACK);

        primaryStage.setTitle("Media Note Taker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /*
    make and return the menu item that the user can click on
    to open a local media file in the media note taker; successfully
    opening a local media will also enable the user to save their progress
    or extract and save their notes in the future by enable the saveProgress
    and extractAndSave menu items
    
    requires: saveProgress, extractAndSave != null
     */
    private MenuItem makeLocalOpenMenuItem(final MenuItem saveProgress,
            final MenuItem extractAndSave) {
        MenuItem localOpen = new MenuItem("Open");
        localOpen.setOnAction((ActionEvent event) -> {
            File mediaFile = chooseLocalMedia();
            if (mediaFile == null) {
                return;
            }
            mediaSource = mediaFile.toURI().toString();
            mediaName = mediaFile.getName();
            if (mediaSource == null) {
                return;
            }
            if (noteTaker != null) {
                isAudioOnly = false;
                noteTaker.stopVideo();
                noteTaker = null;
                System.gc();
            }
            isAudioOnly = fileIsAudioOnly(getExtension(mediaName));
            noteTaker = new LocalMediaNoteTaker((int)(width * 0.85), 
                    (int)(height * 0.9));
            root.setCenter(noteTaker);
            ((LocalMediaNoteTaker) noteTaker).startPlaying(mediaSource,
                    isAudioOnly);
            saveProgress.setDisable(false);
            extractAndSave.setDisable(false);
            forLocalMedia = true;
        });

        return localOpen;
    }

    /*
    return true if the extension represents an audio only file, and
    false if otherwise
    
    requires: extension != null
     */
    private boolean fileIsAudioOnly(String extension) {
        return (extension.equals(".mp3")
                | extension.equals(".wav")
                | extension.equals(".aif")
                | extension.equals(".aiff"));
    }

    /*
    extract the YouTube video ID from url and update the media source 
    to be that ID
    
    requires: url != null and is a valid YouTube video url (either
    web or embedded url)
     */
    private void setMediaSourceFromYouTube(String url) {
        boolean isEmbeddedURL = false;
        int index = url.lastIndexOf("watch?v=");
        if (index == -1) {
            isEmbeddedURL = true;
            index = url.lastIndexOf("embed/");
        }

        if (isEmbeddedURL) {
            mediaSource = url.substring(index + 6);
        } else {
            mediaSource = url.substring(index + 8);
        }
    }

    /*
    make and return the menu item that the user can click on
    to open a media from YouTube in the media note taker; successfully
    opening a local media will also enable the user to save their progress
    or extract and save their notes in the future by enable the saveProgress
    and extractAndSave menu items
     */
    private MenuItem makeURLOpenMenuItem(final MenuItem saveProgress,
            final MenuItem extractAndSave) {
        MenuItem urlOpen = new MenuItem("Open from YouTube URL");
        urlOpen.setOnAction((ActionEvent event) -> {
            TextInputDialog askForURL = new TextInputDialog();
            askForURL.setTitle("URL input");
            askForURL.setHeaderText("Please enter the URL for the YouTube video");
            Optional<String> inputURL = askForURL.showAndWait();

            while (inputURL.isPresent() && inputURL.get().isEmpty()) {
                askForURL = new TextInputDialog();
                askForURL.setTitle("URL input");
                askForURL.setHeaderText("Invalid. Please enter the URL again.");
                inputURL = askForURL.showAndWait();
            }

            if (inputURL.isPresent()) {
                if (noteTaker != null) {
                    isAudioOnly = false;
                    noteTaker.stopVideo();
                    noteTaker = null;
                    System.gc();
                }
                noteTaker = new YouTubeNoteTaker((int)(width * 0.85), 
                    (int)(height * 0.9));
                root.setCenter(noteTaker);

                String url = inputURL.get();
                mediaName = url;
                setMediaSourceFromYouTube(url);
                ((YouTubeNoteTaker) noteTaker).startPlaying(mediaSource);

                saveProgress.setDisable(false);
                extractAndSave.setDisable(false);
                forLocalMedia = false;
            }
        });
        return urlOpen;
    }

    /*
    writes the info from infoList (which stores all the progress--i.e. the
    notes the user has added--the user has made during this session)
    into .DAT file and enables the user to choose where to save this file
    
    requires: infoList != null and is not empty
     */
    private void saveProgressFile(final ArrayList<NoteMarkerInfo> infoList) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "DAT file (.dat)", "*.dat"));
            fc.setTitle("Save progress");

            NoteProgress progress = new NoteProgress(infoList,
                    forLocalMedia, isAudioOnly, mediaSource, mediaName,
                    noteTaker.getTotalDuration());

            File file = fc.showSaveDialog(stage);
            if (file != null) {
                fos = new FileOutputStream(file);
                out = new ObjectOutputStream(fos);
                out.writeObject(progress);
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (fos != null && out != null) {
                    fos.close();
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /*
    make and return the  menu item that the user can click
    on to save their progress in a .DAT file that they can open in the
    future to continue their work (this will display both the media and the
    embedded notes)
     */
    private MenuItem makeSaveProgressMenuItem() {
        MenuItem saveProgress = new MenuItem("Save progress");
        saveProgress.setOnAction((ActionEvent event) -> {
            ArrayList<NoteMarker> markers = noteTaker.getNoteMarkers();
            if (markers.isEmpty()) {
                alertNothingToSave();
                return;
            }
            ArrayList<NoteMarkerInfo> infoList = new ArrayList();
            for (NoteMarker marker : markers) {
                infoList.add(marker.info);
            }
            saveProgressFile(infoList);
        });
        saveProgress.setDisable(true);
        return saveProgress;
    }

    /*
    make and return the menu item that the user can click on to
    extract and save their notes as text files
     */
    private MenuItem makeExtractAndSaveMenuItem() {
        MenuItem extractAndSave = new MenuItem("Extract notes and save as");
        extractAndSave.setOnAction((ActionEvent event) -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Extract and save note");

            ArrayList<NoteMarker> markers = noteTaker.getNoteMarkers();
            if (markers.isEmpty()) {
                alertNothingToSave();
                return;
            }

            String note = writeNoteAsTxt(markers);
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                writeNoteToFile(note, file);
            }
        });
        extractAndSave.setDisable(true);
        return extractAndSave;
    }

    /*
    read from file the previous progress (i.e. the previous notes)
    the user has made
    
    requires: file of type .DAT and is created and saved through this
              application
     */
    private NoteProgress readProgressFromFile(final File file) {
        NoteProgress progress = new NoteProgress();
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fis);
            while (true) {
                try {
                    progress = (NoteProgress) (in.readObject());
                } catch (EOFException ex) {
                    break;
                }
            }
            fis.close();
            in.close();
        } catch (IOException | ClassNotFoundException ex) {
        }
        return progress;
    }

    /*
    make and return the menu item that the user can click on to
    continue their work by opening the .DAT file that they've saved
    earlier; also, enable the user to save their new progress in this
    session or to extract/save their notes by enabling the saveProgress
    and the extractAndSave menu items
     */
    private MenuItem makeContinuePreviousMenuItem(final MenuItem saveProgress,
            final MenuItem extractAndSave) {
        MenuItem continuePrevious = new MenuItem("Continue from last time");
        continuePrevious.setOnAction((ActionEvent event) -> {
            File file = choosePreviousProgress();
            if (file == null) {
                return;
            }
            NoteProgress progress = readProgressFromFile(file);
            if (noteTaker != null) {
                isAudioOnly = false;
                noteTaker.stopVideo();
                noteTaker = null;
                System.gc();
            }
            if (progress.isForLocalMedia()) {
                noteTaker = new LocalMediaNoteTaker((int)(width * 0.85), 
                    (int)(height * 0.9));
                root.setCenter(noteTaker);
                ((LocalMediaNoteTaker) noteTaker).
                        startPlaying(progress.getMediaSource(), progress);
            } else {
                noteTaker = new YouTubeNoteTaker((int)(width * 0.85), 
                    (int)(height * 0.9));
                root.setCenter(noteTaker);
                ((YouTubeNoteTaker) noteTaker).
                        startPlaying(progress.getMediaSource(), progress);
            }
            mediaName = progress.getMediaName();
            saveProgress.setDisable(false);
            extractAndSave.setDisable(false);
        });
        return continuePrevious;
    }

    /* 
    make and return the menu bar of the application, through which the user
    can open a new media file (from local or YouTube), save/continue their 
    progress on a particular media, and save/extract their note separately
     */
    private MenuBar makeMenuBar() {
        MenuBar bar = new MenuBar();
        Menu menuFile = new Menu("File");

        MenuItem saveProgress = makeSaveProgressMenuItem();
        MenuItem extractAndSave = makeExtractAndSaveMenuItem();
        MenuItem localOpen = makeLocalOpenMenuItem(saveProgress, extractAndSave);
        MenuItem urlOpen = makeURLOpenMenuItem(saveProgress, extractAndSave);
        MenuItem continuePrevious = makeContinuePreviousMenuItem(saveProgress,
                extractAndSave);

        menuFile.getItems().addAll(localOpen, urlOpen, continuePrevious,
                saveProgress, extractAndSave);
        bar.getMenus().add(menuFile);
        return bar;
    }

    // alert the user that he/she haven't added any notes to save
    private void alertNothingToSave() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Save Dialog");
        alert.setHeaderText("Nothing to save!");
        alert.showAndWait();
    }

    /*
    return the info of markers arranged in a way that's easy to read;
    this should be called when the user wants to extract and save his/her
    notes separately
     */
    private String writeNoteAsTxt(final ArrayList<NoteMarker> markers) {
        String note = mediaName + "\n\n";
        for (NoteMarker marker : markers) {
            note += marker.info;
        }
        return note;
    }

    /* 
    write the noteContent into file
    
    requires: file != null
     */
    private void writeNoteToFile(final String noteContent, final File file) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(noteContent);
            fw.close();
        } catch (IOException ex) {
        }
    }

    /*
    enable the user to chooose the local media he/she wants to
    open in the application, and then return that file
     */
    private File chooseLocalMedia() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Opening Media");
        fc.getExtensionFilters().addAll(
                new ExtensionFilter("Supported Media Types "
                        + "(.aif, .aiff, .fxm, .flv, .m3u8, .mp3, .mp4, .m4a, .m4v)", 
                        "*.aif", "*.aiff", "*.fxm", "*.flv", "*.m3u8", "*.mp3", 
                        "*.mp4", "*.m4a", "*.m4v","*.wav")
        );
        File selectedFile = fc.showOpenDialog(stage);
        return selectedFile;
    }

    /*
    enable the user to choose the .DAT file that he/she saves from
    last time working on the same video, and then return that file
    
    requires: --> if the associated media is local, its location in the
              directory shouldn't have changed from last time
              --> if the associated media is from YouTube, its url should
              still be valid
     */
    private File choosePreviousProgress() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Opening previous notes");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("DAT file (.dat)", "*.dat")
        );
        File selectedFile = fc.showOpenDialog(stage);
        return selectedFile;
    }

    /*
    get and return the extension the file reached through fileURI
    
    requires: fileURI != null is a valid url for a local file
     */
    private String getExtension(final String fileURI) {
        int i = fileURI.lastIndexOf(".");
        return fileURI.substring(i);
    }
}
