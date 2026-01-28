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

    int[][][] figures = {
            {{0,0}, {1,0}, {2,0}},
            {{0,0}, {0,1}, {0,2}},
            {{0,0}, {1,0}, {0,1}, {1,1}},
            {{0,0}, {1,0}, {2,0}, {0,1}},
            {{0,0}, {1,0}, {1,1}, {2,1}},
            {{0,0}, {-1,1}, {0,1}, {1,1}},
            {{0,0}, {1,0}, {-1,1}, {0,1}, {1,1}}
    };

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
                if (currentObject.type == 3) {
                    happyTickCounter++;
                    if (happyTickCounter >= 7) {
                        happyFastMode = !happyFastMode;
                        happyTickCounter = 0;
                    }
                }
                if (isAtBottom() || hitsOther()) {
                    freezeObject();
                    checkFullLines();
                    if (isGameOver()) {
                        showGameOver();
                        return;
                    }
                    createNewObject();
                } else {
                    int fallSpeed = getFallSpeed(currentObject.type);
                    int bottom = getBottom(currentObject);
                    int maxFall = 800 - bottom;
                    if (fallSpeed > maxFall && maxFall > 0) {
                        currentObject.objY += maxFall;
                    } else {
                        currentObject.objY += fallSpeed;
                    }
                }
                repaint();
            }
        });
        gameTimer.start();
    }
    private int getBottom(GameObject obj) {
        if (obj == null) return 0;

        int maxY = obj.objY;
        for (int i = 0; i < obj.obj.length; i++) {
            int blockY = obj.objY + obj.obj[i][1] * setka_razm;
            if (blockY > maxY) {
                maxY = blockY;
            }
        }
        return maxY + setka_razm;
    }
    private void freezeObject() {
        for (int i = 0; i < currentObject.obj.length; i++) {
            int blockX = currentObject.objX + currentObject.obj[i][0] * setka_razm;
            int blockY = currentObject.objY + currentObject.obj[i][1] * setka_razm;
            if (blockY + setka_razm > 800) {
                blockY = 800 - setka_razm;
            }
            if (fallenCount < fallenBlocks.length) {
                fallenBlocks[fallenCount] = new Block(blockX, blockY, currentObject.type);
                fallenCount++;
            }
        }
    }

    private void checkFullLines() {
        boolean lineRemoved = true;
        while (lineRemoved) {
            lineRemoved = false;
            for (int lineY = 50; lineY < 800; lineY += setka_razm) {
                int blocksInLine = 0;
                for (int i = 0; i < fallenCount; i++) {
                    if (fallenBlocks[i] != null) {
                        if (fallenBlocks[i].y == lineY) {
                            blocksInLine++;
                        }
                    }
                }
                if (blocksInLine >= 7) {
                    removeLine(lineY);
                    lineRemoved = true;
                    break;
                }
            }
        }
    }

    private void removeLine(int lineY) {
        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null && fallenBlocks[i].y == lineY) {
                fallenBlocks[i] = null;
            }
        }
        compressArray();
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

    private int getFallSpeed(int type) {
        if (type == 1) {
            return 25;
        } else if (type == 2) {
            return 27;
        } else {
            if (happyFastMode) {
                return 60;
            } else {
                return 10;
            }
        }
    }

    private boolean isAtBottom() {
        if (currentObject == null) return true;

        for (int i = 0; i < currentObject.obj.length; i++) {
            int blockY = currentObject.objY + currentObject.obj[i][1] * setka_razm;
            if (blockY + setka_razm >= 800) {
                return true;
            }
        }
        return false;
    }

    private boolean hitsOther() {
        if (currentObject == null) return false;

        int fallSpeed = getFallSpeed(currentObject.type);

        for (int i = 0; i < currentObject.obj.length; i++) {
            int nextBlockY = currentObject.objY + currentObject.obj[i][1] * setka_razm + fallSpeed;
            if (nextBlockY + setka_razm > 800) {
                return true;
            }

            int currentBlockX = currentObject.objX + currentObject.obj[i][0] * setka_razm;
            int currentBlockY = currentObject.objY + currentObject.obj[i][1] * setka_razm + fallSpeed;
            for (int j = 0; j < fallenCount; j++) {
                if (fallenBlocks[j] != null) {
                    if (Math.abs(currentBlockX - fallenBlocks[j].x) < setka_razm &&
                            Math.abs(currentBlockY - fallenBlocks[j].y) < setka_razm) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isGameOver() {
        for (int i = 0; i < fallenCount; i++) {
            if (fallenBlocks[i] != null && fallenBlocks[i].y <= 56) {
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
        int startX = getAlignedX();
        int startY = 56;
        int[][] shape = figures[rand.nextInt(figures.length)];
        int type = rand.nextInt(3) + 1;
        currentObject = new GameObject(startX, startY, shape, type);
        if (type == 3) {
            happyTickCounter = 0;
            happyFastMode = false;
        }
    }

    private int getAlignedX() {
        int rawX = rand.nextInt(1100);
        int alignedX = 0;
        while(alignedX < rawX) {
            alignedX += setka_razm;
        }
        return alignedX;
    }
    private boolean canMove(int deltaX) {
        if (!gameActive) return false;
        int moveDistance = deltaX;
        if (currentObject.type == 2) {
            if (deltaX > 0) {
                moveDistance = 150;
            } else {
                moveDistance = -150;
            }
        }
        int newX = currentObject.objX + moveDistance;
        if (newX < 0 || newX > 1100) return false;

        for (int i = 0; i < currentObject.obj.length; i++) {
            int blockX = newX + currentObject.obj[i][0] * setka_razm;
            int blockY = currentObject.objY + currentObject.obj[i][1] * setka_razm;
            if (blockX < 0 || blockX + setka_razm > 1200) {
                return false;
            }
            for (int j = 0; j < fallenCount; j++) {
                if (fallenBlocks[j] != null) {
                    if (Math.abs(blockX - fallenBlocks[j].x) < setka_razm &&
                            Math.abs(blockY - fallenBlocks[j].y) < setka_razm) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image fon = Toolkit.getDefaultToolkit().getImage("fonscenet.png");
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
        if (currentObject != null) {
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
                img = Toolkit.getDefaultToolkit().getImage("sad.jpg");
            } else if (block.type == 2) {
                img = Toolkit.getDefaultToolkit().getImage("angry.jpg");
            } else {
                img = Toolkit.getDefaultToolkit().getImage("happy.jpg");
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
        Image img = null;
        try {
            if (obj.type == 1) {
                img = Toolkit.getDefaultToolkit().getImage("sad.jpg");
            } else if (obj.type == 2) {
                img = Toolkit.getDefaultToolkit().getImage("angry.jpg");
            } else {
                img = Toolkit.getDefaultToolkit().getImage("happy.jpg");
            }
        } catch (Exception e) {
            img = null;
        }
        for (int i = 0; i < obj.obj.length; i++) {
            int blockX = obj.objX + obj.obj[i][0] * setka_razm;
            int blockY = obj.objY + obj.obj[i][1] * setka_razm;
            g.drawImage(img, blockX, blockY, setka_razm, setka_razm, this);
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameActive) return;
        int moveAmount = setka_razm;
        if (currentObject.type == 1) {
            moveAmount = 38;
        } else if (currentObject.type == 2) {
            moveAmount = 150;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (canMove(-moveAmount)) {
                currentObject.objX -= moveAmount;
                repaint();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (canMove(moveAmount)) {
                currentObject.objX += moveAmount;
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Эмоциональный Тетрис");
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
        GameObject(int objX, int objY, int[][] obj, int type) {
            this.objX = objX;
            this.objY = objY;
            this.obj = obj;
            this.type = type;
        }
    }

    class Block {
        int x, y, type;
        Block(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
}