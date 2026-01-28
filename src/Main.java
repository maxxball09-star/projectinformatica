import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

        JFrame f = new JFrame("Menu");
        f.setLayout(new BorderLayout());

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image b = Toolkit.getDefaultToolkit().getImage("D:/hey/background.png");
                g.drawImage(b, 0, 0, getWidth(), getHeight(), this);
            }
        };
        p.setLayout(null);

        int w = 220;
        int h = 70;
        int x = (800 - w) / 2;
        int y = 180;
        int s = 20;

        JButton btn1 = createButton("D:/hey/start.png", x, y, w, h);
        JButton btn2 = createButton("D:/hey/exit.png", x, y + h + s, w, h);
        JButton btn3 = createButton("D:/hey/settingsbutton.png", x, y + 2 * (h + s), w, h);

        btn1.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    game1.main(new String[]{});
                });
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        btn2.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                f.dispose();
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        btn3.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                settings.main(new String[]{});
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        p.add(btn1);
        p.add(btn2);
        p.add(btn3);

        f.add(p, BorderLayout.CENTER);
        f.setSize(800, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    private static JButton createButton(String imagePath, int x, int y, int w, int h) {
        Image img = Toolkit.getDefaultToolkit().getImage(imagePath);
        Image scaledImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImg);

        JButton btn = new JButton(icon);
        btn.setBounds(x, y, w, h);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(true);

        return btn;
    }
}