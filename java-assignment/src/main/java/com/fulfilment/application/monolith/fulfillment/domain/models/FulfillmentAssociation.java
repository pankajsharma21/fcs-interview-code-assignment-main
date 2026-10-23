package com.fulfilment.application.monolith.fulfillment.domain.models;

/**
 * Pure domain model representing a product-warehouse-store fulfillment association.
 * Free of ORM, persistence, or framework dependencies.
 */
public class FulfillmentAssociation {

  public Long id;
  public Long storeId;
  public Long productId;
  public String warehouseBusinessUnitCode;

  public FulfillmentAssociation() {}

  public FulfillmentAssociation(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this(null, storeId, productId, warehouseBusinessUnitCode);
  }

  public FulfillmentAssociation(
      Long id, Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.id = id;
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
