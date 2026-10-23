package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.AssociateProductOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.DisassociateProductOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.GetFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST API adapter exposing Fulfillment endpoints following Hexagonal Architecture.
 * Delegates incoming requests to domain ports.
 */
@Path("/fulfillment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

  private final AssociateProductOperation associateOperation;
  private final DisassociateProductOperation disassociateOperation;
  private final GetFulfillmentOperation getFulfillmentOperation;

  @Inject
  public FulfillmentResource(
      AssociateProductOperation associateOperation,
      DisassociateProductOperation disassociateOperation,
      GetFulfillmentOperation getFulfillmentOperation) {
    this.associateOperation = associateOperation;
    this.disassociateOperation = disassociateOperation;
    this.getFulfillmentOperation = getFulfillmentOperation;
  }

  @POST
  @Path("/associate")
  public Response associate(FulfillmentRequest request) {
    if (request == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Request body is required").build());
    }

    try {
      FulfillmentAssociation association =
          associateOperation.associate(
              request.storeId, request.productId, request.warehouseBusinessUnitCode);
      return Response.status(Response.Status.CREATED).entity(association).build();
    } catch (WebApplicationException e) {
      throw e;
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
        disassociateOperation.disassociate(
            request.storeId, request.productId, request.warehouseBusinessUnitCode);
    if (!removed) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  @Path("/store/{storeId}")
  public List<FulfillmentAssociation> getByStore(@PathParam("storeId") Long storeId) {
    return getFulfillmentOperation.getByStore(storeId);
  }

  @GET
  @Path("/warehouse/{businessUnitCode}")
  public List<FulfillmentAssociation> getByWarehouse(
      @PathParam("businessUnitCode") String businessUnitCode) {
    return getFulfillmentOperation.getByWarehouse(businessUnitCode);
  }
}
