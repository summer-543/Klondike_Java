import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// ユーザーからのマウス入力を処理し、モデルとビューを制御するクラス
public class GameController extends MouseAdapter {

    private final GameModel model;
    private GamePanel panel;
    private final KlondikeSolitaire mainFrame;

    // 選択中のカード情報
    private Card selectedCard = null;
    private Point selectedCardSourceLocation = null;
    private int selectedCardSourcePileIndex = -1;
    private String selectedCardSourcePileType = "";

    public GameController(GameModel model, GamePanel panel, KlondikeSolitaire mainFrame) {
        this.model = model;
        this.panel = panel;
        this.mainFrame = mainFrame;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (model.isGameWon()) return;

        int clickX = e.getX();
        int clickY = e.getY();

        if (e.getClickCount() == 2) {
            handleDoubleClick(clickX, clickY);
            return;
        }

        // 山札のクリック
        Rectangle stockBounds = new Rectangle(GamePanel.CARD_MARGIN_X, GamePanel.CARD_MARGIN_Y, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT);
        if (stockBounds.contains(clickX, clickY)) {
            handleStockClick(); // 通常の山札クリック処理
            return;
        }

        // 捨て札のクリック
        int wasteX = GamePanel.CARD_MARGIN_X + GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X;
        Rectangle wasteBounds = new Rectangle(wasteX, GamePanel.CARD_MARGIN_Y, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT);
        if (wasteBounds.contains(clickX, clickY)) {
            handleWasteClick(wasteX, GamePanel.CARD_MARGIN_Y);
            return;
        }

        // 組札のクリック
        if (handleFoundationClick(clickX, clickY, wasteX)) return;

        // 場札のクリック
        if (handleTableauClick(clickX, clickY)) return;

        clearSelection();
        panel.repaint();
    }
    
    private void handleStockClick() {
        if (!model.getStockPile().isEmpty()) {
            Card drawnCard = model.getStockPile().pop();
            drawnCard.setFaceUp(true);
            model.getWastePile().push(drawnCard);
            model.saveCurrentState();
        } else if (!model.getWastePile().isEmpty()) {
            while (!model.getWastePile().isEmpty()) {
                Card card = model.getWastePile().pop();
                card.setFaceUp(false);
                model.getStockPile().push(card);
            }
            model.saveCurrentState();
        }
        clearSelection();
        panel.repaint();
        checkWinAndRepaint();
    }

    private void handleWasteClick(int x, int y) {
        if (!model.getWastePile().isEmpty()) {
            if (selectedCard == model.getWastePile().peek()) {
                clearSelection();
            } else {
                selectCard(model.getWastePile().peek(), new Point(x, y), -1, "waste");
            }
            panel.repaint();
        }
    }

    private boolean handleFoundationClick(int clickX, int clickY, int wasteX) {
        for (int i = 0; i < 4; i++) {
            int foundationX = (wasteX + GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X) + (i + 1) * (GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X);
            Rectangle foundationBounds = new Rectangle(foundationX, GamePanel.CARD_MARGIN_Y, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT);
            if (foundationBounds.contains(clickX, clickY)) {
                Stack<Card> destinationPile = model.getFoundationPiles().get(i);
                if (selectedCard != null) {
                    // 選択中のカードがある場合、組札への移動を試みる
                    if (canMoveToFoundation(selectedCard, destinationPile)) {
                        moveSelectedCard(destinationPile);
                        model.saveCurrentState();
                        checkWinAndRepaint();
                    } else {
                        clearSelection(); // 無効な移動の場合、選択を解除
                    }
                } else if (!destinationPile.isEmpty()) {
                    // 選択中のカードがない場合、組札のカードを選択する
                    selectCard(destinationPile.peek(), new Point(foundationX, GamePanel.CARD_MARGIN_Y), i, "foundation");
                }
                panel.repaint();
                return true;
            }
        }
        return false;
    }

    private boolean handleTableauClick(int clickX, int clickY) {
        for (int i = 0; i < 7; i++) {
            int tableauX = GamePanel.CARD_MARGIN_X + i * (GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X);
            int tableauY = GamePanel.CARD_MARGIN_Y + GamePanel.CARD_HEIGHT + GamePanel.CARD_MARGIN_Y;
            Stack<Card> pile = model.getTableauPiles().get(i);

            int pileHeight = pile.isEmpty() ? GamePanel.CARD_HEIGHT : (pile.size() - 1) * 25 + GamePanel.CARD_HEIGHT;
            Rectangle pileBounds = new Rectangle(tableauX, tableauY, GamePanel.CARD_WIDTH, pileHeight);

            if (pileBounds.contains(clickX, clickY)) {
                if (selectedCard != null) {
                    // 選択中のカードがある場合、場札への移動を試みる
                    if (canMoveToTableau(selectedCard, pile)) {
                        moveSelectedCard(pile);
                        model.saveCurrentState();
                        checkWinAndRepaint();
                    } else {
                        // 無効な移動の場合、選択を解除し、クリックした場札のカードを選択し直す
                        clearSelection();
                        selectClickedCardInTableau(clickX, clickY, i);
                    }
                } else {
                    // 選択中のカードがない場合、クリックした場札のカードを選択する
                    selectClickedCardInTableau(clickX, clickY, i);
                }
                panel.repaint();
                return true;
            }
        }
        return false;
    }

    private void selectClickedCardInTableau(int clickX, int clickY, int pileIndex) {
        Stack<Card> pile = model.getTableauPiles().get(pileIndex);
        if (pile.isEmpty()) return;

        int tableauX = GamePanel.CARD_MARGIN_X + pileIndex * (GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X);
        int tableauY = GamePanel.CARD_MARGIN_Y + GamePanel.CARD_HEIGHT + GamePanel.CARD_MARGIN_Y;

        for (int j = pile.size() - 1; j >= 0; j--) {
            Card card = pile.get(j);
            if (!card.isFaceUp()) continue;

            int cardY = tableauY + j * 25;
            Rectangle cardBounds = (j == pile.size() - 1)
                ? new Rectangle(tableauX, cardY, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT)
                : new Rectangle(tableauX, cardY, GamePanel.CARD_WIDTH, 25);

            if (cardBounds.contains(clickX, clickY)) {
                selectCard(card, new Point(tableauX, cardY), pileIndex, "tableau");
                return;
            }
        }
    }

    private void handleDoubleClick(int clickX, int clickY) {
        Card clickedCard = null;
        String clickedCardSourceType = "";
        int clickedCardSourceIndex = -1;
        Point clickedCardSourceLocation = null;

        // 1. 捨て札の確認
        int wasteX = GamePanel.CARD_MARGIN_X + GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X;
        Rectangle wasteBounds = new Rectangle(wasteX, GamePanel.CARD_MARGIN_Y, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT);
        if (wasteBounds.contains(clickX, clickY) && !model.getWastePile().isEmpty()) {
            clickedCard = model.getWastePile().peek();
            clickedCardSourceType = "waste";
            clickedCardSourceIndex = -1;
            clickedCardSourceLocation = new Point(wasteX, GamePanel.CARD_MARGIN_Y);
        }

        // 2. 場札の確認
        if (clickedCard == null) {
            for (int i = 0; i < 7; i++) {
                Stack<Card> pile = model.getTableauPiles().get(i);
                if (!pile.isEmpty()) {
                    int tableauX = GamePanel.CARD_MARGIN_X + i * (GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X);
                    int tableauY = GamePanel.CARD_MARGIN_Y + GamePanel.CARD_HEIGHT + GamePanel.CARD_MARGIN_Y;
                    for (int j = pile.size() - 1; j >= 0; j--) {
                        Card card = pile.get(j);
                        if (card.isFaceUp()) {
                            int cardY = tableauY + j * 25;
                            Rectangle cardBounds = new Rectangle(tableauX, cardY, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT);
                            if (cardBounds.contains(clickX, clickY)) {
                                clickedCard = card;
                                clickedCardSourceType = "tableau";
                                clickedCardSourceIndex = i;
                                clickedCardSourceLocation = new Point(tableauX, cardY);
                                break;
                            }
                        }
                    }
                }
                if (clickedCard != null) break;
            }
        }
        
        // 3. 組札の確認
        if (clickedCard == null) {
            for (int i = 0; i < 4; i++) {
                int foundationX = (wasteX + GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X) + (i + 1) * (GamePanel.CARD_WIDTH + GamePanel.CARD_MARGIN_X);
                Rectangle foundationBounds = new Rectangle(foundationX, GamePanel.CARD_MARGIN_Y, GamePanel.CARD_WIDTH, GamePanel.CARD_HEIGHT);
                if (foundationBounds.contains(clickX, clickY) && !model.getFoundationPiles().get(i).isEmpty()) {
                    clickedCard = model.getFoundationPiles().get(i).peek();
                    clickedCardSourceType = "foundation";
                    clickedCardSourceIndex = i;
                    clickedCardSourceLocation = new Point(foundationX, GamePanel.CARD_MARGIN_Y);
                    break;
                }
            }
        }
        
        if (clickedCard != null) {
            // 一時的にカードを選択する
            selectCard(clickedCard, clickedCardSourceLocation, clickedCardSourceIndex, clickedCardSourceType);

            // 組札への移動を試みる
            // クリックされたカードが組札からの場合、組札への移動は試みない
            if (!clickedCardSourceType.equals("foundation")) {
            	for (int i = 0; i < 4; i++) {
	                Stack<Card> foundationPile = model.getFoundationPiles().get(i);
	                if (canMoveToFoundation(clickedCard, foundationPile)) {
	                    moveSelectedCard(foundationPile);
	                    model.saveCurrentState();
	                    checkWinAndRepaint();
	                    return;
	                }
            	}
            }
            
            // 場札への移動を試みる
            for (int i = 0; i < 7; i++) {
                if (clickedCardSourceType.equals("tableau") && clickedCardSourceIndex == i) continue;
                Stack<Card> tableauPile = model.getTableauPiles().get(i);
                if (canMoveToTableau(clickedCard, tableauPile)) {
                    moveSelectedCard(tableauPile);
                    model.saveCurrentState();
                    checkWinAndRepaint();
                    return;
                }
            }
        }
        
        clearSelection();
        panel.repaint();
    }

    private void moveSelectedCard(Stack<Card> destinationPile) {
        if (selectedCard == null) return;

        List<Card> cardsToMove = new ArrayList<>();
        Stack<Card> sourcePile = null;

        switch (selectedCardSourcePileType) {
            case "waste":
                sourcePile = model.getWastePile();
                if (!sourcePile.isEmpty() && sourcePile.peek() == selectedCard) {
                    cardsToMove.add(sourcePile.pop());
                }
                break;
            case "foundation":
                sourcePile = model.getFoundationPiles().get(selectedCardSourcePileIndex);
                if (!sourcePile.isEmpty() && sourcePile.peek() == selectedCard) {
                    cardsToMove.add(sourcePile.pop());
                }
                break;
            case "tableau":
                sourcePile = model.getTableauPiles().get(selectedCardSourcePileIndex);
                int index = sourcePile.indexOf(selectedCard);
                if (index != -1) {
                    while (sourcePile.size() > index) {
                        cardsToMove.add(sourcePile.remove(index));
                    }
                }
                break;
        }

        if (cardsToMove.isEmpty()) {
            clearSelection();
            return;
        }

        destinationPile.addAll(cardsToMove);

        // 場札から移動した場合、元の山札の一番上のカードを表向きにする
        if ("tableau".equals(selectedCardSourcePileType) && sourcePile != null && !sourcePile.isEmpty() && !sourcePile.peek().isFaceUp()) {
            sourcePile.peek().setFaceUp(true);
        }

        clearSelection();
    }

    private boolean canMoveToTableau(Card cardToMove, Stack<Card> destinationPile) {
        if (destinationPile.isEmpty()) {
            return cardToMove.getRank().equals("K");
        } else {
            Card topCard = destinationPile.peek();
            return topCard.isFaceUp() && cardToMove.getValue() == topCard.getValue() - 1 && (cardToMove.isRed() != topCard.isRed());
        }
    }

    private boolean canMoveToFoundation(Card cardToMove, Stack<Card> destinationPile) {
        // 場札から移動する場合、選択されたカードが一番上にあることを確認
        if (selectedCardSourcePileType.equals("tableau")) {
            Stack<Card> sourcePile = model.getTableauPiles().get(selectedCardSourcePileIndex);
            if (sourcePile.peek() != selectedCard) {
                return false;
            }
        }
        // 捨て札から移動する場合、選択されたカードが一番上にあることを確認
        if (selectedCardSourcePileType.equals("waste")) {
             if (model.getWastePile().peek() != selectedCard) {
                return false;
            }
        }

        if (destinationPile.isEmpty()) {
            return cardToMove.getRank().equals("A");
        } else {
            Card topCard = destinationPile.peek();
            return cardToMove.getSuit().equals(topCard.getSuit()) && cardToMove.getValue() == topCard.getValue() + 1;
        }
    }

    private void checkWinAndRepaint() {
        model.checkWinCondition();
        if (model.isGameWon()) {
            mainFrame.showWinScreen();
        }
        panel.repaint();
    }

    private void selectCard(Card card, Point sourceLocation, int pileIndex, String pileType) {
        this.selectedCard = card;
        this.selectedCardSourceLocation = sourceLocation;
        this.selectedCardSourcePileIndex = pileIndex;
        this.selectedCardSourcePileType = pileType;
    }

    public void clearSelection() {
        this.selectedCard = null;
        this.selectedCardSourceLocation = null;
        this.selectedCardSourcePileIndex = -1;
        this.selectedCardSourcePileType = "";
    }

    public Card getSelectedCard() { return selectedCard; }
    public Point getSelectedCardSourceLocation() { return selectedCardSourceLocation; }
    
    public void setPanel(GamePanel panel) {
    	this.panel = panel;
    }
}
