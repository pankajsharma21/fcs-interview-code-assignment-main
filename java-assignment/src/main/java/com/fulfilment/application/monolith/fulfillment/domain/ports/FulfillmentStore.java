package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import java.util.List;

/**
 * Outbound SPI persistence port for Fulfillment associations.
 */
public interface FulfillmentStore {

  FulfillmentAssociation save(FulfillmentAssociation association);

  boolean delete(Long storeId, Long productId, String warehouseBusinessUnitCode);

  FulfillmentAssociation findSpecific(
      Long storeId, Long productId, String warehouseBusinessUnitCode);

  List<FulfillmentAssociation> findByStore(Long storeId);

  List<FulfillmentAssociation> findByWarehouse(String warehouseBusinessUnitCode);

  List<FulfillmentAssociation> findByProductAndStore(Long productId, Long storeId);

  long deleteAll();
}
