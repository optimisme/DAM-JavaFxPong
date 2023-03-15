import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Main extends Application {

    public static Drawing drawing = new Drawing();

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
        
        Scene scene = new Scene(UtilsViews.parentContainer);
        scene.addEventFilter(KeyEvent.ANY, keyEvent -> { keyEvent(keyEvent); });
        
        stage.setScene(scene);
        stage.onCloseRequestProperty(); // Call close method when closing window
        stage.setTitle("JavaFX - Pong");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        // Start drawing loop
        drawing.start(((CtrlGame) UtilsViews.getController("ViewGame")).canvas);

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:./assets/icon.png");
            stage.getIcons().add(icon);
        }
    }

    @Override
    public void stop() { 
        System.exit(1); // Kill all executor services
    }

    public void keyEvent (KeyEvent evt) {

        // Quan apretem una tecla
        if (evt.getEventType() == KeyEvent.KEY_PRESSED) {
            if (evt.getCode() == KeyCode.LEFT) {
                drawing.playerDirection = "left";
            }
            if (evt.getCode() == KeyCode.RIGHT) {
                drawing.playerDirection = "right";
            }
        }

        // Quan deixem anar la tecla
        if (evt.getEventType() == KeyEvent.KEY_RELEASED) {
            if (evt.getCode() == KeyCode.LEFT) {
                if (drawing.playerDirection.equals("left")) {
                    drawing.playerDirection = "none";
                }
            }
            if (evt.getCode() == KeyCode.RIGHT) {
                if (drawing.playerDirection.equals("right")) {
                    drawing.playerDirection = "none";
                }
            }
        }
    }
}
