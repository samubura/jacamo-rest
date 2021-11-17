package wot.java;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class AbstractWotHttpClient implements WotHttpClient {

    private static final String IdentityHeader = "x-agent-id";
    private final HttpClient client;
    private final String agentName;

    public AbstractWotHttpClient(String name){
        this.agentName = name;
        this.client = HttpClients.createDefault();
    }

    protected HttpClient getClient(){
        return this.client;
    }

    @Override
    public JsonElement readProperty(String url) throws WotClientException {
        HttpUriRequest request = getReadRequest(url);
        request.setHeader(IdentityHeader, agentName);
        return executeRequest(request);
    }

    public abstract HttpUriRequest getReadRequest(String url) throws WotClientException;

    @Override
    public JsonElement invokeAction(String url, JsonElement obj) throws WotClientException {
        HttpUriRequest request = getInvokeRequest(url, obj);
        request.setHeader(IdentityHeader, agentName);
        return executeRequest(request);
    }

    public abstract HttpUriRequest getInvokeRequest(String url, JsonElement obj) throws WotClientException;

    private JsonElement executeRequest(HttpUriRequest request) throws WotClientException {
        HttpResponse res;
        int code;
        try {
            res = client.execute(request);
            code = res.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new WotClientException("Request failed with IO exception");
        }
        if(code >= 400) {
            throw new WotClientException("Request failed with status code "+code);
        } else {
            HttpEntity body = res.getEntity();
            try {
                String jsonString = EntityUtils.toString(body, StandardCharsets.UTF_8);
                return new JsonParser().parse(jsonString);
            } catch (IOException e) {
                throw new WotClientException("Failed to parse body");
            }
        }
    }
}

