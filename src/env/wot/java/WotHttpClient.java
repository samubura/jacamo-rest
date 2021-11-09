package wot.java;

import com.google.gson.JsonElement;

public interface WotHttpClient {
    JsonElement readProperty(String url) throws WotClientException;

    JsonElement invokeAction(String url, JsonElement obj) throws WotClientException;
}
