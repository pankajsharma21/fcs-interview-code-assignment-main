package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

/**
 * Data Transfer Object for fulfillment association requests.
 */
public class FulfillmentRequest {

  public Long storeId;
  public Long productId;
  public String warehouseBusinessUnitCode;

  public FulfillmentRequest() {}

  public FulfillmentRequest(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
