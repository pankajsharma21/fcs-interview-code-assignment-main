package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;

/**
 * Inbound port for creating a product-warehouse-store fulfillment association.
 */
public interface AssociateProductOperation {

  FulfillmentAssociation associate(Long storeId, Long productId, String warehouseBusinessUnitCode);
}
