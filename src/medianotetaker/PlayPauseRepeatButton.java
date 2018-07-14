package medianotetaker;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class PlayPauseRepeatButton extends Button {
    private PPRMode pprMode;
    
    // constructor
    // requires: iconSource != null
    PlayPauseRepeatButton(final String playIconSource) {
        pprMode = PPRMode.PLAY;
        setIcon(playIconSource);
        setStyle("-fx-background-color:BLACK");
    }
    
    /* 
    set the icon of the ppr button (implicit object) to be the one taken 
    from iconSource 
    requires: iconSource != null
    */
    void setIcon(final String iconSource) {
        Image icon = new Image(getClass().getResourceAsStream(iconSource));
        ImageView iconView = new ImageView(icon);
        setGraphic(iconView);
    }
    
    /*
    set the object's pprMode to be the argument pprMode
    requires: pprMode != null
    */
    void setMode(final PPRMode pprMode) {
        this.pprMode = pprMode;
    }
    
    // returns the object's ppr mode
    PPRMode getMode() {
        return pprMode;
    }
}
