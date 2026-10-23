package com.fulfilment.application.monolith.fulfillment.domain.ports;

/**
 * Inbound port for removing an existing product-warehouse-store fulfillment association.
 */
public interface DisassociateProductOperation {

  boolean disassociate(Long storeId, Long productId, String warehouseBusinessUnitCode);
}
