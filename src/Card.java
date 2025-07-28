import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

// ソリティアのカード一枚を表すクラス
public class Card {
    private final String suit;
    private final String rank;
    private final int value; // A=1, K=13
    private boolean faceUp;
    private Image img1;

    public Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
        this.faceUp = false;
        
        try {
			this.img1 = ImageIO.read(new File("nagareboshi.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    // コピー用コンストラクタ
    public Card(Card other) {
        this.suit = other.suit;
        this.rank = other.rank;
        this.value = other.value;
        this.faceUp = other.faceUp;
        this.img1 = other.img1;
    }

    public String getSuit() { return suit; }
    public String getRank() { return rank; }
    public int getValue() { return value; }
    public boolean isFaceUp() { return faceUp; }
    public void setFaceUp(boolean faceUp) { this.faceUp = faceUp; }

    public boolean isRed() {
        return suit.equals("Hearts") || suit.equals("Diamonds");
    }

    public boolean isBlack() {
        return suit.equals("Clubs") || suit.equals("Spades");
    }

 
    // カードを描画する
    public void draw(Graphics2D g2d, int x, int y, int width, int height) {
        if (faceUp) {
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(x, y, width, height, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x, y, width, height, 10, 10);

            if (isRed()) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLACK);
            }
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString(rank, x + 5, y + 20);
            g2d.drawString(getSuitSymbol(), x + 5, y + 40);
        } else {
            g2d.setColor(new Color(0, 0, 50)); // 裏面の色
            g2d.fillRoundRect(x, y, width, height, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x, y, width, height, 10, 10);
            g2d.drawImage(img1, x + 5, y + 10, width - 8, height - 15, null); // 裏面の模様
        }
    }

    private String getSuitSymbol() {
        switch (suit) {
            case "Hearts": return "♥";
            case "Diamonds": return "♦";
            case "Clubs": return "♣";
            case "Spades": return "♠";
            default: return "";
        }
    }
}

