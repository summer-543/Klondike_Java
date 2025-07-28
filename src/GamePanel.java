import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

// ゲームの盤面を描画するためのJPanel
public class GamePanel extends JPanel {

    private final GameModel model;
    private final GameController controller;
    private Image img1;
    
    // 定数
    public static final int CARD_WIDTH = 91;
    public static final int CARD_HEIGHT = 130;
    public static final int CARD_MARGIN_X = 10;
    public static final int CARD_MARGIN_Y = 20;

    public GamePanel(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        setBackground(new Color(0, 120, 0)); // 緑色のテーブル
        
        try {
			this.img1 = ImageIO.read(new File("回転矢印.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 山札
        int stockX = CARD_MARGIN_X;
        int stockY = CARD_MARGIN_Y;
        if (!model.getStockPile().isEmpty()) {
            model.getStockPile().peek().draw(g2d, stockX, stockY, CARD_WIDTH, CARD_HEIGHT);
        } else {
            drawEmptyPileSlot(g2d, stockX, stockY);
            g2d.drawImage(img1, stockX + 15, stockY + 30, CARD_WIDTH - 30, CARD_HEIGHT - 60, this);
        }

        // 捨て札
        int wasteX = stockX + CARD_WIDTH + CARD_MARGIN_X;
        int wasteY = CARD_MARGIN_Y;
        if (!model.getWastePile().isEmpty()) {
            model.getWastePile().peek().draw(g2d, wasteX, wasteY, CARD_WIDTH, CARD_HEIGHT);
        } else {
            drawEmptyPileSlot(g2d, wasteX, wasteY);
        }

        // 組札
        for (int i = 0; i < 4; i++) {
            int foundationX = (wasteX + CARD_WIDTH + CARD_MARGIN_X) + (i + 1) * (CARD_WIDTH + CARD_MARGIN_X);
            int foundationY = CARD_MARGIN_Y;
            if (!model.getFoundationPiles().get(i).isEmpty()) {
                model.getFoundationPiles().get(i).peek().draw(g2d, foundationX, foundationY, CARD_WIDTH, CARD_HEIGHT);
            } else {
                drawEmptyPileSlot(g2d, foundationX, foundationY);
            }
        }

        // 場札
        for (int i = 0; i < 7; i++) {
            int tableauX = CARD_MARGIN_X + i * (CARD_WIDTH + CARD_MARGIN_X);
            int tableauY = CARD_MARGIN_Y + CARD_HEIGHT + CARD_MARGIN_Y;
            Stack<Card> pile = model.getTableauPiles().get(i);
            if (pile.isEmpty()) {
                drawEmptyPileSlot(g2d, tableauX, tableauY);
            } else {
                for (int j = 0; j < pile.size(); j++) {
                    Card card = pile.get(j);
                    card.draw(g2d, tableauX, tableauY + j * 25, CARD_WIDTH, CARD_HEIGHT);
                }
            }
        }

        // 選択されたカードのハイライト
        if (controller.getSelectedCard() != null && controller.getSelectedCardSourceLocation() != null) {
            Point loc = controller.getSelectedCardSourceLocation();
            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(loc.x, loc.y, CARD_WIDTH, CARD_HEIGHT, 10, 10);
            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawEmptyPileSlot(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(0, 80, 0));
        g2d.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);
    }
}

