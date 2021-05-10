
package jaxrs.examples.resource;

import java.util.concurrent.Executors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("/greet1")    // CDI bean-defining annotation
@RequestScoped      // implied by default
public class SampleResource1 {

    private final GreetBean greetBean;

    // Use @Inject instead of @Context
    @Inject
    private UriInfo uriInfo;

    // Use of @HeaderParam as CDI qualifier
    @Inject
    @HeaderParam("who")
    private String who;

    // CDI Injection in constructor
    @Inject
    public SampleResource1(GreetBean greetingConfig) {
        this.greetBean = greetingConfig;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(@HeaderParam("who") String who) {
        return greetBean.getMessage() + " " + who;
    }

    // Use of @Entity is required
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void putMessage(@QueryParam("override") boolean override, @Entity String message) {
        if (greetBean.getMessage().isEmpty() || override) {
            greetBean.setMessage(message);
        }
    }

    // No need to use @Suspended with AsyncResponse
    @POST
    @Path("async")
    @Consumes(MediaType.TEXT_PLAIN)
    public void putMessageAsync(@QueryParam("override") boolean override, @Entity String message, AsyncResponse ar) {
        Executors.newSingleThreadExecutor().submit(() -> {
            if (greetBean.getMessage().isEmpty() || override) {
                greetBean.setMessage(message);
            }
            ar.resume("Done");
        });
    }

    // Any JAX-RS bean such as Sse or SseEventSink can be injected without @Context
    // They are regular CDI beans produced by JAX-RS implementations
    @GET
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getMessage(@HeaderParam("who") String who, Sse sse, SseEventSink sseEventSink) {
        sseEventSink.send(sse.newEvent(greetBean.getMessage() + " " + who));
    }
}
