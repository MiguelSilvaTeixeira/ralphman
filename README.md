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