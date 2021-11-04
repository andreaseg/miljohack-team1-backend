package com.example.appengine.quarkus;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Optional;

@OpenAPIDefinition(info = @Info(title = "House Api", version = "1.0.0"))
@Path("/houses")
public class HouseResource {

    private static final String COLLECTION = "houses";


    @ConfigProperty(name = "store.impl")
    String storeImplementation;

    @ConfigProperty(name = "GOOGLE_CLOUD_PROJECT")
    Optional<String> projectId;

    private Datastore datastore;

    private Datastore getDatastore() {
        if (datastore == null) {
            System.out.println("Using datastore impl " + storeImplementation);
            System.out.println("Using projectId " + projectId.orElse(null));

            switch (storeImplementation) {
                case "Firestore":
                    try {
                        datastore = new FirestoreDatastoreImpl(
                                projectId.orElseThrow(() -> new IllegalStateException("GOOGLE_CLOUD_PROJECT")),
                                COLLECTION
                        );
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    break;
                case "HashMap":
                    datastore = new HashMapDatastoreImpl();
                    break;
                default:
                    throw new IllegalStateException("Property store.impl not valid, was " + storeImplementation);
            }
        }

        return datastore;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public House get(@PathParam("id") String id) {
        return getDatastore().get(id, House.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String post(House body) {
        return getDatastore().put(body);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void post(@PathParam("id") String id, House body) {
        getDatastore().put(id, body);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") String id) {
        getDatastore().delete(id);
    }
}
