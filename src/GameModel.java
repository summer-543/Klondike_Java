import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

// クロンダイクのゲームロジックとデータ全体を管理するクラス
public class GameModel {

    private Stack<Card> stockPile;
    private Stack<Card> wastePile;
    private List<Stack<Card>> foundationPiles;
    private List<Stack<Card>> tableauPiles;
    private boolean gameWon = false;

    private Stack<GameState> undoStack;

    public GameModel() {
        initializeGame();
    }

    public void initializeGame() {
        List<Card> deck = createShuffledDeck();

        stockPile = new Stack<>();
        wastePile = new Stack<>();
        foundationPiles = new ArrayList<>();
        tableauPiles = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            foundationPiles.add(new Stack<>());
        }

        for (int i = 0; i < 7; i++) {
            Stack<Card> pile = new Stack<>();
            for (int j = 0; j <= i; j++) {
                Card card = deck.remove(0);
                if (j == i) {
                    card.setFaceUp(true);
                }
                pile.push(card);
            }
            tableauPiles.add(pile);
        }

        stockPile.addAll(deck);
        gameWon = false;

        undoStack = new Stack<>();
        saveCurrentState();
    }

    private List<Card> createShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                deck.add(new Card(suit, ranks[i], i + 1));
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    public void saveCurrentState() {
        undoStack.push(new GameState(stockPile, wastePile, foundationPiles, tableauPiles));
    }

    public void undoLastMove() {
        if (undoStack.size() > 1) {
            undoStack.pop();
            GameState previousState = undoStack.peek();

            this.stockPile = GameState.deepCopyStack(previousState.getStockPileSnapshot());
            this.wastePile = GameState.deepCopyStack(previousState.getWastePileSnapshot());
            this.foundationPiles = GameState.deepCopyListOfStacks(previousState.getFoundationPilesSnapshot());
            this.tableauPiles = GameState.deepCopyListOfStacks(previousState.getTableauPilesSnapshot());

            this.gameWon = false;
        } else {
            System.out.println("これ以上戻れません。");
        }
    }

    public void checkWinCondition() {
        for (Stack<Card> foundation : foundationPiles) {
            if (foundation.size() != 13) {
                this.gameWon = false;
                return;
            }
        }
        this.gameWon = true;
    }

    public Stack<Card> getStockPile() { return stockPile; }
    public Stack<Card> getWastePile() { return wastePile; }
    public List<Stack<Card>> getFoundationPiles() { return foundationPiles; }
    public List<Stack<Card>> getTableauPiles() { return tableauPiles; }
    public boolean isGameWon() { return gameWon; }
}
