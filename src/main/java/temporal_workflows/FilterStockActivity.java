package temporal_workflows;

import common.DTO.BaseStock;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

@ActivityInterface
public interface FilterStockActivity {
    @ActivityMethod
    List<BaseStock> filterStockData(List<BaseStock> stocks);
}
