package slot.engine;

import slot.model.LineMap;
import slot.model.Paytable;
import slot.model.PayoutDetail;
import slot.model.SlotConfig;
import slot.model.SpinResult;

public class GameService {
    private final SpinEngine spinEngine;
    private final PayoutEvaluator payoutEvaluator;

    public GameService(SpinEngine spinEngine, PayoutEvaluator payoutEvaluator) {
        this.spinEngine = spinEngine;
        this.payoutEvaluator = payoutEvaluator;
    }

    public SpinOutcome spin(SlotConfig config) {
        SpinResult result = spinEngine.spin(config);
        Paytable paytable = config.paytable();
        LineMap lineMap = config.lineMap();
        PayoutDetail detail = payoutEvaluator.evaluate(result, config, paytable, lineMap);
        return new SpinOutcome(result, detail);
    }
}
