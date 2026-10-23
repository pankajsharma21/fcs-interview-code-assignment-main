package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import java.util.List;

/**
 * Inbound port for querying fulfillment associations.
 */
public interface GetFulfillmentOperation {

  List<FulfillmentAssociation> getByStore(Long storeId);

  List<FulfillmentAssociation> getByWarehouse(String warehouseBusinessUnitCode);
}
