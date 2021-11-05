package wot;
import cartago.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WoTHttpClient extends Artifact{

    private HttpClient client;
    void init() {
        client = HttpClients.createDefault();
    }

    @OPERATION
    void readProperty(String url, OpFeedbackParam<JsonElement> result) {
        HttpGet request = new HttpGet(url);
        result.set(executeRequest(request));

    }

    @OPERATION
    void invokeAction(String url, JsonElement obj, OpFeedbackParam<JsonElement> result) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(obj.toString(), ContentType.APPLICATION_JSON));
        result.set(executeRequest(request));
    }

    private JsonElement executeRequest(HttpUriRequest request) {
        HttpResponse res = null;
        int code = 500;
        try {
            res = client.execute(request);
            code = res.getStatusLine().getStatusCode();
        } catch (IOException e) {
            failed("Request failed with IO exception");
        }
        if(code >= 400) {
            failed("Request failed with status code "+code, "http_status", code);
        } else {
            HttpEntity body = res.getEntity();
            try {
                String jsonString = EntityUtils.toString(body, StandardCharsets.UTF_8);
                return new JsonParser().parse(jsonString);
            } catch (IOException e) {
                failed("Failed to parse body");
            }
        }
        //unreachable so it doesn't matter
        return null;
    }
}
