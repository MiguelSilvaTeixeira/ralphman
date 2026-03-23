# Ralph Main
Ralph Main é uma implementação JavaFX do clássico jogo Pac-Man, adaptada com o personagem Detona Ralph. O jogador controla o Ralph para comer pontos pequenos e os pontos grandes, evitando fantasmas. Quando o Ralph come um ponto grande, ele pode destruir paredes do mapa.

## Funcionalidades
- Controles com teclas W/A/S/D ou setas direcionais.
- Três níveis de dificuldade.
- Modo especial: ao comer pontos grandes, o Ralph pode destruir paredes.
- Pontuação e níveis exibidos em tempo real.
- Reinício do jogo com a tecla G.

## Estrutura do Repositório
- **README.md**: Este arquivo, com descrição e instruções.
- **ralphman.iml**: Arquivo de configuração do IntelliJ IDEA.
- **src/module-info.java**: Configuração do módulo Java.
- **src/pckRalphman/**: Código fonte principal (MVC).
  - **Main.java**: Classe principal que inicia a aplicação JavaFX e configura a janela.
  - **Controller.java**: Gerencia entrada do usuário (teclas), atualiza o modelo e a visão, controla o timer.
  - **RalphManModel.java**: Contém a lógica do jogo, estado do tabuleiro, movimento do Ralph e fantasmas.
  - **RalphManView.java**: Renderiza o tabuleiro usando imagens, atualiza a exibição baseada no modelo.
  - **ralphman.fxml**: Arquivo FXML que define o layout da interface (labels para pontuação, nível, etc.).
- **src/levels/**: Arquivos de texto definindo os layouts dos níveis (level1.txt, level2.txt, level3.txt).
- **src/res/**: Recursos visuais (imagens GIF para o Ralph, fantasmas, paredes, pontos).
- **.gitignore**: Arquivos e pastas ignorados pelo Git (ex.: bin/, arquivos temporários).

## Arquitetura e Detalhes Técnicos

### Arquitetura (MVC)
O projeto utiliza o padrão **Model-View-Controller (MVC)** para separar a lógica do jogo da interface do usuário:
- **Model (`RalphManModel.java`)**: Mantém o estado do jogo, incluindo a grade lógica (`grid`), posição dos personagens, pontuação, nível e regras de colisão.
- **View (`RalphManView.java`)**: Gerencia a representação visual. Atualiza as imagens na tela (Ralph, fantasmas, paredes) com base nos dados fornecidos pelo Model a cada frame.
- **Controller (`Controller.java`)**: Coordena o jogo. Processa a entrada do teclado, gerencia o loop de tempo (`Timer`) e orquestra as atualizações entre o Model e a View.

```java
// Exemplo no Controller.java coordenando o loop do jogo
private void update(RalphManModel.Direction direction) {
    // 1. Atualiza a lógica do jogo (Model)
    this.ralphManModel.step(direction);

    // 2. Atualiza a representação visual (View)
    this.ralphManView.update(ralphManModel);

    // 3. Atualiza labels da interface
    this.scoreLabel.setText(String.format("Score: %d", this.ralphManModel.getScore()));
}
```

### Renderização do Mapa
O mapa é construído dinamicamente a partir de arquivos de texto (`level1.txt`, etc.).
1. O **Model** lê o arquivo onde cada caractere representa um objeto específico do jogo.
2. A **View** inicializa uma grade de `ImageViews`.
3. A cada atualização, a View percorre a matriz lógica do Model e atribui a imagem correspondente (sprite da parede, ponto ou personagem) à célula da grade visual.

#### Dicionário do Mapa (.txt)
Cada letra no arquivo de texto é traduzida para um `CellValue` (Enum) no código:

| Letra | Significado | Descrição |
| :---: | :--- | :--- |
| **W** | Wall (Parede) | Obstáculo físico (azul). Destrutível com poder. |
| **S** | Small Dot | Ponto pequeno. Vale 10 pontos. |
| **B** | Big Dot | Ponto grande. Vale 50 pontos e ativa o modo destruidor. |
| **P** | Player | Posição inicial do Ralph. |
| **1** | Ghost 1 | Casa do fantasma 1. |
| **2** | Ghost 2 | Casa do fantasma 2. |
| **E** | Empty | Espaço vazio. |

```java
// Exemplo no RalphManModel.java interpretando o arquivo de texto
if (value.equals("W")){
    thisValue = CellValue.WALL;
}
else if (value.equals("S")){
    thisValue = CellValue.SMALLDOT;
    dotCount++;
}
else if (value.equals("B")){
    thisValue = CellValue.BIGDOT;
} // ... P, 1, 2 mapeados para seus respectivos Enums
```

```java
// Exemplo no RalphManView.java desenhando o mapa
public void update(RalphManModel model) {
    for (int row = 0; row < this.rowCount; row++) {
        for (int column = 0; column < this.columnCount; column++) {
            CellValue value = model.getCellValue(row, column);

            if (value == CellValue.WALL) {
                this.cellViews[row][column].setImage(this.wallImage);
            } else if (value == CellValue.BIGDOT) {
                this.cellViews[row][column].setImage(this.bigDotImage);
            } // ... outros casos
        }
    }
}
```

### Movimentação dos Personagens
- **Ralph**: Move-se baseado em vetores de direção alterados pelas teclas W/A/S/D. O sistema verifica colisões com paredes antes de confirmar o movimento e permite o "wrap-around" (atravessar bordas da tela).
- **Fantasmas**: Possuem uma inteligência artificial simples. Se estiverem alinhados (mesma linha ou coluna) com o Ralph, perseguem-no. Caso contrário, ou ao colidir com paredes, escolhem direções aleatórias.

```java
// Exemplo no RalphManModel.java calculando colisão e 'wrap-around'
public void moveRalphman(Direction direction) {
    Point2D potentialVelocity = changeVelocity(direction);
    Point2D potentialLocation = ralphmanLocation.add(potentialVelocity);

    // Lógica de Wrap-around (atravessar paredes da tela)
    potentialLocation = setGoingOffscreenNewLocation(potentialLocation);

    // Verifica se colidiu com uma parede
    if (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL){
        // Para o movimento ou destrói a parede (se estiver no modo fantasma)
    } else {
        ralphmanLocation = potentialLocation; // Confirma o movimento
    }
}
```

### Modo Fantasma
Ativado ao comer um **Ponto Grande**, alterando a jogabilidade:
- **Mecânica**: Ralph ganha velocidade e a capacidade de **destruir paredes** ao colidir com elas. A lógica dos fantasmas inverte, fazendo com que fujam do Ralph.
- **Visual**: Ralph ganha uma animação de poder e os fantasmas ficam azuis.

```java
// Exemplo no RalphManModel.java da mecânica de destruir paredes
if (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == CellValue.WALL){
    if (ghostEatingMode) {
        // Quebra a parede
        grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] = CellValue.EMPTY;
        
        // Continua movendo através da parede destruída
        ralphmanLocation = potentialLocation; 
    }
}
```

## Requisitos para Rodar
- **Java Development Kit (JDK)**: Versão 17 ou superior (disponível em [oracle.com](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)).
- **Apache Maven**: Versão 3.6 ou superior (baixe de [maven.apache.org](https://maven.apache.org/download.cgi) e adicione ao PATH).

## Como Rodar
1. **Instale o JDK 17** e o **Maven**.
2. **Clone ou baixe o repositório** para uma pasta local (ex.: `C:\ralphman`).
3. **Abra um terminal** na pasta raiz do projeto.
4. **Execute o jogo**:
   ```
   mvn clean compile javafx:run
   ```

Ou execute:
   ```
   cp "src/pckRalphman/ralphman.fxml" "target/classes/pckRalphman/"; java --module-path "javafx-sdk-17.0.2/lib;target/classes" --add-modules javafx.controls,javafx.fxml -m ralphman/pckRalphman.Main
   ```

Isso compila o código, baixa as dependências JavaFX automaticamente e executa o jogo. A janela do Ralph Main abrirá.

## Controles
- **W/A/S/D** ou **setas direcionais**: Mover o Ralph.
- **G**: Reiniciar o jogo.

## Controles
- **W/A/S/D** ou **setas direcionais**: Mover o Ralph.
- **G**: Reiniciar o jogo.

## Funcionalidades Especiais
- Comer pontos grandes (brancos) ativa o modo poder: Ralph cresce, acelera, mostra GIF especial e pode destruir paredes.
- Evite os fantasmas; no modo poder, coma-os para pontos extras.