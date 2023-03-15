import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Drawing {

    long[] frameTimes = new long[120];
  
    private Canvas cnv;
    private GraphicsContext gc;
    private AnimationTimer animationTimer;

    private double borderSize = 5;

    private String gameStatus = "playing";

    private int playerPoints = 0;
    private double playerX = Double.POSITIVE_INFINITY;
    private double playerY = Double.POSITIVE_INFINITY;
    private double playerWidth = 200;
    private double playerHalf = playerWidth / 2;
    private double playerHeight = 5;
    private double playerSpeed = 5;
    public String playerDirection = "none";

    private double ballX = Double.POSITIVE_INFINITY;
    private double ballY = Double.POSITIVE_INFINITY;
    private double ballSize = 15;
    private double ballHalf = ballSize / 2;
    private double ballSpeed = 1.5;
    private String ballDirection = "upRight";
    
    public Drawing () { }

    // Iniciar el context i bucle de dibuix
    public void start (Canvas canvas) {

        cnv = canvas;

        // Definir contexte de dibuix
        gc = canvas.getGraphicsContext2D();

        // Iniciar el bucle de dibuix
        animationTimer = new DrawingFps(this::run, this::draw);

        // Iniciar el bucle de dibuix
        animationTimer.start();
    }

    // Aturar el bucle de dibuix
    public void stop () {
        animationTimer.stop();
    }

    // Animar
    private void run(double fps) {

        double speedFactor = fps / 60;

        double boardWidth = cnv.getWidth();
        double boardHeight = cnv.getHeight();

        double playerMinX = borderSize + playerHalf;
        double playerMaxX = boardWidth - borderSize - playerHalf;

        // Init ball at board center
        if (ballX == Double.POSITIVE_INFINITY) {
            ballX = boardWidth / 2;
            ballY = boardHeight / 2;
        }

        // Move ball
        double ballNextX = ballX;
        double ballNextY = ballY;
        switch (ballDirection) {
            case "upRight": 
                ballNextX = ballX + ballSpeed * speedFactor;
                ballNextY = ballY - ballSpeed * speedFactor;
                break;
            case "upLeft": 
                ballNextX = ballX - ballSpeed * speedFactor;
                ballNextY = ballY - ballSpeed * speedFactor;
                break;
            case "downRight": 
                ballNextX = ballX + ballSpeed * speedFactor;
                ballNextY = ballY + ballSpeed * speedFactor;
                break;
            case "downLeft": 
                ballNextX = ballX - ballSpeed * speedFactor;
                ballNextY = ballY + ballSpeed * speedFactor;
                break;
        }

        // Check ball collision with board sides
        double[][] lineBall = { {ballX, ballY}, {ballNextX, ballNextY} };

        double[][] lineBoardLeft = { {borderSize, 0}, {borderSize, boardHeight} };
        double[] intersectionLeft = findIntersection(lineBall, lineBoardLeft);

        double boardMaxX = boardWidth - borderSize;
        double[][] lineBoardRight = { {boardMaxX, 0}, {boardMaxX, boardHeight} };
        double[] intersectionRight = findIntersection(lineBall, lineBoardRight);

        double[][] lineBoardTop = { {0, borderSize}, {boardWidth, borderSize} };
        double[] intersectionTop = findIntersection(lineBall, lineBoardTop);

        if (intersectionLeft[0] != Double.POSITIVE_INFINITY) {
            switch (ballDirection) {
                case "upLeft": 
                    ballDirection = "upRight";
                    break;
                case "downLeft": 
                    ballDirection = "downRight";
                    break;
            }
            ballX = intersectionLeft[0] + ballHalf;
            ballY = intersectionLeft[1];

        } else if (intersectionRight[0] != Double.POSITIVE_INFINITY) {

            switch (ballDirection) {
                case "upRight": 
                    ballDirection = "upLeft";
                    break;
                case "downRight": 
                    ballDirection = "downLeft";
                    break;
            }
            ballX = intersectionRight[0] - ballHalf;
            ballY = intersectionRight[1];

        } else if (intersectionTop[0] != Double.POSITIVE_INFINITY) {

            switch (ballDirection) {
                case "upRight": 
                    ballDirection = "downRight"; 
                    break;
                case "upLeft": 
                    ballDirection = "downLeft"; 
                    break;
            }
            ballX = intersectionTop[0];
            ballY = intersectionTop[1] + ballHalf;

        } else {
            if (ballNextY > boardHeight) {
                gameStatus = "gameOver";
            } else {
                ballX = ballNextX;
                ballY = ballNextY;
            }
        }

        // Move player
        switch (playerDirection) {
            case "right":
                playerX = playerX + playerSpeed * speedFactor; 
                break;
            case "left":
                playerX = playerX - playerSpeed * speedFactor;
                break;
        }

        // Keep player in bounds
        if (playerX == Double.POSITIVE_INFINITY) {
            playerX = boardWidth / 2;
        } else if (playerX < playerMinX) {
            playerX = playerMinX;
        } else if (playerX > playerMaxX) {
            playerX = playerMaxX;
        }

        // Check ball collision with player
        double[][] linePlayer = { {playerX - playerHalf, playerY}, {playerX + playerHalf, playerY} };
        double[] intersectionPlayer = findIntersection(lineBall, linePlayer);
        if (intersectionPlayer[0] != Double.POSITIVE_INFINITY) {

            switch (ballDirection) {
                case "downRight": 
                    ballDirection = "upRight";
                    break;
                case "downLeft": 
                    ballDirection = "upLeft";
                    break;
            }
            ballX = intersectionPlayer[0];
            ballY = intersectionPlayer[1] - ballHalf;
            playerPoints = playerPoints + 1;
            ballSpeed = ballSpeed + 0.2;
        }

        // Set player Y position
        playerY = cnv.getHeight() - playerHeight - 10;
    }

    // Dibuixar
    private void draw() {

        // Clean drawing area
        gc.clearRect(0, 0, cnv.getWidth(), cnv.getHeight());

        // Draw board
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(borderSize);
        gc.strokeRect(0, 0, borderSize, cnv.getHeight());
        gc.strokeRect(0, 0, cnv.getWidth(), borderSize);
        gc.strokeRect(cnv.getWidth() - borderSize, 0, borderSize, cnv.getHeight());

        // Draw ball
        gc.setFill(Color.BLACK);
        gc.fillArc(ballX - ballHalf, ballY - ballHalf, ballSize, ballSize, 0.0, 360, ArcType.ROUND);

        // Draw player
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(playerHeight);
        gc.strokeRect(playerX - playerHalf, playerY, playerWidth, playerHeight);

        // Draw text with points
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 20));
        String pointsText = "Points: " + playerPoints;
        drawText(gc, pointsText, cnv.getWidth() - 20, 20, "right");

        // Draw game over text
        if (gameStatus.equals("gameOver")) {
            double centerX = cnv.getWidth() / 2;
            double centerY = cnv.getHeight() / 2;

            gc.setFont(new Font("Arial", 40));
            drawText(gc, "GAME OVER", centerX, centerY - 20, "center");

            gc.setFont(new Font("Arial", 25));
            drawText(gc, "You are a loser!", centerX, centerY + 20, "center");
        }
    }

    public static void drawText(GraphicsContext gc, String text, double x, double y, String alignment) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        switch (alignment) {
            case "center":
                x = x - textWidth / 2;
                y = y + textHeight / 2;
                break;
            case "right":
                x = x - textWidth;
                y = y + textHeight / 2;
                break;
            case "left":
                y = y + textHeight / 2;
                break;
        }
        gc.fillText(text, x, y);
    }

    public static double[] findIntersection(double[][] lineA, double[][] lineB) {
        double[] result = new double[2];
    
        double aX0 = lineA[0][0];
        double aY0 = lineA[0][1];
        double aX1 = lineA[1][0];
        double aY1 = lineA[1][1];
    
        double bX0 = lineB[0][0];
        double bY0 = lineB[0][1];
        double bX1 = lineB[1][0];
        double bY1 = lineB[1][1];
    
        double x, y;
    
        if (aX1 == aX0) { // lineA is vertical
            if (bX1 == bX0) { // lineB is vertical too
                result[0] = Double.POSITIVE_INFINITY;
                result[1] = Double.POSITIVE_INFINITY;
                return result;
            }
            x = aX0;
            double bM = (bY1 - bY0) / (bX1 - bX0);
            double bB = bY0 - bM * bX0;
            y = bM * x + bB;
        } else if (bX1 == bX0) { // lineB is vertical
            x = bX0;
            double aM = (aY1 - aY0) / (aX1 - aX0);
            double aB = aY0 - aM * aX0;
            y = aM * x + aB;
        } else {
            double aM = (aY1 - aY0) / (aX1 - aX0);
            double aB = aY0 - aM * aX0;
    
            double bM = (bY1 - bY0) / (bX1 - bX0);
            double bB = bY0 - bM * bX0;
    
            if (aM == bM) {
                result[0] = Double.POSITIVE_INFINITY;
                result[1] = Double.POSITIVE_INFINITY;
                return result;
            }
    
            x = (bB - aB) / (aM - bM);
            y = aM * x + aB;
        }
    
        // Check if the intersection point is within the bounding boxes of both line segments
        boolean withinA = x >= Math.min(aX0, aX1) && x <= Math.max(aX0, aX1) && y >= Math.min(aY0, aY1) && y <= Math.max(aY0, aY1);
        boolean withinB = x >= Math.min(bX0, bX1) && x <= Math.max(bX0, bX1) && y >= Math.min(bY0, bY1) && y <= Math.max(bY0, bY1);
    
        if (withinA && withinB) {
            result[0] = x;
            result[1] = y;
        } else {
            result[0] = Double.POSITIVE_INFINITY;
            result[1] = Double.POSITIVE_INFINITY;
        }
    
        return result;
    }
}