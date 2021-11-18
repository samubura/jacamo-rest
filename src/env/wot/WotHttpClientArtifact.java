package wot;
import cartago.*;
import com.google.gson.JsonElement;
import wot.java.*;

/**
 * A client for a WebThing.
 * Can be configured with an authorization schema that changes the implementation of the HTTPClient used internally.
 * Use should be to instantiate an artifact for each thing that the agent wants to use.
 * The artifact should be used by a single agent and the id remembered as a mental note that can be retrieved with a test goal.
 */
public class WotHttpClientArtifact extends Artifact{

    private WotHttpClient client;
    private String name;

    void init(String name) {
        this.name = name;
        this.client = new BasicHttpClient(name);
    }

    @OPERATION
    void authorizeWithKey(String location, String tokenName, String tokenValue){
        TokenLocation tokenLocation = TokenLocation.valueOf(location.toUpperCase());
        this.client = new StringTokenAuthenticatedHttpClient(this.name, tokenLocation, tokenName, tokenValue);
    }

    @OPERATION
    void readProperty(String url, OpFeedbackParam<String> result) {
        try {
            result.set(client.readProperty(url).toString());
        } catch (WotClientException e) {
            failed(e.getMessage());
        }
    }

    @OPERATION
    void invokeAction(String url, JsonElement obj, OpFeedbackParam<String> result) {
        try {
            result.set(client.invokeAction(url, obj).toString());
        } catch (WotClientException e) {
            failed(e.getMessage());
        }
    }
}
