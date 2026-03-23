/**
 * O Controller lida com a entrada do usuário e coordena a atualização do modelo e da visão com a ajuda de um timer.
 */

package pckRalphman;

import javafx.fxml.FXML;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements EventHandler<KeyEvent> {
    final private static double FRAMES_PER_SECOND = 60.0;
    private int contadorFrames = 0;
    private static final int FRAMES_POR_MOVIMENTO = 12;

    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label gameOverLabel;
    @FXML private RalphManView ralphManView;
    private RalphManModel ralphManModel;
    private static final String[] levelFiles = {"src/levels/level1.txt", "src/levels/level2.txt", "src/levels/level3.txt"};

    private Timer timer;
    private static int ghostEatingModeCounter;
    private boolean paused;

    public Controller() {
        this.paused = false;
    }

    /**
     * Inicializa e atualiza o modelo e a visão a partir do primeiro arquivo txt e inicia o timer.
     */
    public void initialize() {
        String file = this.getLevelFile(0);
        this.ralphManModel = new RalphManModel();
        this.update(RalphManModel.Direction.NONE);
        ghostEatingModeCounter = 25;
        this.startTimer();
    }

    /**
     * Agenda o modelo para atualizar baseado no timer.
     */
    private void startTimer() {
        this.timer = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        update(ralphManModel.getCurrentDirection());
                    }
                });
            }
        };

        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    /**
     * Avança o RalphManModel, atualiza a visão, atualiza pontuação e nível, exibe Game Over/You Won, e instruções de como jogar
     * @param direction a direção mais recentemente inserida para o RalphMan se mover
     */
    private void update(RalphManModel.Direction direction) {
        if (!paused) {
            contadorFrames++;

            if (contadorFrames >= FRAMES_POR_MOVIMENTO) {

                this.ralphManModel.step(direction);

                // mover o contador pra cá
                if (ralphManModel.isGhostEatingMode()) {
                    ghostEatingModeCounter--;
                }

                contadorFrames = 0;
            }
        }
        this.ralphManView.update(ralphManModel);
        this.scoreLabel.setText(String.format("Score: %d", this.ralphManModel.getScore()));
        this.levelLabel.setText(String.format("Level: %d", this.ralphManModel.getLevel()));
        if (ralphManModel.justAteBigDot()) {
            pauseForPowerAnimation();
        }
        if (ralphManModel.isGameOver()) {
            this.gameOverLabel.setText(String.format("GAME OVER"));
            this.gameOverLabel.setStyle("-fx-font-size: 150%; -fx-text-fill: red");
            pause();
        }
        if (ralphManModel.isYouWon()) {
            this.gameOverLabel.setText(String.format("YOU WON!"));
            this.gameOverLabel.setStyle("-fx-font-size: 150%; -fx-text-fill: white");
        }
        if (ghostEatingModeCounter == 0 && ralphManModel.isGhostEatingMode()) {
            ralphManModel.setGhostEatingMode(false);
            ralphManModel.resetSpeedMultiplier();
        }
    }

    /**
     * Recebe entrada de teclado do usuário para controlar o movimento do RalphMan e iniciar novos jogos
     * @param keyEvent clique de tecla do usuário
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        RalphManModel.Direction direction = RalphManModel.Direction.NONE;
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            direction = RalphManModel.Direction.LEFT;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            direction = RalphManModel.Direction.RIGHT;
        } else if (code == KeyCode.W || code == KeyCode.UP) {
            direction = RalphManModel.Direction.UP;
        } else if (code == KeyCode.S || code == KeyCode.DOWN) {
            direction = RalphManModel.Direction.DOWN;
        } else if (code == KeyCode.G) {
            pause();
            this.ralphManModel.startNewGame();
            this.gameOverLabel.setText(String.format(""));
            this.gameOverLabel.setStyle("-fx-font-size: 150%; -fx-text-fill: white");
            paused = false;
            this.startTimer();
        } else {
            keyRecognized = false;
        }
        if (keyRecognized) {
            keyEvent.consume();
            ralphManModel.setCurrentDirection(direction);
        }
    }

    /**
     * Pausa o timer
     */
    public void pause() {
            this.timer.cancel();
            this.paused = true;
    }

    /**
     * Pausa para animação de poder
     */
    public void pauseForPowerAnimation() {
        this.paused = true;
        Timer animationTimer = new Timer();
        animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    paused = false;
                    ralphManModel.resetJustAteBigDot();
                });
            }
        }, 2000); // 2 segundos para a animação
    }

    public double getBoardWidth() {
        return RalphManView.CELL_WIDTH * this.ralphManView.getColumnCount();
    }

    public double getBoardHeight() {
        return RalphManView.CELL_WIDTH * this.ralphManView.getRowCount();
    }

    public static void setGhostEatingModeCounter() {
        ghostEatingModeCounter = 25;
    }

    public static int getGhostEatingModeCounter() {
        return ghostEatingModeCounter;
    }

    public static String getLevelFile(int x)
    {
        return levelFiles[x];
    }

    public boolean getPaused() {
        return paused;
    }
}
