package temporal_workflows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.DTO.BaseStock;
import common.DTO.Stock;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class EnrichStockActivityImpl implements EnrichStockActivity {
    private final String baseUrl;
    private final String techBaseUrl;
    private final Map<String, String> identifierBaseHeaders;
    private final String fallBackAddition;
    private final String techSuffix;
    private final Map<String, String> techBaseHeaders;

    public EnrichStockActivityImpl(String baseUrl, String techBaseUrl, Map<String, String> identifierBaseHeaders, String fallBackAddition, String techSuffix, Map<String, String> techBaseHeaders) {
        this.baseUrl = baseUrl;
        this.techBaseUrl = techBaseUrl;
        this.identifierBaseHeaders = identifierBaseHeaders;
        this.fallBackAddition = fallBackAddition;
        this.techSuffix = techSuffix;
        this.techBaseHeaders = techBaseHeaders;
    }

    @Override
    public Stock enrichStockData(BaseStock stock) {
        try {
            Stock newStock = null;

            var result = getStockIdentifier(stock.ticker_symbol());
            if (result == null) {
                result = getStockIdentifier(this.fallBackAddition + stock.ticker_symbol());
            }
            if (result != null) {
                String identifier = result.getAsJsonPrimitive("isin").getAsString();
                String otherName = result.getAsJsonPrimitive("companyName").getAsString();
                var techResult = getStockTech(identifier);
                double tech_asssessment = techResult.getAsJsonObject("data").getAsJsonArray("indicators").asList()
                        .get(0).getAsJsonObject().getAsJsonPrimitive("technical_indicator").getAsDouble();
                newStock = new Stock(stock.company_id(), stock.name(), stock.ticker_symbol(), stock.exchange_symbol()
                        , stock.filing_date(), stock.market_cap_before_filing_date(), stock.final_assessment(), stock.buying_recommendation(), identifier, otherName, tech_asssessment);
            }

            return newStock;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private JsonObject getStockIdentifier(String tickerSymbol) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + tickerSymbol))
                .method("GET", HttpRequest.BodyPublishers.noBody());
        AddHeadersToRequestBuilder(requestBuilder, this.identifierBaseHeaders);

        System.out.println("sending enrich request");
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        System.out.println("returning request");
        JsonElement element = new JsonParser().parse(response.body());
        return element.getAsJsonObject().getAsJsonArray("data").get(0).getAsJsonObject();
    }

    private JsonObject getStockTech(String identifier) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(this.techBaseUrl + this.techSuffix + identifier))
                .method("GET", HttpRequest.BodyPublishers.noBody());
        AddHeadersToRequestBuilder(requestBuilder, this.techBaseHeaders);

        System.out.println("sending request");
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        System.out.println("returning request");
        JsonElement element = new JsonParser().parse(response.body());
        return element.getAsJsonObject();
    }

    private void AddHeadersToRequestBuilder(HttpRequest.Builder requestBuilder, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
    }
}
