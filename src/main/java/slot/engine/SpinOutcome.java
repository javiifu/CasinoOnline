package slot.engine;

import slot.model.PayoutDetail;
import slot.model.SpinResult;

public record SpinOutcome(SpinResult result, PayoutDetail payoutDetail) {
}
