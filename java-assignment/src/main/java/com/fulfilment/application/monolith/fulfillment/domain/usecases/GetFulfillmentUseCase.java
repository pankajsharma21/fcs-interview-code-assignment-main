package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.GetFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Domain use case implementing GetFulfillmentOperation.
 */
@ApplicationScoped
public class GetFulfillmentUseCase implements GetFulfillmentOperation {

  private final FulfillmentStore fulfillmentStore;

  @Inject
  public GetFulfillmentUseCase(FulfillmentStore fulfillmentStore) {
    this.fulfillmentStore = fulfillmentStore;
  }

  @Override
  public List<FulfillmentAssociation> getByStore(Long storeId) {
    return fulfillmentStore.findByStore(storeId);
  }

  @Override
  public List<FulfillmentAssociation> getByWarehouse(String warehouseBusinessUnitCode) {
    return fulfillmentStore.findByWarehouse(warehouseBusinessUnitCode);
  }
}
