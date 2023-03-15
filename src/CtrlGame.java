import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class CtrlGame implements Initializable {

    private static CtrlGameCanvas drawing = new CtrlGameCanvas();

    @FXML
    private AnchorPane anchor;

    @FXML
    private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize canvas responsive size
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> {
            updateCanvasSize();
        });
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> {
            updateCanvasSize();
        });
    }

    public void startDrawing () {
        drawing.start(canvas);
    }

    public void updateCanvasSize () {

        // Set Canvas size
        canvas.setWidth(UtilsViews.parentContainer.getWidth());
        canvas.setHeight(UtilsViews.parentContainer.getHeight());
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