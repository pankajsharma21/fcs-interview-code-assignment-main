package com.fulfilment.application.monolith.fulfillment.events;

/**
 * Event published when a fulfillment association is removed.
 */
public record FulfillmentDisassociatedEvent(
    Long storeId, Long productId, String warehouseBusinessUnitCode) {}
