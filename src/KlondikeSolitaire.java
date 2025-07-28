import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

/*
   クロンダイクソリティアゲームのメインフレームクラス
   ウィンドウ、ボタン、および各コンポーネント（モデル、ビュー、コントローラ）のセットアップを行う
*/
public class KlondikeSolitaire extends JFrame {

    private GameModel model;
    private GamePanel gamePanel;
    private GameController controller;

    private JPanel glassPane;
    private JButton retryButton;

    public KlondikeSolitaire() {
        // 1. モデルとコントローラ、ビュー（パネル）の初期化
        model = new GameModel();
        // コントローラはモデルとビュー（パネル）への参照を持つ
        // GamePanelのコンストラクタ内でコントローラを初期化
        controller = new GameController(model, null, this);
        gamePanel = new GamePanel(model, controller);
        // GameControllerがGamePanelへの参照を持てるように後から設定
        controller.setPanel(gamePanel);


        // 2. メインウィンドウの設定
        setTitle("クロンダイク・ソリティア");
        setSize(GamePanel.CARD_WIDTH * 9, GamePanel.CARD_HEIGHT * 8);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 3. コンポーネントの配置
        add(gamePanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.NORTH);
        
        // 4. マウスリスナーの設定
        gamePanel.addMouseListener(controller);

        // 5. クリア画面用のGlassPane設定
        setupGlassPane();
    }

    private JPanel createButtonPanel() {
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        topButtonPanel.setBackground(new Color(0, 120, 0)); 
        
        JButton restartButton = new JButton("新しいゲーム");
        restartButton.setBorderPainted(false);
        restartButton.setFocusPainted(false);
        restartButton.setOpaque(false);
        restartButton.setBackground(new Color(0, 120, 0));
        restartButton.setForeground(new Color(255, 255, 255));
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        restartButton.addActionListener(e -> {
            if (glassPane.isVisible()) {
                glassPane.setVisible(false);
                glassPane.removeAll();
            }
            model.initializeGame();
            controller.clearSelection();
            gamePanel.repaint();
        });
        topButtonPanel.add(restartButton);

        JButton undoButton = new JButton("一つ戻る");
        undoButton.setBorderPainted(false);
        undoButton.setFocusPainted(false);
        undoButton.setOpaque(false);
        undoButton.setBackground(new Color(0, 120, 0));
        undoButton.setForeground(new Color(255, 255, 255));
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        undoButton.addActionListener(e -> {
             if (glassPane.isVisible()) {
                glassPane.setVisible(false);
                glassPane.removeAll();
            }
            model.undoLastMove();
            controller.clearSelection();
            gamePanel.repaint();
        });
        topButtonPanel.add(undoButton);

        return topButtonPanel;
    }

    private void setupGlassPane() {
        glassPane = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                if (model.isGameWon()) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(0, 180, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 80));
                    FontMetrics fm = g2d.getFontMetrics();
                    String winText = "クリア!!!";
                    int textWidth = fm.stringWidth(winText);
                    g2d.drawString(winText, (getWidth() - textWidth) / 2, (getHeight() + fm.getAscent()) / 2 - 100);
                }
            }
        };
        glassPane.setOpaque(false);
        glassPane.setVisible(false);
        setGlassPane(glassPane);

        retryButton = new JButton("新しいゲーム");
        retryButton.setBorderPainted(true);
        retryButton.setBorder(new LineBorder(Color.WHITE, 2));
        retryButton.setFocusPainted(false);
        retryButton.setOpaque(false);
        retryButton.setBackground(new Color(0, 120, 0));
        retryButton.setForeground(new Color(255, 255, 255));
        retryButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        retryButton.addActionListener(e -> {
            glassPane.setVisible(false);
            glassPane.removeAll();
            model.initializeGame();
            controller.clearSelection();
            gamePanel.repaint();
        });
    }

    public void showWinScreen() {
        SwingUtilities.invokeLater(() -> {
            glassPane.add(retryButton);
            glassPane.setVisible(true);
            glassPane.revalidate();
            glassPane.repaint();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KlondikeSolitaire game = new KlondikeSolitaire();
            game.setVisible(true);
        });
    }
}

