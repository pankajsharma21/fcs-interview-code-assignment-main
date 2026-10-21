package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfillment_association",
    uniqueConstraints = {
      @UniqueConstraint(
          columnNames = {"storeId", "productId", "warehouseBusinessUnitCode"})
    })
@Cacheable
public class FulfillmentAssociation extends PanacheEntity {

  @Column(nullable = false)
  public Long storeId;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  public FulfillmentAssociation() {}

  public FulfillmentAssociation(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
