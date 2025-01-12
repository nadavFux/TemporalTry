package project;

import common.DTO.Stock;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
import temporal_workflows.*;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    static final List<Integer> sectors = Arrays.asList(
            10,
            15,
            20,
            25,
            30,
            35,
            40,
            45,
            50,
            55,
            60
    );
    static final String queueName = "EntryTaskQueue";

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Stock Analyzer...");

        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        ConnectWorker(factory);

        factory.start();

        var finalResults = ExecuteWorkflow(client);

        SaveResults(finalResults, "results.txt");

        System.out.println("Stock Analyzer Initialized.");
    }

    private static void ConnectWorker(WorkerFactory factory) {
        var worker = factory.newWorker(queueName);
        worker.registerWorkflowImplementationTypes(GetStockWorkflowImpl.class);

        //TODO: Many things to keep in some organized config
        HashMap<String, String> headers = new HashMap<>();
        headers.put("api-key", "nono");
        headers.put("date-format", "epoch");
        String baseURL = "nono";
        worker.registerActivitiesImplementations(new FetchStockActivityImpl(baseURL, "{code}", headers));

        List<String> exchanges = List.of("TASE", "NYSE", "NasdaqGS");
        worker.registerActivitiesImplementations(new FilterStockActivityImpl(exchanges, 1000000000, 80));

        HashMap<String, String> identifyHeaders = new HashMap<>();
        identifyHeaders.put("Referer", "nono");
        identifyHeaders.put("authorization", "nono");
        worker.registerActivitiesImplementations(new EnrichStockActivityImpl("nono", "nono", identifyHeaders, "IL-", "identifier=", headers));
    }

    private static List<Stock> ExecuteWorkflow(WorkflowClient client) {
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(queueName)
                .setWorkflowRunTimeout(Duration.ofMinutes(10))
                .build();

        List<CompletableFuture<List<Stock>>> results = new ArrayList<>();
        for (int sector : sectors) {
            GetStockWorkflow workflow = client.newWorkflowStub(GetStockWorkflow.class, options);
            results.add(WorkflowClient.execute(workflow::getStockData, sector));
        }
        return results.stream().map(CompletableFuture::join).flatMap(List::stream).toList();
    }

    private static void SaveResults(List<Stock> finalResults, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(finalResults);
        oos.close();
    }

    private static List<Stock> ReadResults(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<Stock> stocks = (List<Stock>) ois.readObject();
        ois.close();
        return stocks;
    }
}
