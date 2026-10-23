package com.fulfilment.application.monolith.fulfillment.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.jboss.logging.Logger;

/**
 * Transactional event listener for Fulfillment lifecycle events.
 * Listens AFTER_SUCCESS to ensure events are processed strictly post-commit.
 */
@ApplicationScoped
public class FulfillmentEventListener {

  private static final Logger LOG = Logger.getLogger(FulfillmentEventListener.class);

  public void onAssociated(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) FulfillmentAssociatedEvent event) {
    if (event != null && event.association() != null) {
      LOG.infof(
          "Fulfillment association committed: storeId=%d, productId=%d, warehouse=%s",
          event.association().storeId,
          event.association().productId,
          event.association().warehouseBusinessUnitCode);
    }
  }

  public void onDisassociated(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) FulfillmentDisassociatedEvent event) {
    if (event != null) {
      LOG.infof(
          "Fulfillment disassociation committed: storeId=%d, productId=%d, warehouse=%s",
          event.storeId(), event.productId(), event.warehouseBusinessUnitCode());
    }
  }
}
