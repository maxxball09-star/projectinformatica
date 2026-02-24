import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class game1 extends JPanel implements KeyListener {

    Block[] fallenBlocks = new Block[1000];
    int fallenCount = 0;
    GameObject currentObject;
    Timer gameTimer;
    Random rand = new Random();
    JFrame parentFrame;
    boolean gameActive = true;
    int setka_razm = 75;
    int happyTickCounter = 0;
    boolean happyFastMode = false;
    Font customFont;
    Font scoreFont;

    int[] typeDelayCounter = new int[4];
    int[] typeDelayThreshold = {2, 1, 3};

    boolean animatingDrop = false;
    int animationStep = 0;
    final int ANIMATION_STEPS = 15;

    int[] animatingBlocksIndices = new int[1000];
    int[] animationStartY = new int[1000];
    int[] animationTargetY = new int[1000];
    int animatingBlocksCount = 0;

    int[][][] figures = {
            {{0,0}, {1,0}, {2,0}},
            {{0,0}, {0,1}, {0,2}},
            {{0,0}, {1,0}, {0,1}, {1,1}},
            {{0,0}, {1,0}, {2,0}, {0,1}},
            {{0,0}, {1,0}, {1,1}, {2,1}},
            {{0,0}, {-1,1}, {0,1}, {1,1}},
            {{0,0}, {1,0}, {-1,1}, {0,1}, {1,1}}
    };

    int gridWidth = 16;
    int gridHeight = 10;
    int score = 0;

    int[][] nextShape;
    int nextType;

    public game1(JFrame parent) {
        this.parentFrame = parent;
        setPreferredSize(new Dimension(1500, 900));
        addKeyListener(this);
        setFocusable(true);
        customFont = new Font("Arial", Font.BOLD, 24);
        scoreFont = new Font("Arial", Font.BOLD, 36);
        createNewObject();
        gameTimer = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameActive) return;

                if (animatingDrop) {
                    animationStep++;

                    for (int i = 0; i < animatingBlocksCount; i++) {
                        int blockIndex = animatingBlocksIndices[i];
                        if (fallenBlocks[blockIndex] != null) {
                            int startY = animationStartY[i];
                            int targetY = animationTargetY[i];
                            int newY = startY + (targetY - startY) * animationStep / ANIMATION_STEPS;
                            fallenBlocks[blockIndex].y = newY;
                        }
                    }

                    if (animationStep >= ANIMATION_STEPS) {
                        for (int i = 0; i < animatingBlocksCount; i++) {
                            int blockIndex = animatingBlocksIndices[i];
                            if (fallenBlocks[blockIndex] != null) {
                                fallenBlocks[blockIndex].y = animationTargetY[i];
                                fallenBlocks[blockIndex].gridY =
                                        (fallenBlocks[blockIndex].y - 50) / setka_razm;
                            }
                        }

                        animatingDrop = false;
                        animationStep = 0;
                        animatingBlocksCount = 0;

                        checkFullLines();
                    }

                    repaint();
                    return;
                }

                if (currentObject.type == 3) {
                    happyTickCounter++;
                    if (happyTickCounter >= 7) {
                        happyFastMode = !happyFastMode;
                        happyTickCounter = 0;
                    }
                }

                typeDelayCounter[currentObject.type]++;
                int requiredDelay = typeDelayThreshold[currentObject.type - 1];
                if (happyFastMode && currentObject.type == 3) {
                    requiredDelay = 1;
                }

                if (typeDelayCounter[currentObject.type] >= requiredDelay) {
                    typeDelayCounter[currentObject.type] = 0;

                    if (!moveDown()) {
                        freezeObject();
                        checkFullLines();
                        if (isGameOver()) {
                            showGameOver();
                            return;
                        }
                        createNewObject();
                    }
                }
                repaint();
            }
        });
        gameTimer.start();
    }

    private void generateNextFigure() {
        nextShape = figures[rand.nextInt(figures.length)];
        nextType = rand.nextInt(3) + 1;
    }

    private boolean moveDown() {
        if (currentObject == null) return false;

        for (Block block : currentObject.blocks) {
            int newGridY = block.gridY + 1;

            if (newGridY >= gridHeight) {
                return false;
            }

            if (isCellOccupied(block.gridX, newGridY)) {
                return false;
            }
        }

        for (Block block : currentObject.blocks) {
            block.gridY++;
            block.y = 50 + block.gridY * setka_razm;
        }
        currentObject.objY += setka_razm;

        return true;
    }

    private boolean canRotate() {
        if (currentObject == null || animatingDrop) return false;

        if (isSquareShape(currentObject.obj)) {
            return false;
        }

        int[][] rotated = rotateShape(currentObject.obj);

        int baseGridX = currentObject.blocks[0].gridX - currentObject.obj[0][0];
        int baseGridY = currentObject.blocks[0].gridY - currentObject.obj[0][1];

        for (int i = 0; i < rotated.length; i++) {
            int newGridX = baseGridX + rotated[i][0];
            int newGridY = baseGridY + rotated[i][1];

            if (newGridX < 0 || newGridX >= gridWidth ||
                    newGridY < 0 || newGridY >= gridHeight) {
                return false;
            }

            if (isCellOccupied(newGridX, newGridY)) {
                return false;
            }
        }

        return true;
    }

    private int[][] rotateShape(int[][] shape) {
        int[][] rotated = new int[shape.length][2];

        for (int i = 0; i < shape.length; i++) {
            int x = shape[i][0];
            int y = shape[i][1];
            rotated[i][0] = -y;
            rotated[i][1] = x;
        }

        return rotated;
    }

    private void rotateObject() {
        if (!canRotate()) return;

        int[][] rotated = rotateShape(currentObject.obj);

        int baseGridX = currentObject.blocks[0].gridX - currentObject.obj[0][0];
        int baseGridY = currentObject.blocks[0].gridY - currentObject.obj[0][1];

        currentObject.obj = rotated;

        for (int i = 0; i < currentObject.blocks.length; i++) {
            int newGridX = baseGridX + rotated[i][0];
            int newGridY = baseGridY + rotated[i][1];

            currentObject.blocks[i].gridX = newGridX;
            currentObject.blocks[i].gridY = newGridY;
            currentObject.blocks[i].x = newGridX * setka_razm;
            currentObject.blocks[i].y = 50 + newGridY * setka_razm;
        }

        repaint();
    }

    private boolean isSquareShape(int[][] shape) {
        if (shape.length != 4) return false;

        int minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i][0] < minX) minX = shape[i][0];
            if (shape[i][0] > maxX) maxX = shape[i][0];
            if (shape[i][1] < minY) minY = shape[i][1];
            if (shape[i][1] > maxY) maxY = shape[i][1];
        }

        return (maxX - minX == 1) && (maxY - minY == 1);
    }

    private boolean isCellOccupied(int gridX, int gridY) {
        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null &&
                    fallenBlocks[i].gridX == gridX &&
                    fallenBlocks[i].gridY == gridY) {
                return true;
            }
        }
        return false;
    }

    private void freezeObject() {
        for (Block block : currentObject.blocks) {
            if (!isCellOccupied(block.gridX, block.gridY)) {
                if (fallenCount < fallenBlocks.length) {
                    fallenBlocks[fallenCount] = new Block(
                            block.x, block.y,
                            block.gridX, block.gridY,
                            currentObject.type
                    );
                    fallenCount++;
                }
            }
        }

        for (int i = 0; i < typeDelayCounter.length; i++) {
            typeDelayCounter[i] = 0;
        }
    }

    private void checkFullLines() {
        boolean lineRemoved = true;
        while (lineRemoved) {
            lineRemoved = false;

            for (int gridY = gridHeight - 1; gridY >= 0; gridY--) {
                if (hasConsecutiveBlocksInLine(gridY, 7)) {
                    score += 100;
                    removeLineWithAnimation(gridY);
                    lineRemoved = true;
                    break;
                }
            }
        }
    }

    private void removeLineWithAnimation(int removedGridY) {
        boolean[] lineToRemove = new boolean[fallenCount];

        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null && fallenBlocks[i].gridY == removedGridY) {
                lineToRemove[i] = true;
            }
        }

        for (int i = 0; i < fallenCount; i++) {
            if (lineToRemove[i]) {
                fallenBlocks[i] = null;
            }
        }

        compressArray();

        animatingBlocksCount = 0;

        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null && fallenBlocks[i].gridY < removedGridY) {
                int startY = fallenBlocks[i].y;
                int targetY = 50 + (fallenBlocks[i].gridY + 1) * setka_razm;

                animatingBlocksIndices[animatingBlocksCount] = i;
                animationStartY[animatingBlocksCount] = startY;
                animationTargetY[animatingBlocksCount] = targetY;
                animatingBlocksCount++;

                fallenBlocks[i].gridY++;
            }
        }

        if (animatingBlocksCount > 0) {
            animatingDrop = true;
            animationStep = 0;
        }
    }

    private boolean hasConsecutiveBlocksInLine(int gridY, int minConsecutive) {
        boolean[] lineCells = new boolean[gridWidth];

        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null && fallenBlocks[i].gridY == gridY) {
                if (fallenBlocks[i].gridX >= 0 && fallenBlocks[i].gridX < gridWidth) {
                    lineCells[fallenBlocks[i].gridX] = true;
                }
            }
        }

        int maxConsecutive = 0;
        int currentConsecutive = 0;

        for (int x = 0; x < gridWidth; x++) {
            if (lineCells[x]) {
                currentConsecutive++;
                if (currentConsecutive > maxConsecutive) {
                    maxConsecutive = currentConsecutive;
                }
            } else {
                currentConsecutive = 0;
            }
        }

        return maxConsecutive >= minConsecutive;
    }

    private void compressArray() {
        int newIndex = 0;
        Block[] temp = new Block[fallenBlocks.length];

        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null) {
                temp[newIndex] = fallenBlocks[i];
                newIndex++;
            }
        }

        fallenBlocks = temp;
        fallenCount = newIndex;
    }

    private boolean isGameOver() {
        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null && fallenBlocks[i].gridY < 1) {
                return true;
            }
        }
        return false;
    }

    private void showGameOver() {
        gameActive = false;
        gameTimer.stop();
        JOptionPane.showMessageDialog(parentFrame,
                "Игра окончена!\nСобрано блоков: " + fallenCount + "\nОчки: " + score,
                "Конец игры", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createNewObject() {
        if (nextShape == null) {
            generateNextFigure();
        }

        int[][] shape = nextShape;
        int type = nextType;

        int startGridX = getAlignedGridX(shape);
        int startGridY = 0;

        Block[] blocks = new Block[shape.length];
        for (int i = 0; i < shape.length; i++) {
            int gridX = startGridX + shape[i][0];
            int gridY = startGridY + shape[i][1];
            int pixelX = gridX * setka_razm;
            int pixelY = 50 + gridY * setka_razm;

            blocks[i] = new Block(pixelX, pixelY, gridX, gridY, type);
        }

        currentObject = new GameObject(
                startGridX * setka_razm,
                50 + startGridY * setka_razm,
                shape,
                type,
                blocks
        );

        if (type == 3) {
            happyTickCounter = 0;
            happyFastMode = false;
        }

        typeDelayCounter[type] = 0;

        if (isGameOver()) {
            showGameOver();
            return;
        }

        generateNextFigure();
    }

    private int getAlignedGridX(int[][] shape) {
        int minX = 0;
        int maxX = 0;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i][0] < minX) minX = shape[i][0];
            if (shape[i][0] > maxX) maxX = shape[i][0];
        }

        int shapeWidth = maxX - minX + 1;
        int maxStart = gridWidth - shapeWidth;
        int minStart = -minX;

        if (isSquareShape(shape)) {
            return (gridWidth - 2) / 2;
        }

        if (minStart > maxStart) {
            return minStart;
        }

        return minStart + rand.nextInt(maxStart - minStart + 1);
    }

    private boolean canMove(int deltaGridX) {
        if (!gameActive || currentObject == null || animatingDrop) return false;

        for (Block block : currentObject.blocks) {
            int newGridX = block.gridX + deltaGridX;

            if (newGridX < 0 || newGridX >= gridWidth) {
                return false;
            }

            if (isCellOccupied(newGridX, block.gridY)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image fon = Toolkit.getDefaultToolkit().getImage("image//fonscenet.png");
        if (fon != null) {
            g.drawImage(fon, 0, 0, 1500, 900, this);
        } else {
            g.setColor(new Color(20, 20, 30));
            g.fillRect(0, 0, 1500, 900);
        }

        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null) {
                drawBlock(g, fallenBlocks[i]);
            }
        }

        if (currentObject != null && !animatingDrop) {
            drawObject(g, currentObject);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2.5f));
        g.setColor(Color.BLACK);

        for (int x = 0; x <= 1200; x += setka_razm) {
            g.drawLine(x, 50, x, 900);
        }
        for (int y = 50; y <= 900; y += setka_razm) {
            g.drawLine(0, y, 1200, y);
        }

        g2d.setStroke(new BasicStroke(4.0f));
        g.drawRect(0, 50, 1200, 850);

        g.setColor(new Color(255, 255, 0));
        g.setFont(scoreFont);

        int xPos = 1050;
        int yPos = 80;

        g.setColor(Color.BLACK);
        g.drawString("Счет: " + score, xPos - 2, yPos - 2);
        g.drawString("Счет: " + score, xPos - 2, yPos + 2);
        g.drawString("Счет: " + score, xPos + 2, yPos - 2);
        g.drawString("Счет: " + score, xPos + 2, yPos + 2);

        g.setColor(new Color(255, 255, 0));
        g.drawString("Счет: " + score, xPos, yPos);

        g.setColor(Color.WHITE);
        g.setFont(customFont);
        g.drawString("Фигур: " + (fallenCount / 3), 1050, 120);

        if (nextShape != null) {
            int previewX = 1250;
            int previewY = 150;
            int previewSize = 200;

            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(previewX, previewY, previewSize, previewSize);

            g.setColor(Color.WHITE);
            g.drawRect(previewX, previewY, previewSize, previewSize);

            g.setFont(customFont);
            g.drawString("Следующая:", previewX + 10, previewY - 10);

            int cellSize = 30;

            int minX = nextShape[0][0], maxX = nextShape[0][0];
            int minY = nextShape[0][1], maxY = nextShape[0][1];
            for (int i = 1; i < nextShape.length; i++) {
                if (nextShape[i][0] < minX) minX = nextShape[i][0];
                if (nextShape[i][0] > maxX) maxX = nextShape[i][0];
                if (nextShape[i][1] < minY) minY = nextShape[i][1];
                if (nextShape[i][1] > maxY) maxY = nextShape[i][1];
            }

            int shapeWidth = (maxX - minX + 1) * cellSize;
            int shapeHeight = (maxY - minY + 1) * cellSize;

            int offsetX = previewX + (previewSize - shapeWidth) / 2;
            int offsetY = previewY + (previewSize - shapeHeight) / 2;

            for (int i = 0; i < nextShape.length; i++) {
                int localX = nextShape[i][0] - minX;
                int localY = nextShape[i][1] - minY;

                int px = offsetX + localX * cellSize;
                int py = offsetY + localY * cellSize;

                Color color;
                if (nextType == 1) {            // грусть
                    color = new Color(128, 0, 128); // фиолетовый
                } else if (nextType == 2) {     // гнев
                    color = Color.RED;
                } else {                        // радость
                    color = Color.YELLOW;
                }
                g.setColor(color);
                g.fillRect(px, py, cellSize, cellSize);

                g.setColor(Color.BLACK);
                g.drawRect(px, py, cellSize, cellSize);
            }
        }
    }

    private void drawBlock(Graphics g, Block block) {
        Image img = null;
        try {
            if (block.type == 1) {
                img = Toolkit.getDefaultToolkit().getImage("images//sad.jpg");
            } else if (block.type == 2) {
                img = Toolkit.getDefaultToolkit().getImage("images//angry.jpg");
            } else {
                img = Toolkit.getDefaultToolkit().getImage("images//happy.jpg");
            }
        } catch (Exception e) {
            img = null;
        }

        if (img != null) {
            g.drawImage(img, block.x, block.y, setka_razm, setka_razm, this);
        } else {
            Color color;
            if (block.type == 1) {          // грусть
                color = new Color(128, 0, 128); // фиолетовый
            } else if (block.type == 2) {   // гнев
                color = Color.RED;
            } else {                        // радость
                color = Color.YELLOW;
            }
            g.setColor(color);
            g.fillRect(block.x, block.y, setka_razm, setka_razm);
        }
    }

    private void drawObject(Graphics g, GameObject obj) {
        for (Block block : obj.blocks) {
            drawBlock(g, block);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameActive || currentObject == null || animatingDrop) return;

        int key = e.getKeyCode();
        int deltaGridX = 0;

        if (key == KeyEvent.VK_LEFT) {
            deltaGridX = -1;
        } else if (key == KeyEvent.VK_RIGHT) {
            deltaGridX = 1;
        } else if (key == KeyEvent.VK_SPACE) {
            if (!isSquareShape(currentObject.obj)) {
                rotateObject();
            }
            return;
        }

        if (deltaGridX != 0 && canMove(deltaGridX)) {
            for (Block block : currentObject.blocks) {
                block.gridX += deltaGridX;
                block.x = block.gridX * setka_razm;
            }
            currentObject.objX += deltaGridX * setka_razm;
            repaint();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Эмоциональный Тетрис");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1500, 900);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        game1 panel = new game1(f);
        f.add(panel);
        f.setVisible(true);
        panel.requestFocusInWindow();
    }

    class GameObject {
        int objX;
        int objY;
        int[][] obj;
        int type;
        Block[] blocks;

        GameObject(int objX, int objY, int[][] obj, int type, Block[] blocks) {
            this.objX = objX;
            this.objY = objY;
            this.obj = obj;
            this.type = type;
            this.blocks = blocks;
        }
    }

    class Block {
        int x, y;
        int gridX, gridY;
        int type;

        Block(int x, int y, int gridX, int gridY, int type) {
            this.x = x;
            this.y = y;
            this.gridX = gridX;
            this.gridY = gridY;
            this.type = type;
        }
    }
}

