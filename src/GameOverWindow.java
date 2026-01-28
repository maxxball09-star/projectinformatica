import javax.swing.*;
import java.awt.*;

public class GameOverWindow extends JDialog {

    public GameOverWindow(JFrame parent) {
        super(parent, "Game Over", true);
        setSize(600, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        ImageIcon gameOverIcon = new ImageIcon("gameover.jpg");
        JLabel imageLabel = new JLabel(gameOverIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(imageLabel);
    }
}