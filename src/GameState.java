import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// undo機能のために、特定の瞬間のゲーム状態を保存するクラス

public class GameState {
    private final Stack<Card> stockPileSnapshot;
    private final Stack<Card> wastePileSnapshot;
    private final List<Stack<Card>> foundationPilesSnapshot;
    private final List<Stack<Card>> tableauPilesSnapshot;

    public GameState(Stack<Card> stock, Stack<Card> waste,
                     List<Stack<Card>> foundations, List<Stack<Card>> tableaus) {
        this.stockPileSnapshot = deepCopyStack(stock);
        this.wastePileSnapshot = deepCopyStack(waste);
        this.foundationPilesSnapshot = deepCopyListOfStacks(foundations);
        this.tableauPilesSnapshot = deepCopyListOfStacks(tableaus);
    }

    public Stack<Card> getStockPileSnapshot() { return stockPileSnapshot; }
    public Stack<Card> getWastePileSnapshot() { return wastePileSnapshot; }
    public List<Stack<Card>> getFoundationPilesSnapshot() { return foundationPilesSnapshot; }
    public List<Stack<Card>> getTableauPilesSnapshot() { return tableauPilesSnapshot; }

    // Cardのスタックをディープコピーする。
    public static Stack<Card> deepCopyStack(Stack<Card> original) {
        Stack<Card> copy = new Stack<>();
        for (Card card : original) {
            copy.push(new Card(card)); // Cardのコピーコンストラクタを使用
        }
        return copy;
    }

    // Cardのスタックのリストをディープコピーする。
    public static List<Stack<Card>> deepCopyListOfStacks(List<Stack<Card>> originalList) {
        List<Stack<Card>> copyList = new ArrayList<>();
        for (Stack<Card> stack : originalList) {
            copyList.add(deepCopyStack(stack));
        }
        return copyList;
    }
}

