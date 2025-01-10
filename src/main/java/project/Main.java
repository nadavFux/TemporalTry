package project;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import temporal_workflows.FetchStockActivityImpl;
import temporal_workflows.FetchStockWorkflow;
import temporal_workflows.FetchStockWorkflowImpl;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Entry point for the Stock Analyzer Java Project
        System.out.println("Starting Stock Analyzer...");
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        ArrayList<Worker> workers = new ArrayList<Worker>();
        workers.add(factory.newWorker("EntryTaskQueue"));


        for (Worker worker : workers) {
            worker.registerWorkflowImplementationTypes(FetchStockWorkflowImpl.class);
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("api-key", "nono");
            headers.put("date-format", "epoch");
            String baseURL = "nono";
            worker.registerActivitiesImplementations(new FetchStockActivityImpl(baseURL, "{code}", headers));
        }
        factory.start();

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId("StockFetcherWorkflow")
                .setTaskQueue("EntryTaskQueue")
                .setWorkflowRunTimeout(Duration.ofMinutes(10))
                .build();

        FetchStockWorkflow workflow = client.newWorkflowStub(FetchStockWorkflow.class, options);
        workflow.fetchStockData(10);

        //WorkflowClient.start(workflow::fetchStockData, 10);


        // TODO: 1. Fetch stock data using Temporal workflows
        // TODO: 2. Process data with Flink for real-time insights
        // TODO: 3. Perform analytics with Spark for historical trends
        // TODO: 4. Display processed data

        System.out.println("Stock Analyzer Initialized.");
    }
}
