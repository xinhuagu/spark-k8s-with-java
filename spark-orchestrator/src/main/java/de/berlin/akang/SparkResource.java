package de.berlin.akang;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/spark")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SparkResource {

  @Inject
  SparkService sparkService;

  @POST
  public Response start(String sparkAppName) {
      try {
          this.sparkService.submitSparkApp(sparkAppName);

          return Response.accepted("Spark job started").build();
      } catch (Exception e) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Failed to start Spark job: " + e.getMessage())
                         .build();
      }
  }
}
