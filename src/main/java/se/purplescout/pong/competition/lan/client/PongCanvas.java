package se.purplescout.pong.competition.lan.client;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import se.purplescout.pong.game.collision.Rectangle;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.Pong;
import se.purplescout.pong.game.SomethingMovedListener;

class PongCanvas extends Canvas implements SomethingMovedListener {

    public static final Color PADDLE_COLOR = Color.web("#4F3576");
    public static final Color BALL_COLOR = Color.WHITE;
    public static final Color BACKGROUND_COLOR = Color.BLACK;

    private Pong pong;

    public void setPong(Pong pong) {
        this.pong = pong;
        pong.setOnSomethingMovedListener(this);
    }

    @Override
    public void onSomethingMoved() {
        Platform.runLater(() -> {
            drawShapes(PongCanvas.this.getGraphicsContext2D());
//            Toolkit.getDefaultToolkit().sync();
        });
    }

    private void drawShapes(GraphicsContext g) {
        resetGraphics(g);

        if (pong != null) {
            paintPaddle(g, pong.getLeftPaddle());
            paintPaddle(g, pong.getRightPaddle());
            paintBall(g);
        }
    }

    private void resetGraphics(GraphicsContext g) {
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void paintPaddle(GraphicsContext g, Paddle paddle) {
        Rectangle boundingBox = paddle.getBoundingBox();

        int cellColumns = 2;
        double verticalSeparatorWidth = 1;
        double cellWidth = (boundingBox.getWidth() - ((cellColumns -1) * verticalSeparatorWidth)) / cellColumns;

        double horizontalSeparatorWidth = 1;
        double cellHeightCandidate = cellWidth;
        int numberOfRows = (int) Math.ceil((boundingBox.getHeight() + horizontalSeparatorWidth) / (cellHeightCandidate + horizontalSeparatorWidth));
        double cellHeight = (boundingBox.getHeight() + horizontalSeparatorWidth) / (numberOfRows + horizontalSeparatorWidth);

        // Paint paddle background
        g.setFill(PADDLE_COLOR);
        g.fillRect(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());

        // Paint vertical lines
        g.setStroke(BACKGROUND_COLOR);
        for (int column = 0; column < cellColumns; column++) {
            double x = boundingBox.getX() + cellWidth + cellWidth * column;
            g.strokeLine(x, boundingBox.getY(), x, boundingBox.getY() + boundingBox.getHeight());
        }

        // Paint horizontal lines
        for (int row = 0; row < numberOfRows; row++) {
            double y = boundingBox.getY() + cellHeight + cellHeight * row;
            g.strokeLine(boundingBox.getX(), y, boundingBox.getX() + boundingBox.getWidth(), y);
        }
    }

    private void paintBall(GraphicsContext g) {
        g.setFill(BALL_COLOR);
        Rectangle boundingBox = pong.getBall().getBoundingBox();
        g.fillOval(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());
    }
}
