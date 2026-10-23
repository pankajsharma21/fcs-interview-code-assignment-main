package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.ports.DisassociateProductOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.events.FulfillmentDisassociatedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Domain use case implementing DisassociateProductOperation.
 */
@ApplicationScoped
public class DisassociateProductUseCase implements DisassociateProductOperation {

  private final FulfillmentStore fulfillmentStore;
  private final Event<FulfillmentDisassociatedEvent> eventPublisher;

  @Inject
  public DisassociateProductUseCase(
      FulfillmentStore fulfillmentStore, Event<FulfillmentDisassociatedEvent> eventPublisher) {
    this.fulfillmentStore = fulfillmentStore;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public boolean disassociate(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    boolean deleted = fulfillmentStore.delete(storeId, productId, warehouseBusinessUnitCode);
    if (deleted && eventPublisher != null) {
      eventPublisher.fire(
          new FulfillmentDisassociatedEvent(storeId, productId, warehouseBusinessUnitCode));
    }
    return deleted;
  }
}
