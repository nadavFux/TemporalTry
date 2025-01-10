package temporal_workflows;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class FetchStockActivityImpl implements FetchStockActivity {
    private final String baseUrl;
    private final Map<String, String> baseHeaders;
    private final String replacementTemplate;

    public FetchStockActivityImpl(String baseUrl, String replacementTemplate, Map<String, String> baseHeaders) {
        this.baseUrl = baseUrl;
        this.baseHeaders = baseHeaders;
        this.replacementTemplate = replacementTemplate;
    }

    @Override
    public String fetchStockData(int sector) throws IOException, InterruptedException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl.replace(this.replacementTemplate, String.valueOf(sector))))
                .method("GET", HttpRequest.BodyPublishers.noBody());

        AddHeadersToRequestBuilder(requestBuilder);
        System.out.println("sending request");
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        System.out.println("returning request");
        var returnval = new JsonParser().parse(response.body());
        System.out.println("exiting");
        return returnval.toString();


    }

    private void AddHeadersToRequestBuilder(HttpRequest.Builder requestBuilder) {
        for (Map.Entry<String, String> entry : this.baseHeaders.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
    }
}
