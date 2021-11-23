package wot.java;

import com.google.gson.JsonElement;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.net.URI;
import java.net.URISyntaxException;


public class StringTokenAuthenticatedHttpClient extends AbstractWotHttpClient {

    private final TokenLocation location;
    private final String tokenName;
    private final String tokenValue;


    public StringTokenAuthenticatedHttpClient(String name, TokenLocation location, String tokenName, String tokenValue){
        super(name);
        this.location = location;
        this.tokenName = tokenName;
        this.tokenValue = tokenValue;
    }

    @Override
    public HttpUriRequest getReadRequest(String url) throws WotClientException {
        HttpGet req;
        try {
            URI uri = new URIBuilder(url).build();
            switch(location) {
                case HEADER:
                    req = new HttpGet(uri);
                    req.addHeader(this.tokenName, this.tokenValue);
                    break;
                case QUERY:
                    URI queryUri = new URIBuilder(uri).
                            addParameter(this.tokenName, this.tokenValue)
                            .build();
                    req = new HttpGet(queryUri);
                    break;
                case BODY:
                case COOKIE:
                default: throw new WotClientException("Unsupported token location");
            }
        } catch (URISyntaxException e){
            throw new WotClientException("URI is not correct");
        }
        return req;
    }

    @Override
    public HttpUriRequest getInvokeRequest(String url, String method, JsonElement obj) throws WotClientException {
        HttpEntityEnclosingRequestBase req;
        switch (method){
            case "PUT": req = new HttpPut(url); break;
            case "POST":
            default: req = new HttpPost(url); break;
        }
        try {
            URI uri = new URIBuilder(url).build();
            switch(location) {
                case HEADER:
                    req.addHeader(this.tokenName, this.tokenValue);
                    if(obj != null) {
                        req.setEntity(new StringEntity(obj.toString(), ContentType.APPLICATION_JSON));
                    }
                    break;
                case QUERY:
                    URI queryUri = new URIBuilder(uri).
                            addParameter(this.tokenName, this.tokenValue)
                            .build();
                    req.setURI(queryUri);
                    if(obj != null) {
                        req.setEntity(new StringEntity(obj.toString(), ContentType.APPLICATION_JSON));
                    }
                    break;
                case BODY:
                case COOKIE:
                default: throw new WotClientException("Unsupported token location");
            }
        } catch (URISyntaxException e){
            throw new WotClientException("URI is not correct");
        }
        return req;
    }
}
