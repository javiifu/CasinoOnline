package slot.engine;

import java.util.ArrayList;
import java.util.List;
import slot.model.LineMap;
import slot.model.LineWin;
import slot.model.Paytable;
import slot.model.PayoutDetail;
import slot.model.SlotConfig;
import slot.model.SpinResult;
import slot.model.Symbol;

public class PayoutEvaluator {

    public PayoutDetail evaluate(SpinResult result, SlotConfig cfg, Paytable paytable, LineMap lines) {
        List<LineWin> lineWins = new ArrayList<>();

        for (int lineIndex = 0; lineIndex < lines.lineCount(); lineIndex++) {
            int[] line = lines.getLine(lineIndex);
            Symbol[] symbols = new Symbol[5];
            for (int r = 0; r < 5; r++) {
                symbols[r] = result.getAt(line[r], r);
            }

            LineWin win = evaluateLine(symbols, lineIndex, paytable, cfg.betLine());
            if (win != null) {
                lineWins.add(win);
            }
        }

        int scatterCount = countScatter(result);
        int scatterWin = paytable.getScatterPay(scatterCount) * cfg.betLine();
        boolean bonusTriggered = scatterCount >= cfg.bonusTrigger();
        int bonusWin = bonusTriggered ? new BonusEngine().bonusWin(cfg) : 0;

        int total = scatterWin + bonusWin;
        for (LineWin lineWin : lineWins) {
            total += lineWin.win();
        }

        return new PayoutDetail(lineWins, scatterCount, scatterWin, bonusTriggered, bonusWin, total);
    }

    private LineWin evaluateLine(Symbol[] symbols, int lineIndex, Paytable paytable, int betLine) {
        Symbol base = null;
        for (Symbol symbol : symbols) {
            if (symbol.isScatter()) {
                base = null;
                break;
            }
            if (!symbol.isWild()) {
                base = symbol;
                break;
            }
        }
        if (base == null) {
            base = Symbol.WILD;
        }

        int count = 0;
        for (Symbol symbol : symbols) {
            if (symbol.isScatter()) {
                break;
            }
            if (symbol == base || symbol.isWild()) {
                count++;
            } else {
                break;
            }
        }

        int bestPay = 0;
        Symbol bestSymbol = base;
        int basePay = paytable.getPay(base, count);
        if (basePay > 0) {
            bestPay = basePay;
        }

        int wildCount = 0;
        for (Symbol symbol : symbols) {
            if (symbol.isScatter()) {
                break;
            }
            if (symbol.isWild()) {
                wildCount++;
            } else {
                break;
            }
        }
        int wildPay = paytable.getPay(Symbol.WILD, wildCount);
        if (wildPay > bestPay) {
            bestPay = wildPay;
            bestSymbol = Symbol.WILD;
            count = wildCount;
        }

        if (bestPay > 0) {
            int win = bestPay * betLine;
            return new LineWin(lineIndex, bestSymbol, count, bestPay, win);
        }
        return null;
    }

    private int countScatter(SpinResult result) {
        int count = 0;
        Symbol[][] window = result.getWindow();
        for (int row = 0; row < 3; row++) {
            for (int reel = 0; reel < 5; reel++) {
                if (window[row][reel].isScatter()) {
                    count++;
                }
            }
        }
        return count;
    }
}
