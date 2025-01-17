package temporal_workflows;

import common.DTO.BaseStock;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.io.IOException;
import java.util.List;

@ActivityInterface
public interface FetchStockActivity {
    @ActivityMethod
    List<BaseStock> fetchStockData(int sector) throws IOException, InterruptedException;
}
