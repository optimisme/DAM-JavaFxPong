import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private CtrlGame ctrlGame;

    public static void main(String[] args) {

        // Iniciar app JavaFX   
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {

        final int windowWidth = 800;
        final int windowHeight = 600;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        UtilsViews.addView(getClass(), "ViewGame", "./assets/viewGame.fxml");
        ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
        
        Scene scene = new Scene(UtilsViews.parentContainer);
        scene.addEventFilter(KeyEvent.ANY, keyEvent -> { ctrlGame.keyEvent(keyEvent); });
        
        stage.setScene(scene);
        stage.onCloseRequestProperty(); // Call close method when closing window
        stage.setTitle("JavaFX - Pong");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> { ctrlGame.drawingStart(); });
        stage.show();

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:./assets/icon.png");
            stage.getIcons().add(icon);
        }
    }

    @Override
    public void stop() { 
        ctrlGame.drawingStop();
        System.exit(1); // Kill all executor services
    }
}
