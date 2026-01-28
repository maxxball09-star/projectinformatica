import javax.swing.*;
import java.awt.*;

public class settings extends JComponent {
    public static void main(String[] args) {
        JFrame f = new JFrame("settings");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 600);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image i1 = Toolkit.getDefaultToolkit().getImage("D:/hey/settings.png");
                g.drawImage(i1, 0, 0, 800, 600, this);
            }
        };

        panel.setLayout(new BorderLayout());
        f.add(panel);
        f.setVisible(true);

    }

}