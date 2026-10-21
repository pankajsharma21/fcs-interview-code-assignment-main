package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/fulfillment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

  @Inject FulfillmentService fulfillmentService;

  @POST
  @Path("/associate")
  public Response associate(FulfillmentRequest request) {
    if (request == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Request body is required").build());
    }

    try {
      FulfillmentAssociation association =
          fulfillmentService.associate(
              request.storeId, request.productId, request.warehouseBusinessUnitCode);
      return Response.status(Response.Status.CREATED).entity(association).build();
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
    }
  }

  @POST
  @Path("/disassociate")
  public Response disassociate(FulfillmentRequest request) {
    if (request == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Request body is required").build());
    }

    boolean removed =
        fulfillmentService.disassociate(
            request.storeId, request.productId, request.warehouseBusinessUnitCode);
    if (!removed) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  @Path("/store/{storeId}")
  public List<FulfillmentAssociation> getByStore(@PathParam("storeId") Long storeId) {
    return fulfillmentService.getAssociationsForStore(storeId);
  }

  @GET
  @Path("/warehouse/{businessUnitCode}")
  public List<FulfillmentAssociation> getByWarehouse(
      @PathParam("businessUnitCode") String businessUnitCode) {
    return fulfillmentService.getAssociationsForWarehouse(businessUnitCode);
  }
}
