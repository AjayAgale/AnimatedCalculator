import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AnimatedCalculator extends JFrame implements ActionListener {

    private JTextField display;
    private JPanel buttonPanel;
    private String currentInput = "";
    private String operator = "";
    private double result = 0;
    private boolean isResultShown = false;
    private Color gradientStart = new Color(0x2980b9);
    private Color gradientEnd = new Color(0x6dd5fa);
    private float gradientOffset = 0.0f;
    private Timer animationTimer;

    // History
    private DefaultListModel<String> historyModel = new DefaultListModel<>();
    private JList<String> historyList;

    public AnimatedCalculator() {
        setTitle("Animated Calculator");
        setSize(600, 520); // increased width for history panel
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel animatedBackgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();

                float offset = (float) Math.abs(Math.sin(gradientOffset));
                Color intermediate = blendColors(gradientStart, gradientEnd, offset);

                GradientPaint gp = new GradientPaint(0, 0, intermediate, width, height, Color.BLACK, true);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        animatedBackgroundPanel.setLayout(new BorderLayout());
        setContentPane(animatedBackgroundPanel);

        display = new JTextField();
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setBackground(Color.WHITE);
        display.setForeground(Color.BLACK);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        animatedBackgroundPanel.add(display, BorderLayout.NORTH);

        buttonPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        buttonPanel.setOpaque(false);

        String[] buttons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "C"
        };

        for (String label : buttons) {
            JButton btn = new JButton(label);
            btn.setFont(new Font("Arial", Font.BOLD, 24));
            btn.setFocusable(false);
            btn.setBackground(new Color(240, 240, 240));
            btn.setForeground(Color.BLACK);
            btn.addActionListener(this);
            buttonPanel.add(btn);
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));
        centerPanel.add(buttonPanel, BorderLayout.CENTER);

        JLabel nameLabel = new JLabel("Ajay Agale", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centerPanel.add(nameLabel, BorderLayout.SOUTH);

        animatedBackgroundPanel.add(centerPanel, BorderLayout.CENTER);

        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setPreferredSize(new Dimension(180, 0));
        historyScroll.setBorder(BorderFactory.createTitledBorder("History"));
        animatedBackgroundPanel.add(historyScroll, BorderLayout.EAST);

        animationTimer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gradientOffset += 0.03f;
                repaint();
            }
        });
        animationTimer.start();
    }

    private Color blendColors(Color c1, Color c2, float offset) {
        int r = (int) ((1 - offset) * c1.getRed() + offset * c2.getRed());
        int g = (int) ((1 - offset) * c1.getGreen() + offset * c2.getGreen());
        int b = (int) ((1 - offset) * c1.getBlue() + offset * c2.getBlue());
        return new Color(r, g, b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.matches("[0-9.]")) {
            if (cmd.equals(".") && currentInput.contains(".")) return;

            if (isResultShown) {
                display.setText("");
                currentInput = "";
                result = 0;
                operator = "";
                isResultShown = false;
            }

            currentInput += cmd;
            display.setText(display.getText() + cmd);
        }

        else if (cmd.matches("[+\\-*/]")) {
            if (!currentInput.isEmpty()) {
                double number = Double.parseDouble(currentInput);

                if (isResultShown) {
                    isResultShown = false;
                }

                if (operator.isEmpty()) {
                    result = number;
                } else {
                    result = performOperation(result, number, operator);
                }

                operator = cmd;
                display.setText(clean(result) + " " + operator + " ");
                currentInput = "";
            }

            else if (display.getText().matches(".* [\\+\\-\\*/] $")) {
                String updated = display.getText().substring(0, display.getText().length() - 3);
                display.setText(updated + " " + cmd + " ");
                operator = cmd;
            }

            else if (isResultShown) {
                display.setText(clean(result) + " " + cmd + " ");
                operator = cmd;
                isResultShown = false;
            }
        }

        else if (cmd.equals("=")) {
            if (!currentInput.isEmpty() && !operator.isEmpty()) {
                double secondOperand = Double.parseDouble(currentInput);
                result = performOperation(result, secondOperand, operator);
                String expression = display.getText() + " = " + clean(result);
                display.setText(expression);
                currentInput = String.valueOf(result);
                operator = "";
                isResultShown = true;

                historyModel.addElement(expression);
                if (historyModel.size() > 100) {
                    historyModel.remove(0);
                }
            }
        }

        else if (cmd.equals("C")) {
            currentInput = "";
            operator = "";
            result = 0;
            display.setText("");
            isResultShown = false;
        }
    }

    private double performOperation(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return (b == 0) ? 0 : a / b;
            default: return b;
        }
    }

    private String clean(double num) {
        if (num == (long) num)
            return String.format("%d", (long) num);
        else
            return String.valueOf(num);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AnimatedCalculator calc = new AnimatedCalculator();
                calc.setVisible(true);
            }
        });
    }
}
