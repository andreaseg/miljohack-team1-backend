package com.example.appengine.quarkus;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.UUID;

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
    @Operation(description = "Get a house from datastore by id")
    public House get(@Parameter(description = "The id. Format is UUID") @PathParam("id") String id) {
        var object = getDatastore().get(id, House.class);
        if (object == null) {
            throw new WebApplicationException("Object with id '" + id + "' not found", HttpURLConnection.HTTP_NOT_FOUND);
        }

        return object;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(description = "Create a new house, returns the id used to retrieve the house as the response")
    @APIResponse(name = "id", description = "Id of the house that has been created")
    public String post(@RequestBody(description = "The new house to be created") House body) {
        return getDatastore().put(body);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Create a new house with the given id, if the id is taken overwrites the old house")
    public void post(
            @Parameter(description = "The id. Format is UUID") @PathParam("id") String id,
            @RequestBody(description = "The new house to be created, or updated if an old id is used") House body
    ) {
        // Check if id is a UUID
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {
            throw new WebApplicationException("Id '" + id + "' is not a valid UUID", HttpURLConnection.HTTP_BAD_REQUEST);
        }

        getDatastore().put(id, body);
    }

    @DELETE
    @Path("{id}")
    @Operation(description = "Delete the house with the given id")
    public void delete(@Parameter(description = "The id. Format is UUID") @PathParam("id") String id) {
        getDatastore().delete(id);
    }
}
