package jacamo.web;

import java.util.HashMap;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class RestAppConfig extends ResourceConfig {
    public RestAppConfig() {
        registerInstances(new RestImpl());
        registerInstances(new RestImplAg());
        registerInstances(new RestImplDF());
        
        addProperties(new HashMap<String,Object>() {{ put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }} );   
    }
}
