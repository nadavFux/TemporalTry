package temporal_workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class FetchStockWorkflowImpl implements FetchStockWorkflow {
    private final FetchStockActivity activity;

    public FetchStockWorkflowImpl() {
        this.activity = Workflow.newActivityStub(FetchStockActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(1)).build());
    }

    @Override
    public String fetchStockData(int sector) {
        try {
            var result = this.activity.fetchStockData(sector);
            return result;
        } catch (Exception e) {
            System.out.println(e);
            return "Failed at " + e;
        }
    }
}
