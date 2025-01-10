package temporal_workflows;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.io.IOException;

@ActivityInterface
public interface FetchStockActivity {
    @ActivityMethod
    String fetchStockData(int sector) throws IOException, InterruptedException;
}
