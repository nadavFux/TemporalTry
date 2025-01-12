package temporal_workflows;

import common.DTO.BaseStock;
import common.DTO.Stock;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface EnrichStockActivity {
    @ActivityMethod
    Stock enrichStockData(BaseStock result);
}
