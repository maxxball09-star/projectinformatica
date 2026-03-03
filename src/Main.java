import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends JComponent {
    private Image a;

    public Main(String b) {
        this.a = Toolkit.getDefaultToolkit().getImage(b);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (a != null) {
            g.drawImage(a, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("Menu");
        f.setLayout(new BorderLayout());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel p = new JPanel() {
            private final Image bg =
                    Toolkit.getDefaultToolkit().getImage("C:/Users/maxxb/hey/images/background.png");

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }

                int brightness = GlobalSettings.brightness;
                int alpha = (int) ((100 - brightness) * 2.55);
                if (alpha > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            }
        };
        p.setLayout(null);

        JButton btn1 = new JButton();
        JButton btn2 = new JButton();
        JButton btn3 = new JButton();

        p.putClientProperty("btn1", btn1);
        p.putClientProperty("btn2", btn2);
        p.putClientProperty("btn3", btn3);

        p.add(btn1);
        p.add(btn2);
        p.add(btn3);

        btn1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                game1.main(new String[]{});
            }
        });

        btn2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                f.dispose();
            }
        });

        btn3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                settings.main(new String[]{});
            }
        });

        f.add(p, BorderLayout.CENTER);

        f.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutButtons(p);
            }
        });

        f.setVisible(true);
        layoutButtons(p);
    }

    private static void layoutButtons(JPanel p) {
        int panelW = p.getWidth();
        int panelH = p.getHeight();
        if (panelW <= 0 || panelH <= 0) return;

        int w = panelW / 5;
        int h = panelH / 10;
        int x = (panelW - w) / 2;
        int y = panelH / 3;
        int s = h / 4;

        JButton btn1 = (JButton) p.getClientProperty("btn1");
        JButton btn2 = (JButton) p.getClientProperty("btn2");
        JButton btn3 = (JButton) p.getClientProperty("btn3");

        if (btn1 == null || btn2 == null || btn3 == null) return;

        setButtonIcon(btn1, "C:/Users/maxxb/hey/images/start.png", w, h);
        setButtonIcon(btn2, "C:/Users/maxxb/hey/images/exit.png", w, h);
        setButtonIcon(btn3, "C:/Users/maxxb/hey/images/settingsbutton.png", w, h);

        btn1.setBounds(x, y, w, h);
        btn2.setBounds(x, y + h + s, w, h);
        btn3.setBounds(x, y + 2 * (h + s), w, h);

        p.revalidate();
        p.repaint();
    }

    private static void setButtonIcon(JButton btn, String imagePath, int w, int h) {
        Image img = Toolkit.getDefaultToolkit().getImage(imagePath);
        Image scaledImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImg);

        btn.setIcon(icon);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
    }
}