package com.example.appengine.quarkus;

import com.google.common.io.Resources;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@OpenAPIDefinition(info = @Info(title = "Base Api", version = "1.0.0"))
@Path("/")
public class BaseResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() throws IOException {
        URL url = Resources.getResource("base.html");
        String text = Resources.toString(url, StandardCharsets.UTF_8);
        return text;
    }

}
