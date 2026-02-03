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
    public game1(JFrame parent) {
        this.parentFrame = parent;
        setPreferredSize(new Dimension(1200, 900));
        addKeyListener(this);
        setFocusable(true);
        customFont = new Font("Arial", Font.BOLD, 24);
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
                                fallenBlocks[blockIndex].gridY = (fallenBlocks[blockIndex].y - 50) / setka_razm;
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
    private boolean moveDown() {
        if (currentObject == null) return false;
        for (Block block : currentObject.blocks) {
            int newGridY = block.gridY + 1;
            if (newGridY >= gridHeight) {
                return false;
            }
            if (zanyata_li_yacheika(block.gridX, newGridY)) {
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
    private boolean zanyata_li_yacheika(int gridX, int gridY) {
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
            if (!zanyata_li_yacheika(block.gridX, block.gridY)) {
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
                if (ryadom_bloki(gridY, 7)) {
                    udalit_Line(gridY);
                    lineRemoved = true;
                    break;
                }
            }
        }
    }
    private void udalit_Line(int removedGridY) {
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
    private boolean ryadom_bloki(int gridY, int minConsecutive) {
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
                "Игра окончена!\nСобрано блоков: " + fallenCount,
                "Конец игры", JOptionPane.INFORMATION_MESSAGE);
    }
    private void createNewObject() {
        int[][] shape = figures[rand.nextInt(figures.length)];
        int type = rand.nextInt(3) + 1;
        int startGridX = getAlignedGridX();
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
        }
    }
    private int getAlignedGridX() {
        return rand.nextInt(gridWidth - 4);
    }
    private boolean canMove(int deltaGridX) {
        if (!gameActive || currentObject == null || animatingDrop) return false;
        for (Block block : currentObject.blocks) {
            int newGridX = block.gridX + deltaGridX;
            if (newGridX < 0 || newGridX >= gridWidth) {
                return false;
            }
            if (zanyata_li_yacheika(newGridX, block.gridY)) {
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
            g.drawImage(fon, 0, 0, 1200, 900, this);
        } else {
            g.setColor(new Color(20, 20, 30));
            g.fillRect(0, 0, 1200, 900);
        }
        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null) {
                drawBlock(g, fallenBlocks[i]);
            }
        }
        if (currentObject != null && !animatingDrop) {
            drawObject(g, currentObject);
        }
        g.setColor(new Color(255, 255, 255, 200));
        for (int x = 0; x <= 1200; x += setka_razm) {
            g.drawLine(x, 50, x, 900);
        }
        for (int y = 50; y <= 900; y += setka_razm) {
            g.drawLine(0, y, 1200, y);
        }
        g.setColor(new Color(255, 255, 0, 200));
        g.drawRect(0, 50, 1200, 850);
        g.setColor(Color.WHITE);
        g.setFont(customFont);
        g.drawString("Фигур: " + (fallenCount / 3), 1050, 40);
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
            if (block.type == 1) {
                color = Color.BLUE;
            } else if (block.type == 2) {
                color = Color.RED;
            } else {
                color = Color.GREEN;
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

        int deltaGridX = 0;

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            deltaGridX = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            deltaGridX = 1;
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

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200, 900);
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