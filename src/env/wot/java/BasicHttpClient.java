package wot.java;

import com.google.gson.JsonElement;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class BasicHttpClient extends AbstractWotHttpClient {

    @Override
    public HttpUriRequest getReadRequest(String url) {
        return new HttpGet(url);
    }

    @Override
    public HttpUriRequest getInvokeRequest(String url, JsonElement obj) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(obj.toString(), ContentType.APPLICATION_JSON));
        return request;
    }
}
