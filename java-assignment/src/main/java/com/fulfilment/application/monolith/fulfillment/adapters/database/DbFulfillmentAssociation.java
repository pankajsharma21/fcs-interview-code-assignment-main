package com.fulfilment.application.monolith.fulfillment.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity for persisting fulfillment associations.
 * Isolated inside the database adapter layer.
 */
@Entity
@Table(
    name = "fulfillment_association",
    indexes = {
      @Index(name = "idx_fa_store", columnList = "storeId"),
      @Index(name = "idx_fa_warehouse", columnList = "warehouseBusinessUnitCode"),
      @Index(name = "idx_fa_product_store", columnList = "productId, storeId")
    })
public class DbFulfillmentAssociation extends PanacheEntity {

  @Column(nullable = false)
  public Long storeId;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  public DbFulfillmentAssociation() {}

  public DbFulfillmentAssociation(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
