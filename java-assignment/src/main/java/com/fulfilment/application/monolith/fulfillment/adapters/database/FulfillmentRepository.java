package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence adapter implementing the outbound FulfillmentStore port using Hibernate Panache.
 */
@ApplicationScoped
public class FulfillmentRepository
    implements FulfillmentStore, PanacheRepository<DbFulfillmentAssociation> {

  @Override
  public FulfillmentAssociation save(FulfillmentAssociation association) {
    DbFulfillmentAssociation entity =
        new DbFulfillmentAssociation(
            association.storeId, association.productId, association.warehouseBusinessUnitCode);
    persist(entity);
    association.id = entity.id;
    return association;
  }

  @Override
  public boolean delete(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    long deleted =
        delete(
            "storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
            storeId,
            productId,
            warehouseBusinessUnitCode);
    return deleted > 0;
  }

  @Override
  public FulfillmentAssociation findSpecific(
      Long storeId, Long productId, String warehouseBusinessUnitCode) {
    DbFulfillmentAssociation entity =
        find(
                "storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
                storeId,
                productId,
                warehouseBusinessUnitCode)
            .firstResult();
    return entity != null ? toDomain(entity) : null;
  }

  @Override
  public List<FulfillmentAssociation> findByStore(Long storeId) {
    return list("storeId", storeId).stream().map(this::toDomain).collect(Collectors.toList());
  }

  @Override
  public List<FulfillmentAssociation> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode", warehouseBusinessUnitCode).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<FulfillmentAssociation> findByProductAndStore(Long productId, Long storeId) {
    return list("productId = ?1 and storeId = ?2", productId, storeId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public long deleteAll() {
    return delete("1=1");
  }

  private FulfillmentAssociation toDomain(DbFulfillmentAssociation entity) {
    return new FulfillmentAssociation(
        entity.id, entity.storeId, entity.productId, entity.warehouseBusinessUnitCode);
  }
}
