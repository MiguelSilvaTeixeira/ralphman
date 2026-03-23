/**
 * Modelo que guarda o estado do jogo: grade, nível, pontuação e posições/velocidades
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
     * Inicia um novo jogo ao criar o modelo
     */
    public RalphManModel() {
        this.startNewGame();
    }

    /**
     * Lê o arquivo do nível e preenche a grade; também define posições iniciais
     * W = parede, E = vazio, B = ponto grande, S = ponto pequeno, 1/2 = posição do fantasma, P = Ralph
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

    /** Inicializa variáveis e carrega o nível inicial */
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

    /** Inicia o próximo nível se o atual estiver completo */
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
                // se não houver mais níveis, termina o jogo
                youWon = true;
                gameOver = true;
                level--;
            }
        }
    }

    /**
     * Move o RalphMan segundo a direção do jogador
     * @param direction direção requisitada
     */
    public void moveRalphman(Direction direction) {
        Point2D potentialRalphmanVelocity = changeVelocity(direction);
        Point2D potentialRalphmanLocation = ralphmanLocation.add(potentialRalphmanVelocity);
        // se sair da tela, faz o contorno
        potentialRalphmanLocation = setGoingOffscreenNewLocation(potentialRalphmanLocation);
        // decide mudar de direção ou continuar
        if (direction.equals(lastDirection)) {
            // se a nova posição for uma parede
            if (grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] == CellValue.WALL){

                if (ghostEatingMode) {
                    // quebra a parede
                    grid[(int) potentialRalphmanLocation.getX()][(int) potentialRalphmanLocation.getY()] = CellValue.EMPTY;

                    ralphmanVelocity = potentialRalphmanVelocity;
                    ralphmanLocation = potentialRalphmanLocation;
                    setLastDirection(direction); // importante

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
        // se direção nova for diferente da anterior, verifica paredes e cantos
        else {
            // se a nova direção bate em parede, tenta manter a anterior
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
                    // se ainda bater em parede, para
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
            // caso contrário, muda de direção e anda
            else {
                ralphmanVelocity = potentialRalphmanVelocity;
                ralphmanLocation = potentialRalphmanLocation;
                setLastDirection(direction);
            }
        }
    }

    /** Move os dois fantasmas */
    public void moveGhosts() {
        Point2D[] ghost1Data = moveAGhost(ghost1Velocity, ghost1Location);
        Point2D[] ghost2Data = moveAGhost(ghost2Velocity, ghost2Location);
        ghost1Velocity = ghost1Data[0];
        ghost1Location = ghost1Data[1];
        ghost2Velocity = ghost2Data[0];
        ghost2Location = ghost2Data[1];

    }

    /**
     * Move um fantasma: persegue o RalphMan em mesma linha/coluna ou anda aleatoriamente ao bater em parede
     * @param velocity velocidade atual
     * @param location posição atual
     * @return array com nova velocidade e posição
     */
    public Point2D[] moveAGhost(Point2D velocity, Point2D location){
        Random generator = new Random();
        // se não estiver no modo de comer fantasmas, persegue RalphMan quando alinhado
        if (!ghostEatingMode) {
            // mesmo coluna: mover verticalmente em direção ao RalphMan
            if (location.getY() == ralphmanLocation.getY()) {
                if (location.getX() > ralphmanLocation.getX()) {
                    velocity = changeVelocity(Direction.UP);
                } else {
                    velocity = changeVelocity(Direction.DOWN);
                }
                Point2D potentialLocation = location.add(velocity);
                // contorna se sair da tela
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                // escolhe direções aleatórias até não bater em parede
                while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL) {
                    int randomNum = generator.nextInt(4);
                    Direction direction = intToDirection(randomNum);
                    velocity = changeVelocity(direction);
                    potentialLocation = location.add(velocity);
                    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);
                }
                location = potentialLocation;
            }
            // mesma linha: mover horizontalmente em direção ao RalphMan
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
            // caso geral: segue a velocidade atual até bater em parede, então escolhe nova direção
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
        // se estiver no modo de comer fantasmas, afasta-se do RalphMan quando alinhado
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
     * Envolve a posição se estiver fora dos limites da grade
     * @param objectLocation posição do objeto
     * @return nova posição dentro dos limites
     */
    public Point2D setGoingOffscreenNewLocation(Point2D objectLocation) {
        // saiu à direita
        if (objectLocation.getY() >= columnCount) {
            objectLocation = new Point2D(objectLocation.getX(), 0);
        }
        // saiu à esquerda
        if (objectLocation.getY() < 0) {
            objectLocation = new Point2D(objectLocation.getX(), columnCount - 1);
        }
        // saiu embaixo
        if (objectLocation.getX() >= rowCount) {
            objectLocation = new Point2D(0, objectLocation.getY());
        }
        // saiu em cima
        if (objectLocation.getX() < 0) {
            objectLocation = new Point2D(rowCount - 1, objectLocation.getY());
        }
        return objectLocation;
    }


    /**
     * Converte inteiro 0-3 em Direction
     * @param x inteiro
     * @return direção
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

    /** Redefine fantasma 1 para sua casa */
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

    /** Redefine fantasma 2 para sua casa */
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
     * Atualiza o estado do jogo por um passo: move Ralph, atualiza pontos e fantasmas
     * @param direction direção do jogador
     */
    public void step(Direction direction) {
        this.moveRalphman(direction);
        // garante limites
        if (ralphmanLocation.getX() < 0) ralphmanLocation = new Point2D(rowCount - 1, ralphmanLocation.getY());
        if (ralphmanLocation.getX() >= rowCount) ralphmanLocation = new Point2D(0, ralphmanLocation.getY());
        if (ralphmanLocation.getY() < 0) ralphmanLocation = new Point2D(ralphmanLocation.getX(), columnCount - 1);
        if (ralphmanLocation.getY() >= columnCount) ralphmanLocation = new Point2D(ralphmanLocation.getX(), 0);
        // se Ralph estiver em ponto pequeno, remove e soma pontos
        CellValue ralphmanLocationCellValue = grid[(int) ralphmanLocation.getX()][(int) ralphmanLocation.getY()];
        if (ralphmanLocationCellValue == CellValue.SMALLDOT) {
            grid[(int) ralphmanLocation.getX()][(int) ralphmanLocation.getY()] = CellValue.EMPTY;
            dotCount--;
            score += 10;
        }
        // se Ralph estiver em ponto grande, ativa modo de comer fantasmas
        if (ralphmanLocationCellValue == CellValue.BIGDOT) {
            grid[(int) ralphmanLocation.getX()][(int) ralphmanLocation.getY()] = CellValue.EMPTY;
            dotCount--;
            score += 50;

            ghostEatingMode = true;
            this.speedMultiplier = 1.25;

            justAteBigDot = true; // essencial

            Controller.setGhostEatingModeCounter();
        }
        // se no modo de comer fantasmas e Ralph encontrar um fantasma, manda o fantasma para casa
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
        // se não estiver no modo de comer fantasmas e Ralph encontrar um fantasma, fim de jogo
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
        // move fantasmas e verifica novamente colisões
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
        // inicia próximo nível se atual estiver completo
        if (this.isLevelComplete()) {
            ralphmanVelocity = new Point2D(0,0);
            startNextLevel();
        }
    }

    /**
     * Converte direção para vetor de velocidade:
     * Esquerda=(0,-1) Direita=(0,1) Cima=(-1,0) Baixo=(1,0)
     * @param direction direção
     * @return vetor de velocidade
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
     * Retorna true quando não há mais pontos no nível
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
     * Retorna o valor da célula (linha, coluna)
     * @param row linha
     * @param column coluna
     * @return valor da célula
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

    /** Adiciona pontos à pontuação */
    public void addToScore(int points) {
        this.score += points;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /** Retorna o número de pontos restantes */
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
