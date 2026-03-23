/**
 * O Model armazena informações sobre o estado do jogo, incluindo a grade subjacente de CellValues (conforme carregado do
 * arquivo de texto), vários indicadores booleanos sobre o estado do jogo, nível, pontuação, e o movimento do Ralph e fantasmas.
 */

package pckRalphman;

import javafx.geometry.Point2D;

import javafx.fxml.FXML;
import java.io.*;

import java.util.*;

public class RalphManModel {
    public enum CellValue {
        EMPTY, SMALLDOT, BIGDOT, WALL, GHOST1HOME, GHOST2HOME, RALPHMANHOME
    };
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    };
    @FXML private int rowCount;
    @FXML private int columnCount;
    private CellValue[][] grid;
    private int score;
    private int level;
    private int dotCount;
    private static boolean gameOver;
    private static boolean youWon;
    private static boolean ghostEatingMode;
    private boolean justAteBigDot;
    private double speedMultiplier = 1.0;
    private Point2D ralphmanVelocity;
    private Point2D ralphmanLocation;
    private Point2D ghost1Location;
    private Point2D ghost1Velocity;
    private Point2D ghost2Location;
    private Point2D ghost2Velocity;
    private static Direction lastDirection;
    private static Direction currentDirection;

    /**
     * Inicia um novo jogo na inicialização
     */
    public RalphManModel() {
        this.startNewGame();
    }

    /**
     * Configura os CellValues da grade baseado no arquivo txt e coloca RalphMan e fantasmas em suas localizações iniciais.
     * "W" indica uma parede, "E" indica um quadrado vazio, "B" indica um ponto grande, "S" indica
     * um ponto pequeno, "1" ou "2" indica a casa dos fantasmas, e "P" indica a posição inicial do Ralph.
     *
     * @param fileName arquivo txt contendo a configuração do tabuleiro
     */
    public void initializeLevel(String fileName) {
        File file = new File(fileName);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()) {
                lineScanner.next();
                columnCount++;
            }
            rowCount++;
        }
        columnCount = columnCount/rowCount;
        Scanner scanner2 = null;
        try {
            scanner2 = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        grid = new CellValue[rowCount][columnCount];
        int row = 0;
        int ralphmanRow = 0;
        int ralphmanColumn = 0;
        int ghost1Row = 0;
        int ghost1Column = 0;
        int ghost2Row = 0;
        int ghost2Column = 0;
        while(scanner2.hasNextLine()){
            int column = 0;
            String line= scanner2.nextLine();
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()){
                String value = lineScanner.next();
                CellValue thisValue;
                if (value.equals("W")){
                    thisValue = CellValue.WALL;
                }
                else if (value.equals("S")){
                    thisValue = CellValue.SMALLDOT;
                    dotCount++;
                }
                else if (value.equals("B")){
                    thisValue = CellValue.BIGDOT;
                    dotCount++;
                }
                else if (value.equals("1")){
                    thisValue = CellValue.GHOST1HOME;
                    ghost1Row = row;
                    ghost1Column = column;
                }
                else if (value.equals("2")){
                    thisValue = CellValue.GHOST2HOME;
                    ghost2Row = row;
                    ghost2Column = column;
                }
                else if (value.equals("P")){
                    thisValue = CellValue.RALPHMANHOME;
                    ralphmanRow = row;
                    ralphmanColumn = column;
                }
                else //(value.equals("E"))
                {
                    thisValue = CellValue.EMPTY;
                }
                grid[row][column] = thisValue;
                column++;
            }
            row++;
        }
        ralphmanLocation = new Point2D(ralphmanRow, ralphmanColumn);
        ralphmanVelocity = new Point2D(0,0);
        ghost1Location = new Point2D(ghost1Row,ghost1Column);
        ghost1Velocity = new Point2D(-1, 0);
        ghost2Location = new Point2D(ghost2Row,ghost2Column);
        ghost2Velocity = new Point2D(-1, 0);
        currentDirection = Direction.NONE;
        lastDirection = Direction.NONE;
    }

    /** Inicializa valores das variáveis de instância e inicializa o mapa do nível
     */
    public void startNewGame() {
        this.gameOver = false;
        this.youWon = false;
        this.ghostEatingMode = false;
        this.speedMultiplier = 1.0;
        this.justAteBigDot = false;
        dotCount = 0;
        rowCount = 0;
        columnCount = 0;
        this.score = 0;
        this.level = 1;
        this.initializeLevel(Controller.getLevelFile(0));
    }

    /** Inicializa o mapa do nível para o próximo nível
     *
     */
    public void startNextLevel() {
        if (this.isLevelComplete()) {
            this.level++;
            rowCount = 0;
            columnCount = 0;
            youWon = false;
            ghostEatingMode = false;
            this.speedMultiplier = 1.0;
            try {
                this.initializeLevel(Controller.getLevelFile(level - 1));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                //if there are no levels left in the level array, the game ends
                youWon = true;
                gameOver = true;
                level--;
            }
        }
    }

    /**
     * Move o Ralph baseado na direção indicada pelo usuário (baseado na entrada de teclado do Controller)
     * @param direction a direção mais recentemente inserida para o Ralph se mover
     */
    public void moveRalphman(Direction direction) {
        Point2D potentialRalphmanVelocity = changeVelocity(direction);
        Point2D potentialRalphmanLocation = ralphmanLocation.add(potentialRalphmanVelocity);
        //se o RalphMan sair da tela, dê a volta
        potentialRalphmanLocation = setGoingOffscreenNewLocation(potentialRalphmanLocation);
        //determine se o RalphMan deve mudar de direção ou continuar na direção mais recente
        //se a entrada de direção mais recente for a mesma que a entrada anterior, verifique paredes
        if (direction.equals(lastDirection)) {
            //se mover na mesma direção resultaria em bater em uma parede, pare de se mover ou destrua a parede se estiver no modo de comer fantasmas
            if (grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] == CellValue.WALL){

                if (ghostEatingMode) {
                    // SEMPRE quebra a parede
                    grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] = CellValue.EMPTY;

                    ralphmanVelocity = potentialRalphmanVelocity;
                    ralphmanLocation = potentialRalphmanLocation;
                    setLastDirection(direction); // 🔥 IMPORTANTE

                } else {
                    ralphmanVelocity = changeVelocity(Direction.NONE);
                    setLastDirection(Direction.NONE);
                }
            }
            else {
                ralphmanVelocity = potentialRalphmanVelocity;
                ralphmanLocation = potentialRalphmanLocation;
            }
        }
        //se a entrada de direção mais recente não for a mesma que a entrada anterior, verifique paredes e cantos antes de ir em uma nova direção
        else {
            //se o RalphMan bateria em uma parede com a nova entrada de direção, verifique se ele não bateria em uma parede diferente se continuasse na direção anterior
            if (grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] == CellValue.WALL){
                if (ghostEatingMode) {
                    grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] = CellValue.EMPTY;
                    ralphmanVelocity = potentialRalphmanVelocity;
                    ralphmanLocation = potentialRalphmanLocation;
                    setLastDirection(direction);
                } else {
                    potentialRalphmanVelocity = changeVelocity(lastDirection);
                    potentialRalphmanLocation = ralphmanLocation.add(potentialRalphmanVelocity);
                    potentialRalphmanLocation = setGoingOffscreenNewLocation(potentialRalphmanLocation);
                    //if changing direction would hit another wall, stop moving
                    if (grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] == CellValue.WALL){
                        ralphmanVelocity = changeVelocity(Direction.NONE);
                        setLastDirection(Direction.NONE);
                    }
                    else {
                        ralphmanVelocity = changeVelocity(lastDirection);
                        ralphmanLocation = ralphmanLocation.add(ralphmanVelocity);
                    }
                }
            }
            //otherwise, change direction and keep moving
            else {
                ralphmanVelocity = potentialRalphmanVelocity;
                ralphmanLocation = potentialRalphmanLocation;
                setLastDirection(direction);
            }
        }
    }

    /**
     * Move os fantasmas para seguir o RalphMan conforme estabelecido no método moveAGhost()
     */
    public void moveGhosts() {
        Point2D[] ghost1Data = moveAGhost(ghost1Velocity, ghost1Location);
        Point2D[] ghost2Data = moveAGhost(ghost2Velocity, ghost2Location);
        ghost1Velocity = ghost1Data[0];
        ghost1Location = ghost1Data[1];
        ghost2Velocity = ghost2Data[0];
        ghost2Location = ghost2Data[1];

    }

    /**
     * Move um fantasma para seguir o RalphMan se ele estiver na mesma linha ou coluna, ou se afastar do RalphMan se estiver no modo de comer fantasmas, caso contrário, mova-se aleatoriamente quando bater em uma parede.
     * @param velocity a velocidade atual do fantasma especificado
     * @param location a localização atual do fantasma especificado
     * @return um array de Point2Ds contendo uma nova velocidade e localização para o fantasma
     */
    public Point2D[] moveAGhost(Point2D velocity, Point2D location){
        Random generator = new Random();
        //se o fantasma estiver na mesma linha ou coluna que o RalphMan e não estiver no modo de comer fantasmas,
        // vá na direção dele até bater em uma parede, então vá em uma direção diferente
        //caso contrário, vá em uma direção aleatória, e se bater em uma parede vá em uma direção aleatória diferente
        if (!ghostEatingMode) {
            //verifique se o fantasma está na coluna do RalphMan e mova-se em direção a ele
            if (location.getY() == ralphmanLocation.getY()) {
                if (location.getX() > ralphmanLocation.getX()) {
                    velocity = changeVelocity(Direction.UP);
                } else {
                    velocity = changeVelocity(Direction.DOWN);
                }
                Point2D potentialLocation = location.add(velocity);
                //se o fantasma sair da tela, dê a volta
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                //gere novas direções aleatórias até que o fantasma possa se mover sem bater em uma parede
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            }
            //verifique se o fantasma está na linha do RalphMan e mova-se em direção a ele
            else if (location.getX() == ralphmanLocation.getX()) {
                if (location.getY() > ralphmanLocation.getY()) {
                    velocity = changeVelocity(Direction.LEFT);
                } else {
                    velocity = changeVelocity(Direction.RIGHT);
                }
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            }
            //mova-se em uma direção aleatória consistente até bater em uma parede, então escolha uma nova direção aleatória
            else{
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while(grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL){
                    int randomNum = generator.nextInt( 4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            }
        }
        //se o fantasma estiver na mesma linha ou coluna que o RalphMan e no modo de comer fantasmas, vá na direção oposta
        // até bater em uma parede, então vá em uma direção diferente
        //caso contrário, vá em uma direção aleatória, e se bater em uma parede vá em uma direção aleatória diferente
        if (ghostEatingMode) {
            if (location.getY() == ralphmanLocation.getY()) {
                if (location.getX() > ralphmanLocation.getX()) {
                    velocity = changeVelocity(Direction.DOWN);
                } else {
                    velocity = changeVelocity(Direction.UP);
                }
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            } else if (location.getX() == ralphmanLocation.getX()) {
                if (location.getY() > ralphmanLocation.getY()) {
                    velocity = changeVelocity(Direction.RIGHT);
                } else {
                    velocity = changeVelocity(Direction.LEFT);
                }
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            }
            else{
                Point2D potentialLocation = location.add(velocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                while(grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL){
                    int randomNum = generator.nextInt( 4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            }
        }
        Point2D[] data = {velocity, location};
        return data;

    }


    /**
     * Envolve o tabuleiro do jogo se a localização do objeto estiver fora da tela
     * @param objectLocation a localização do objeto especificado
     * @return Point2D nova localização envolvida
     */
    public Point2D setGoingOffscreenNewLocation(Point2D objectLocation) {
        //se o objeto sair da tela à direita
        if (objectLocation.getY() >= columnCount) {
            objectLocation = new Point2D(objectLocation.getX(), 0);
        }
        //se o objeto sair da tela à esquerda
        if (objectLocation.getY() < 0) {
            objectLocation = new Point2D(objectLocation.getX(), columnCount - 1);
        }
        //se o objeto sair da tela na parte inferior
        if (objectLocation.getX() >= rowCount) {
            objectLocation = new Point2D(0, objectLocation.getY());
        }
        //se o objeto sair da tela na parte superior
        if (objectLocation.getX() < 0) {
            objectLocation = new Point2D(rowCount - 1, objectLocation.getY());
        }
        return objectLocation;
    }

    /**
     * Conecta cada Direction a um inteiro de 0-3
     * @param x um inteiro
     * @return a Direction correspondente
     */
    public Direction intToDirection(int x){
        if (x == 0){
            return Direction.LEFT;
        }
        else if (x == 1){
            return Direction.RIGHT;
        }
        else if(x == 2){
            return Direction.UP;
        }
        else{
            return Direction.DOWN;
        }
    }

    /**
     * Redefine a localização e velocidade do ghost1 para seu estado inicial
     */
    public void sendGhost1Home() {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == CellValue.GHOST1HOME) {
                    ghost1Location = new Point2D(row, column);
                }
            }
        }
        ghost1Velocity = new Point2D(-1, 0);
    }

    /**
     * Redefine a localização e velocidade do ghost2 para seu estado inicial
     */
    public void sendGhost2Home() {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == CellValue.GHOST2HOME) {
                    ghost2Location = new Point2D(row, column);
                }
            }
        }
        ghost2Velocity = new Point2D(-1, 0);
    }

    /**
     * Atualiza o modelo para refletir o movimento do RalphMan e dos fantasmas e a mudança de estado de quaisquer objetos comidos
     * durante esses movimentos. Alterna o estado do jogo para ou do modo de comer fantasmas.
     * @param direction a direção mais recentemente inserida para o RalphMan se mover
     */
    public void step(Direction direction) {
        this.moveRalphman(direction);
        // Garanta que o RalphMan esteja dentro dos limites
        if (ralphmanLocation.getX() < 0) ralphmanLocation = new Point2D(rowCount - 1, ralphmanLocation.getY());
        if (ralphmanLocation.getX() >= rowCount) ralphmanLocation = new Point2D(0, ralphmanLocation.getY());
        if (ralphmanLocation.getY() < 0) ralphmanLocation = new Point2D(ralphmanLocation.getX(), columnCount - 1);
        if (ralphmanLocation.getY() >= columnCount) ralphmanLocation = new Point2D(ralphmanLocation.getX(), 0);
        //se o RalphMan estiver em um ponto pequeno, delete o ponto pequeno
        CellValue ralphmanLocationCellValue = grid[(int) ralphmanLocation.getX()][(int) ralphmanLocation.getY()];
        if (ralphmanLocationCellValue == CellValue.SMALLDOT) {
            grid[(int) ralphmanLocation.getX()][(int) ralphmanLocation.getY()] = CellValue.EMPTY;
            dotCount--;
            score += 10;
        }
        //se o RalphMan estiver em um ponto grande, delete o ponto grande e mude o estado do jogo para o modo de comer fantasmas e inicialize o contador
        if (ralphmanLocationCellValue == CellValue.BIGDOT) {
            grid[(int) ralphmanLocation.getX()][(int) ralphmanLocation.getY()] = CellValue.EMPTY;
            dotCount--;
            score += 50;

            ghostEatingMode = true;
            this.speedMultiplier = 1.25;

            justAteBigDot = true; // ✅ ESSENCIAL

            Controller.setGhostEatingModeCounter();
        }
        //envie o fantasma de volta para a casa do fantasma se o RalphMan estiver em um fantasma no modo de comer fantasmas
        if (ghostEatingMode) {
            if (ralphmanLocation.equals(ghost1Location)) {
                sendGhost1Home();
                score += 100;
            }
            if (ralphmanLocation.equals(ghost2Location)) {
                sendGhost2Home();
                score += 100;
            }
        }
        //fim de jogo se o RalphMan for comido por um fantasma
        else {
            if (ralphmanLocation.equals(ghost1Location)) {
                gameOver = true;
                ralphmanVelocity = new Point2D(0,0);
            }
            if (ralphmanLocation.equals(ghost2Location)) {
                gameOver = true;
                ralphmanVelocity = new Point2D(0,0);
            }
        }
        //mova os fantasmas e verifique novamente se os fantasmas ou o RalphMan são comidos (repetir essas verificações ajuda a contabilizar números pares/ímpares de quadrados entre fantasmas e RalphMan)
        this.moveGhosts();
        if (ghostEatingMode) {
            if (ralphmanLocation.equals(ghost1Location)) {
                sendGhost1Home();
                score += 100;
            }
            if (ralphmanLocation.equals(ghost2Location)) {
                sendGhost2Home();
                score += 100;
            }
        }
        else {
            if (ralphmanLocation.equals(ghost1Location)) {
                gameOver = true;
                ralphmanVelocity = new Point2D(0,0);
            }
            if (ralphmanLocation.equals(ghost2Location)) {
                gameOver = true;
                ralphmanVelocity = new Point2D(0,0);
            }
        }
        //inicie um novo nível se o nível estiver completo
        if (this.isLevelComplete()) {
            ralphmanVelocity = new Point2D(0,0);
            startNextLevel();
        }
    }

    /**
     * Conecta cada direção a vetores de velocidade Point2D (Esquerda = (-1,0), Direita = (1,0), Cima = (0,-1), Baixo = (0,1))
     * @param direction
     * @return vetor de velocidade Point2D
     */
    public Point2D changeVelocity(Direction direction){
        if(direction == Direction.LEFT){
            return new Point2D(0,-1);
        }
        else if(direction == Direction.RIGHT){
            return new Point2D(0,1);
        }
        else if(direction == Direction.UP){
            return new Point2D(-1,0);
        }
        else if(direction == Direction.DOWN){
            return new Point2D(1,0);
        }
        else{
            return new Point2D(0,0);
        }
    }

    public static boolean isGhostEatingMode() {
        return ghostEatingMode;
    }

    public static void setGhostEatingMode(boolean ghostEatingModeBool) {
        ghostEatingMode = ghostEatingModeBool;
    }

    public boolean justAteBigDot() {
        return justAteBigDot;
    }

    public void resetJustAteBigDot() {
        justAteBigDot = false;
    }

    public static boolean isYouWon() {
        return youWon;
    }

    /**
     * Quando todos os pontos são comidos, o nível está completo
     * @return boolean
     */
    public boolean isLevelComplete() {
        return this.dotCount == 0;
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    public CellValue[][] getGrid() {
        return grid;
    }

    /**
     * @param row
     * @param column
     * @return o Valor da Célula da célula (linha, coluna)
     */
    public CellValue getCellValue(int row, int column) {
        assert row >= 0 && row < this.grid.length && column >= 0 && column < this.grid[0].length;
        return this.grid[row][column];
    }

    public static Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction direction) {
        currentDirection = direction;
    }

    public static Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(Direction direction) {
        lastDirection = direction;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /** Adicione novos pontos à pontuação
     *
     * @param points
     */
    public void addToScore(int points) {
        this.score += points;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return o número total de pontos restantes (grandes e pequenos)
     */
    public int getDotCount() {
        return dotCount;
    }

    public void setDotCount(int dotCount) {
        this.dotCount = dotCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public Point2D getRalphmanLocation() {
        return ralphmanLocation;
    }

    public void setRalphmanLocation(Point2D ralphmanLocation) {
        this.ralphmanLocation = ralphmanLocation;
    }

    public Point2D getGhost1Location() {
        return ghost1Location;
    }

    public void setGhost1Location(Point2D ghost1Location) {
        this.ghost1Location = ghost1Location;
    }

    public Point2D getGhost2Location() {
        return ghost2Location;
    }

    public void setGhost2Location(Point2D ghost2Location) {
        this.ghost2Location = ghost2Location;
    }

    public Point2D getralphmanVelocity() {
        return ralphmanVelocity;
    }

    public void setralphmanVelocity(Point2D velocity) {
        this.ralphmanVelocity = velocity;
    }

    public Point2D getGhost1Velocity() {
        return ghost1Velocity;
    }

    public void setGhost1Velocity(Point2D ghost1Velocity) {
        this.ghost1Velocity = ghost1Velocity;
    }

    public Point2D getGhost2Velocity() {
        return ghost2Velocity;
    }

    public void setGhost2Velocity(Point2D ghost2Velocity) {
        this.ghost2Velocity = ghost2Velocity;
    }

    public void resetSpeedMultiplier() {
        this.speedMultiplier = 1.0;
    }
}
