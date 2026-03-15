/**
 * Incorpora as várias Visões da aplicação que fazem referência a diferentes partes do Model, incluindo o tabuleiro principal do jogo, o rótulo de pontuação, o rótulo de nível, e o rótulo de Game Over.
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
    private Image bigDotImage;
    private Image smallDotImage;
    private Image ralphmanPowerImage;

    /**
     * Inicializa os valores das variáveis de instância de imagem a partir de arquivos
     */
    public RalphManView() {
        this.ralphmanRightImage = new Image(getClass().getResourceAsStream("/res/detonaralphRight.gif"));
        this.ralphmanUpImage = new Image(getClass().getResourceAsStream("/res/detonaralphUp.png"));
        this.ralphmanDownImage = new Image(getClass().getResourceAsStream("/res/detonaralphDown.gif"));
        this.ralphmanLeftImage = new Image(getClass().getResourceAsStream("/res/detonaralphLeft.gif"));
        this.ghost1Image = new Image(getClass().getResourceAsStream("/res/redghost.gif"));
        this.ghost2Image = new Image(getClass().getResourceAsStream("/res/ghost2.gif"));
        this.blueGhostImage = new Image(getClass().getResourceAsStream("/res/blueghost.gif"));
        this.wallImage = new Image(getClass().getResourceAsStream("/res/wall.png"));
        this.bigDotImage = new Image(getClass().getResourceAsStream("/res/whitedot.png"));
        this.smallDotImage = new Image(getClass().getResourceAsStream("/res/smalldot.png"));
        this.ralphmanPowerImage = new Image(getClass().getResourceAsStream("/res/detonaralphPower.gif"));
    }

    /**
     * Constrói uma grade vazia de ImageViews
     */
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

    /** Atualiza a visão para refletir o estado do modelo
     *
     * @param model
     */
    public void update(RalphManModel model) {
        assert model.getRowCount() == this.rowCount && model.getColumnCount() == this.columnCount;
        //para cada ImageView, defina a imagem para corresponder ao CellValue daquela célula
        for (int row = 0; row < this.rowCount; row++){
            for (int column = 0; column < this.columnCount; column++){
                CellValue value = model.getCellValue(row, column);
                if (value == CellValue.WALL) {
                    this.cellViews[row][column].setImage(this.wallImage);
                }
                else if (value == CellValue.BIGDOT) {
                    this.cellViews[row][column].setImage(this.bigDotImage);
                }
                else if (value == CellValue.SMALLDOT) {
                    this.cellViews[row][column].setImage(this.smallDotImage);
                }
                else {
                    this.cellViews[row][column].setImage(null);
                }
                // Desenho do Ralph
                if (row == model.getRalphmanLocation().getX() && column == model.getRalphmanLocation().getY()) {
                    Image ralphImage;
                    if (model.justAteBigDot()) {
                        ralphImage = this.ralphmanPowerImage;
                    } else {
                        if (RalphManModel.getLastDirection() == RalphManModel.Direction.RIGHT || RalphManModel.getLastDirection() == RalphManModel.Direction.NONE) {
                            ralphImage = this.ralphmanRightImage;
                        } else if (RalphManModel.getLastDirection() == RalphManModel.Direction.LEFT) {
                            ralphImage = this.ralphmanLeftImage;
                        } else if (RalphManModel.getLastDirection() == RalphManModel.Direction.UP) {
                            ralphImage = this.ralphmanUpImage;
                        } else {
                            ralphImage = this.ralphmanDownImage;
                        }
                    }
                    this.cellViews[row][column].setImage(ralphImage);
                    this.cellViews[row][column].setFitWidth(CELL_WIDTH);
                    this.cellViews[row][column].setFitHeight(CELL_WIDTH);
                }
                //exiba fantasmas azuis no modo de comer fantasmas
                if (RalphManModel.isGhostEatingMode()) {
                    if (row == model.getGhost1Location().getX() && column == model.getGhost1Location().getY()) {
                        this.cellViews[row][column].setImage(this.blueGhostImage);
                    }
                    if (row == model.getGhost2Location().getX() && column == model.getGhost2Location().getY()) {
                        this.cellViews[row][column].setImage(this.blueGhostImage);
                    }
                }
                //exiba imagens de fantasmas normais caso contrário
                else {
                    if (row == model.getGhost1Location().getX() && column == model.getGhost1Location().getY()) {
                        this.cellViews[row][column].setImage(this.ghost1Image);
                    }
                    if (row == model.getGhost2Location().getX() && column == model.getGhost2Location().getY()) {
                        this.cellViews[row][column].setImage(this.ghost2Image);
                    }
                }
            }
        }
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
