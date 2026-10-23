package com.fulfilment.application.monolith.fulfillment.events;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;

/**
 * Event published when a product-warehouse-store fulfillment association is created.
 */
public record FulfillmentAssociatedEvent(FulfillmentAssociation association) {}
