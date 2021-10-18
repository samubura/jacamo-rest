package mas.rest.config;

import java.util.HashMap;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import mas.rest.implementation.RestImpl;
import mas.rest.implementation.RestImplAg;
import mas.rest.implementation.RestImplDF;
import mas.rest.implementation.RestImplEnv;
import mas.rest.implementation.RestImplOrg;

@ApplicationPath("/")
public class RestAppConfig extends ResourceConfig {
    public RestAppConfig() {
        // Registering resource classes
        registerClasses(
                RestImpl.class,
                RestImplAg.class,
                RestImplEnv.class,
                RestImplOrg.class,
                RestImplDF.class);

        // gzip compression
        registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);

        addProperties(new HashMap<String,Object>() {
            private static final long serialVersionUID = 1L;

            { put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }
        } );
    }
}
