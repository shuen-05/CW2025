package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label linesLabel;

    @FXML
    private Label levelLabel;

    @FXML
    private Label highScoreLabel;

    @FXML
    private Label currentLevelLabel;

    @FXML
    private VBox notificationArea;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private int[][] currentBoardMatrix;

    private ViewData currentBrick;

    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    private int totalLinesCleared = 0;
    private int currentLevel = 1;
    private HighScoreManager highScoreManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load custom font
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);

        highScoreManager = new HighScoreManager();
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        setupKeyHandlers();
        initializeGameOverPanel();

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    private void setupKeyHandlers() {
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    handleGameInput(keyEvent);
                }
                handleSystemInput(keyEvent);
            }
        });
    }

    private void handleGameInput(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case LEFT:
            case A:
                refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                keyEvent.consume();
                break;
            case RIGHT:
            case D:
                refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                keyEvent.consume();
                break;
            case UP:
            case W:
                refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                keyEvent.consume();
                break;
            case DOWN:
            case S:
                moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                keyEvent.consume();
                break;
            case SPACE:
                // Hard drop implementation
                hardDrop();
                keyEvent.consume();
                break;
            case R:
                restartGame();
                keyEvent.consume();
                break;
        }
    }

    private void handleSystemInput(KeyEvent keyEvent) {
        // No system-level keybindings currently.
    }

    private void initializeGameOverPanel() {
        gameOverPanel.setVisible(false);
        gameOverPanel.setEffect(new Glow(0.8));
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initializeGameBoard(boardMatrix);
        refreshGameBackground(boardMatrix);
        updateActiveBrick(brick);
        startGameLoop();

        // Make sure the brick panel is visible and properly positioned
        brickPanel.setVisible(true);
    }

    public void updateActiveBrick(ViewData brick) {
        if (brick == null) {
            return;
        }

        currentBrick = brick;
        initializeNextBrickPreview(brick.getNextBrickData());
        drawActiveBrick();
    }

    private void initializeGameBoard(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = createGameCell();
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }
    }

    private void initializeNextBrickPreview(int[][] nextBrickData) {
        brickPanel.getChildren().clear();

        if (nextBrickData == null || nextBrickData.length == 0) {
            return;
        }

        for (int i = 0; i < nextBrickData.length; i++) {
            for (int j = 0; j < nextBrickData[i].length; j++) {
                Rectangle rectangle = createBrickCell(nextBrickData[i][j]);
                brickPanel.add(rectangle, j, i);
            }
        }
    }

    private Rectangle createGameCell() {
        Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setArcHeight(12);
        rectangle.setArcWidth(12);
        rectangle.getStyleClass().add("grid-cell");
        return rectangle;
    }

    private Rectangle createBrickCell(int color) {
        Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
        setRectangleData(color, rectangle);
        return rectangle;
    }

    private void startGameLoop() {
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    private Paint getFillColor(int i) {
        switch (i) {
            case 0: return Color.TRANSPARENT;
            case 1: return Color.CYAN;      // I
            case 2: return Color.BLUE;      // J
            case 3: return Color.ORANGE;    // L
            case 4: return Color.YELLOW;    // O
            case 5: return Color.LIMEGREEN; // S
            case 6: return Color.PURPLE;    // T
            case 7: return Color.RED;       // Z
            default: return Color.WHITE;
        }
    }

    private void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE && brick != null) {
            updateActiveBrick(brick);

            // Debug output
            System.out.println("Refreshing brick at X: " + brick.getxPosition() + ", Y: " + brick.getyPosition());
        }
    }

    public void refreshGameBackground(int[][] board) {
        currentBoardMatrix = MatrixOperations.copy(board);
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }

        drawActiveBrick();
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(12);
        rectangle.setArcWidth(12);

        // Apply CSS classes for enhanced styling
        rectangle.getStyleClass().clear();
        if (color != 0) {
            switch (color) {
                case 1: rectangle.getStyleClass().add("brick-i"); break;
                case 2: rectangle.getStyleClass().add("brick-j"); break;
                case 3: rectangle.getStyleClass().add("brick-l"); break;
                case 4: rectangle.getStyleClass().add("brick-o"); break;
                case 5: rectangle.getStyleClass().add("brick-s"); break;
                case 6: rectangle.getStyleClass().add("brick-t"); break;
                case 7: rectangle.getStyleClass().add("brick-z"); break;
            }
        }
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                handleLineClear(downData);
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    private void drawActiveBrick() {
        if (currentBoardMatrix == null || displayMatrix == null) {
            return;
        }

        for (int i = 2; i < currentBoardMatrix.length; i++) {
            for (int j = 0; j < currentBoardMatrix[i].length; j++) {
                setRectangleData(currentBoardMatrix[i][j], displayMatrix[i][j]);
            }
        }

        if (currentBrick == null) {
            return;
        }

        int[][] brickData = currentBrick.getBrickData();
        int originX = currentBrick.getxPosition();
        int originY = currentBrick.getyPosition();

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                int value = brickData[i][j];
                if (value == 0) {
                    continue;
                }

                int targetX = originX + j;
                int targetY = originY + i;

                if (targetY < 2 || targetY >= displayMatrix.length) {
                    continue;
                }
                if (targetX < 0 || targetX >= displayMatrix[targetY].length) {
                    continue;
                }
                if (displayMatrix[targetY][targetX] == null) {
                    continue;
                }

                setRectangleData(value, displayMatrix[targetY][targetX]);
            }
        }
    }

    private void handleLineClear(DownData downData) {
        totalLinesCleared += downData.getClearRow().getLinesRemoved();
        updateLevel();
        updateLinesDisplay();

        // Show score bonus notification
        NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());

        // Add visual effect for line clear
        addLineClearEffect();
    }

    private void addLineClearEffect() {
        Glow glow = new Glow(0.8);
        gamePanel.setEffect(glow);

        Timeline removeGlow = new Timeline(new KeyFrame(
                Duration.millis(300),
                e -> gamePanel.setEffect(null)
        ));
        removeGlow.play();
    }

    private void hardDrop() {
        if (isPause.getValue() == Boolean.FALSE && eventListener != null) {
            DownData downData = eventListener.onHardDrop(new MoveEvent(EventType.HARD_DROP, EventSource.USER));

            // Handle line clear if any lines were cleared
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                handleLineClear(downData);
            }

            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty integerProperty) {
        if (scoreLabel != null) {
            scoreLabel.textProperty().bind(integerProperty.asString());
        }
        updateLinesDisplay();
        updateLevelDisplay();
        updateHighScoreDisplay();
    }

    public void gameOver() {
        timeLine.stop();

        // Enhanced game over display
        gameOverPanel.setVisible(true);
        gameOverPanel.toFront();
        isGameOver.setValue(Boolean.TRUE);

        // Add pulsing effect to game over panel
        addPulsingEffect(gameOverPanel);
    }

    private void showNewHighScoreCelebration() {
        NotificationPanel highScorePanel = new NotificationPanel("NEW HIGH SCORE! 🏆");
        highScorePanel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 28px;");
        groupNotification.getChildren().add(highScorePanel);
        highScorePanel.showScore(groupNotification.getChildren());
    }

    private void addPulsingEffect(javafx.scene.Node node) {
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new javafx.event.EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                    }
                }),
                new KeyFrame(Duration.millis(500), new javafx.event.EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        node.setScaleX(1.05);
                        node.setScaleY(1.05);
                    }
                }),
                new KeyFrame(Duration.millis(1000), new javafx.event.EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                    }
                })
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    private void resetGameState() {
        gameOverPanel.setVisible(false);
        notificationArea.getChildren().clear();
        groupNotification.getChildren().clear();

        // Reset counters
        totalLinesCleared = 0;
        currentLevel = 1;
        updateLinesDisplay();
        updateLevelDisplay();

        gamePanel.setEffect(null);
        gameOverPanel.setScaleX(1.0);
        gameOverPanel.setScaleY(1.0);
    }

    private void startNewGame() {
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
    }

    private void restartGame() {
        timeLine.stop();
        resetGameState();
        startNewGame();
    }

    private void updateLevel() {
        int newLevel = (totalLinesCleared / 10) + 1;
        if (newLevel != currentLevel) {
            currentLevel = newLevel;
            updateLevelDisplay();

            // Update game mechanics
            if (eventListener != null) {
                eventListener.updateScoreLevel(currentLevel);
            }

            // Increase game speed
            updateGameSpeed();

            // Show level up notification
            showLevelUpNotification();
        }
    }

    private void updateLevelDisplay() {
        if (levelLabel != null) {
            levelLabel.setText(String.valueOf(currentLevel));
        }
        if (currentLevelLabel != null) {
            currentLevelLabel.setText(String.valueOf(currentLevel));
        }
    }

    private void updateLinesDisplay() {
        if (linesLabel != null) {
            linesLabel.setText(String.valueOf(totalLinesCleared));
        }
    }

    private void updateHighScoreDisplay() {
        if (highScoreLabel != null) {
            highScoreLabel.setText(String.valueOf(highScoreManager.getHighScore()));
        }
    }

    private void updateGameSpeed() {
        if (timeLine != null) {
            timeLine.stop();
            double newDuration = Math.max(50, 400 - (currentLevel - 1) * 30);
            timeLine = new Timeline(new KeyFrame(
                    Duration.millis(newDuration),
                    event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
            ));
            timeLine.setCycleCount(Timeline.INDEFINITE);
            timeLine.play();
        }
    }

    private void showLevelUpNotification() {
        NotificationPanel levelUpPanel = new NotificationPanel("LEVEL UP! " + currentLevel + " 🚀");
        levelUpPanel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 24px;");
        notificationArea.getChildren().add(levelUpPanel);

        // Auto-remove after 2 seconds
        Timeline removeNotification = new Timeline(new KeyFrame(
                Duration.seconds(2),
                e -> notificationArea.getChildren().remove(levelUpPanel)
        ));
        removeNotification.play();
    }
}