package jacamo.rest;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import jacamo.infra.JaCaMoLauncher;

public class RestImplDFTest {
    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem("src/test/test1.jcm");
    }

    @AfterClass
    public static void stopSystem() {
        JaCaMoLauncher.getRunner().finish();
    }    


    @Test
    @SuppressWarnings("rawtypes")
    public void testRegisterDF() {
        Client client = ClientBuilder.newClient();
        Response response = client
            .target(uri.toString())
            .path("services")
            //TODO: .path("services") is a workaround. Correct: .path("agents/marcos/services")
            .request(MediaType.APPLICATION_JSON)
            .get();
        
        List vl = new Gson().fromJson(response.readEntity(String.class), List.class);

        //System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);

        assertTrue(vl.contains("vender(banana)"));
        
        Map<String,String> map = new HashMap<>();
        map.put("service", "help(drunks)");
        //map.put("type", "AA");
        //System.out.println(new Gson().toJson( map ));
        client
            .target(uri.toString())
            .path("agents/marcos/services")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson( map )));

        response = client
                .target(uri.toString())
                .path("services")
                //TODO: .path("services") is a workaround. Correct: .path("agents/marcos/services")
                .request(MediaType.APPLICATION_JSON)
                .get();
            
        vl = new Gson().fromJson(response.readEntity(String.class), List.class);

        //System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        
        assertTrue(vl.contains("help(drunks)"));
    }

    @Test(timeout=2000)
    @SuppressWarnings("rawtypes")
    public void testRegisterWP() {
        Client client = ClientBuilder.newClient();
        Response response = client
            .target(uri.toString())
            .path("wp")
            .request(MediaType.APPLICATION_JSON)
            .get();
        
        Map vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        assertTrue( vl.get("marcos") != null);
        
        Map<String,String> map = new HashMap<>();
        map.put("agentid", "jomi");
        map.put("uri", "http://myhouse");
        //System.out.println("=="+new Gson().toJson(map));
       
        // add new entry
        client
            .target(uri.toString())
            .path("wp")
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_PLAIN)
            .post(Entity.json(new Gson().toJson( map )));

        response = client
                .target(uri.toString())
                .path("wp")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .get();
            
        vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
    
        assertTrue( vl.get("jomi").equals("http://myhouse"));       
    }
}
