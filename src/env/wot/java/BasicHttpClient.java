package wot.java;

import com.google.gson.JsonElement;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class BasicHttpClient extends AbstractWotHttpClient {

    public BasicHttpClient(String name) {
        super(name);
    }

    @Override
    public HttpUriRequest getReadRequest(String url) {
        HttpGet request = new HttpGet(url);
        return request;
    }

    @Override
    public HttpUriRequest getInvokeRequest(String url, String method, JsonElement obj) {
        HttpEntityEnclosingRequestBase req;
        switch (method){
            case "PUT": req = new HttpPut(url); break;
            case "POST":
            default: req = new HttpPost(url); break;
        }
        if(obj != null) {
            req.setEntity(new StringEntity(obj.toString(), ContentType.APPLICATION_JSON));
        }
        return req;
    }
}
