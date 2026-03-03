import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class settings extends JComponent {

    public static void main(String[] args) {

        JFrame f = new JFrame("settings");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setLayout(new BorderLayout());

        BrightnessPanel panel = new BrightnessPanel();
        panel.setLayout(null);

        JLabel labelBrightness = new JLabel("Яркость:");
        labelBrightness.setFont(new Font("Arial", Font.BOLD, 20));
        labelBrightness.setBounds(100, 250, 120, 30);
        panel.add(labelBrightness);

        JSlider sliderBrightness = new JSlider(0, 100, GlobalSettings.brightness);
        sliderBrightness.setBounds(230, 246, 400, 50);
        panel.add(sliderBrightness);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setBounds(20, 20, 180, 50);
        backButton.setBackground(Color.GREEN);
        backButton.setOpaque(true);
        backButton.setBorderPainted(true);
        panel.add(backButton);

        sliderBrightness.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = sliderBrightness.getValue();
                GlobalSettings.brightness = value;
                panel.setBrightness(value);
            }
        });

        backButton.addActionListener(e -> {
            f.dispose();
            Main.main(new String[]{});  // возвращаемся в меню
        });

        f.add(panel, BorderLayout.CENTER);
        f.setVisible(true);
    }

    static class BrightnessPanel extends JPanel {
        private int brightness = GlobalSettings.brightness;

        public void setBrightness(int value) {
            this.brightness = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Image i1 = Toolkit.getDefaultToolkit()
                    .getImage("C:/Users/maxxb/hey/images/background.png");
            g.drawImage(i1, 0, 0, getWidth(), getHeight(), this);

            int alpha = (int) ((100 - brightness) * 2.55);
            if (alpha > 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}