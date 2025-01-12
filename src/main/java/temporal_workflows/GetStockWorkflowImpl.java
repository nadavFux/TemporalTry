package temporal_workflows;

import common.DTO.BaseStock;
import common.DTO.Stock;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetStockWorkflowImpl implements GetStockWorkflow {
    private final FetchStockActivity fetchStockActivity;
    private final FilterStockActivity filterStockActivity;
    private final EnrichStockActivity enrichStockActivity;

    public GetStockWorkflowImpl() {
        this.fetchStockActivity = Workflow.newActivityStub(FetchStockActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(1)).build());
        this.filterStockActivity = Workflow.newActivityStub(FilterStockActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(1)).build());
        this.enrichStockActivity = Workflow.newActivityStub(EnrichStockActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(1)).build());
    }

    @Override
    public List<Stock> getStockData(int sector) {
        try {
            var result = this.fetchStockActivity.fetchStockData(sector);
            result = this.filterStockActivity.filterStockData(result);

            List<Promise<Stock>> promises = new ArrayList<>();
            for (BaseStock input : result) {
                promises.add(Async.function(this.enrichStockActivity::enrichStockData, input));
            }
            Promise.allOf(promises).get();

            var results = promises.stream().map(Promise::get).filter(Objects::nonNull).toList();
            
            return results;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
