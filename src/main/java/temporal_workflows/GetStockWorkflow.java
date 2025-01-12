package temporal_workflows;

import common.DTO.Stock;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface GetStockWorkflow {
    @WorkflowMethod
    List<Stock> getStockData(int sector);
}
