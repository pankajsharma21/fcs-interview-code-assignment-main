package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentAssociationRepository
    implements PanacheRepository<FulfillmentAssociation> {

  public List<FulfillmentAssociation> findByStore(Long storeId) {
    return list("storeId", storeId);
  }

  public List<FulfillmentAssociation> findByProductAndStore(Long productId, Long storeId) {
    return list("productId = ?1 and storeId = ?2", productId, storeId);
  }

  public List<FulfillmentAssociation> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode", warehouseBusinessUnitCode);
  }

  public FulfillmentAssociation findSpecific(
      Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return find(
            "storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
            storeId,
            productId,
            warehouseBusinessUnitCode)
        .firstResult();
  }
}
