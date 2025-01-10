package temporal_workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface FetchStockWorkflow {
    @WorkflowMethod
    String fetchStockData(int sector);
}
