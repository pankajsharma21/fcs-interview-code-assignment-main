package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.AssociateProductOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.ProductResolver;
import com.fulfilment.application.monolith.fulfillment.domain.ports.StoreResolver;
import com.fulfilment.application.monolith.fulfillment.events.FulfillmentAssociatedEvent;
import com.fulfilment.application.monolith.fulfillment.validator.FulfillmentValidator;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Domain use case implementing AssociateProductOperation.
 * Coordinates input validation, entity checks via ports, multi-entity constraint enforcement,
 * persistence, and transactional event publishing.
 */
@ApplicationScoped
public class AssociateProductUseCase implements AssociateProductOperation {

  private final FulfillmentStore fulfillmentStore;
  private final WarehouseStore warehouseStore;
  private final StoreResolver storeResolver;
  private final ProductResolver productResolver;
  private final FulfillmentValidator validator;
  private final Event<FulfillmentAssociatedEvent> eventPublisher;

  @Inject
  public AssociateProductUseCase(
      FulfillmentStore fulfillmentStore,
      WarehouseStore warehouseStore,
      StoreResolver storeResolver,
      ProductResolver productResolver,
      FulfillmentValidator validator,
      Event<FulfillmentAssociatedEvent> eventPublisher) {
    this.fulfillmentStore = fulfillmentStore;
    this.warehouseStore = warehouseStore;
    this.storeResolver = storeResolver;
    this.productResolver = productResolver;
    this.validator = validator != null ? validator : new FulfillmentValidator();
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public FulfillmentAssociation associate(
      Long storeId, Long productId, String warehouseBusinessUnitCode) {

    // 1. Validate inputs
    validator.validateInput(storeId, productId, warehouseBusinessUnitCode);

    // 2. Validate external entities exist via ports
    boolean storeExists = storeResolver != null && storeResolver.existsById(storeId);
    boolean productExists = productResolver != null && productResolver.existsById(productId);
    Warehouse warehouse =
        warehouseStore != null
            ? warehouseStore.findByBusinessUnitCode(warehouseBusinessUnitCode)
            : null;

    validator.validateEntitiesExist(
        storeExists, storeId, productExists, productId, warehouse, warehouseBusinessUnitCode);

    // 3. Return existing if already associated
    FulfillmentAssociation existing =
        fulfillmentStore.findSpecific(storeId, productId, warehouseBusinessUnitCode);
    if (existing != null) {
      return existing;
    }

    // 4. Validate supply chain constraints atomically
    List<FulfillmentAssociation> productStoreAssocs =
        fulfillmentStore.findByProductAndStore(productId, storeId);
    List<FulfillmentAssociation> storeAssocs = fulfillmentStore.findByStore(storeId);
    List<FulfillmentAssociation> warehouseAssocs =
        fulfillmentStore.findByWarehouse(warehouseBusinessUnitCode);

    validator.validateConstraints(
        storeId,
        productId,
        warehouseBusinessUnitCode,
        productStoreAssocs,
        storeAssocs,
        warehouseAssocs);

    // 5. Persist new association
    FulfillmentAssociation newAssociation =
        new FulfillmentAssociation(storeId, productId, warehouseBusinessUnitCode);
    FulfillmentAssociation persisted = fulfillmentStore.save(newAssociation);

    // 6. Publish transactional event
    if (eventPublisher != null) {
      eventPublisher.fire(new FulfillmentAssociatedEvent(persisted));
    }

    return persisted;
  }
}
