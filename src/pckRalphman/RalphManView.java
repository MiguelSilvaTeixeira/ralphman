/**
 * Agrupa as visões do jogo: tabuleiro, pontuação, nível e tela de fim de jogo
 */

package pckRalphman;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pckRalphman.RalphManModel.CellValue;

public class RalphManView extends Group {
    public final static double CELL_WIDTH = 40.0;

    @FXML private int rowCount;
    @FXML private int columnCount;
    private ImageView[][] cellViews;
    private Image ralphmanRightImage;
    private Image ralphmanUpImage;
    private Image ralphmanDownImage;
    private Image ralphmanLeftImage;
    private Image ghost1Image;
    private Image ghost2Image;
    private Image blueGhostImage;
    private Image wallImage;
    private Image wallGreenImage;
    private Image wallYellowImage;
    private Image bigDotImage;
    private Image smallDotImage;
    private Image ralphmanPowerImage;

    /** Carrega imagens usadas na view */
    public RalphManView() {
        this.ralphmanRightImage = new Image(getClass().getResourceAsStream("/res/detonaralphRight.gif"));
        this.ralphmanUpImage = new Image(getClass().getResourceAsStream("/res/detonaralphUp.png"));
        this.ralphmanDownImage = new Image(getClass().getResourceAsStream("/res/detonaralphDown.gif"));
        this.ralphmanLeftImage = new Image(getClass().getResourceAsStream("/res/detonaralphLeft.gif"));
        this.ghost1Image = new Image(getClass().getResourceAsStream("/res/redghost.gif"));
        this.ghost2Image = new Image(getClass().getResourceAsStream("/res/ghost2.gif"));
        this.blueGhostImage = new Image(getClass().getResourceAsStream("/res/blueghost.gif"));
        // imagens de parede por nível: primeiro wall, depois wallGreen, depois wallYellow
        this.wallImage = new Image(getClass().getResourceAsStream("/res/wall.png"));
        this.wallGreenImage = new Image(getClass().getResourceAsStream("/res/wallGreen.png"));
        this.wallYellowImage = new Image(getClass().getResourceAsStream("/res/wallYellow.png"));
        this.bigDotImage = new Image(getClass().getResourceAsStream("/res/whitedot.png"));
        this.smallDotImage = new Image(getClass().getResourceAsStream("/res/smalldot.png"));
        this.ralphmanPowerImage = new Image(getClass().getResourceAsStream("/res/detonaralphPower.gif"));
    }

    /** Cria a grade de ImageViews */
    private void initializeGrid() {
        if (this.rowCount > 0 && this.columnCount > 0) {
            this.cellViews = new ImageView[this.rowCount][this.columnCount];
            for (int row = 0; row < this.rowCount; row++) {
                for (int column = 0; column < this.columnCount; column++) {
                    ImageView imageView = new ImageView();
                    imageView.setX((double)column * CELL_WIDTH);
                    imageView.setY((double)row * CELL_WIDTH);
                    imageView.setFitWidth(CELL_WIDTH);
                    imageView.setFitHeight(CELL_WIDTH);
                    this.cellViews[row][column] = imageView;
                    this.getChildren().add(imageView);
                }
            }
        }
    }

    /** Atualiza a view a partir do modelo */
    public void update(RalphManModel model) {
        assert model.getRowCount() == this.rowCount && model.getColumnCount() == this.columnCount;

        // escolhe imagem de parede por nível em ciclo: 1->wall, 2->wallGreen, 3->wallYellow, depois repete
        Image[] wallImages = new Image[] { this.wallImage, this.wallGreenImage, this.wallYellowImage };
        Image wallToUse = wallImages[(Math.max(1, model.getLevel()) - 1) % wallImages.length];

        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {

                CellValue value = model.getCellValue(row, column);

                // Desenha o mapa
                if (value == CellValue.WALL) {
                    this.cellViews[row][column].setImage(wallToUse);

                } else if (value == CellValue.BIGDOT) {
                    this.cellViews[row][column].setImage(this.bigDotImage);

                } else if (value == CellValue.SMALLDOT) {
                    this.cellViews[row][column].setImage(this.smallDotImage);

                } else {
                    this.cellViews[row][column].setImage(null);
                }

                // Desenha fantasmas
                if (RalphManModel.isGhostEatingMode()) {

                    if (row == model.getGhost1Location().getX() &&
                            column == model.getGhost1Location().getY()) {

                        this.cellViews[row][column].setImage(this.blueGhostImage);
                    }

                    if (row == model.getGhost2Location().getX() &&
                            column == model.getGhost2Location().getY()) {

                        this.cellViews[row][column].setImage(this.blueGhostImage);
                    }

                } else {

                    if (row == model.getGhost1Location().getX() &&
                            column == model.getGhost1Location().getY()) {

                        this.cellViews[row][column].setImage(this.ghost1Image);
                    }

                    if (row == model.getGhost2Location().getX() &&
                            column == model.getGhost2Location().getY()) {

                        this.cellViews[row][column].setImage(this.ghost2Image);
                    }
                }
            }
        }

        // Desenha o Ralph por último
        int ralphRow = (int) model.getRalphmanLocation().getX();
        int ralphCol = (int) model.getRalphmanLocation().getY();

        Image ralphImage;

        if (RalphManModel.isGhostEatingMode()) {
            ralphImage = this.ralphmanPowerImage;

        } else {
            if (RalphManModel.getLastDirection() == RalphManModel.Direction.RIGHT ||
                    RalphManModel.getLastDirection() == RalphManModel.Direction.NONE) {

                ralphImage = this.ralphmanRightImage;

            } else if (RalphManModel.getLastDirection() == RalphManModel.Direction.LEFT) {
                ralphImage = this.ralphmanLeftImage;

            } else if (RalphManModel.getLastDirection() == RalphManModel.Direction.UP) {
                ralphImage = this.ralphmanUpImage;

            } else {
                ralphImage = this.ralphmanDownImage;
            }
        }

        this.cellViews[ralphRow][ralphCol].setImage(ralphImage);
        this.cellViews[ralphRow][ralphCol].setFitWidth(CELL_WIDTH);
        this.cellViews[ralphRow][ralphCol].setFitHeight(CELL_WIDTH);
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
        this.initializeGrid();
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        this.initializeGrid();
    }
}
