package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class FulfillmentService {

  @Inject FulfillmentAssociationRepository repository;
  @Inject WarehouseStore warehouseStore;
  @Inject com.fulfilment.application.monolith.products.ProductRepository productRepository;

  @Transactional
  public FulfillmentAssociation associate(
      Long storeId, Long productId, String warehouseBusinessUnitCode) {
    if (storeId == null) {
      throw new IllegalArgumentException("storeId cannot be null.");
    }
    if (productId == null) {
      throw new IllegalArgumentException("productId cannot be null.");
    }
    if (warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank()) {
      throw new IllegalArgumentException("warehouseBusinessUnitCode cannot be blank.");
    }

    // Verify Store exists
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new WebApplicationException("Store with id " + storeId + " not found.", 404);
    }

    // Verify Product exists
    Product product = productRepository.findById(productId);
    if (product == null) {
      throw new WebApplicationException("Product with id " + productId + " not found.", 404);
    }

    // Verify Warehouse exists and is active
    Warehouse warehouse = warehouseStore.findByBusinessUnitCode(warehouseBusinessUnitCode);
    if (warehouse == null) {
      throw new WebApplicationException(
          "Active warehouse with businessUnitCode " + warehouseBusinessUnitCode + " not found.",
          404);
    }

    // If association already exists, return existing
    FulfillmentAssociation existing =
        repository.findSpecific(storeId, productId, warehouseBusinessUnitCode);
    if (existing != null) {
      return existing;
    }

    // Constraint 1: Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
    List<FulfillmentAssociation> productStoreAssocs =
        repository.findByProductAndStore(productId, storeId);
    Set<String> distinctWarehousesForProductInStore =
        productStoreAssocs.stream()
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toSet());

    if (!distinctWarehousesForProductInStore.contains(warehouseBusinessUnitCode)
        && distinctWarehousesForProductInStore.size() >= 2) {
      throw new IllegalStateException(
          "Constraint violation: Product "
              + productId
              + " is already fulfilled by 2 different warehouses for store "
              + storeId);
    }

    // Constraint 2: Each Store can be fulfilled by a maximum of 3 different Warehouses
    List<FulfillmentAssociation> storeAssocs = repository.findByStore(storeId);
    Set<String> distinctWarehousesForStore =
        storeAssocs.stream()
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toSet());

    if (!distinctWarehousesForStore.contains(warehouseBusinessUnitCode)
        && distinctWarehousesForStore.size() >= 3) {
      throw new IllegalStateException(
          "Constraint violation: Store "
              + storeId
              + " is already fulfilled by 3 different warehouses ("
              + distinctWarehousesForStore
              + ")");
    }

    // Constraint 3: Each Warehouse can store maximally 5 types of Products
    List<FulfillmentAssociation> warehouseAssocs =
        repository.findByWarehouse(warehouseBusinessUnitCode);
    Set<Long> distinctProductsInWarehouse =
        warehouseAssocs.stream().map(a -> a.productId).collect(Collectors.toSet());

    if (!distinctProductsInWarehouse.contains(productId)
        && distinctProductsInWarehouse.size() >= 5) {
      throw new IllegalStateException(
          "Constraint violation: Warehouse "
              + warehouseBusinessUnitCode
              + " can maximally store 5 types of products (already has "
              + distinctProductsInWarehouse.size()
              + ")");
    }

    FulfillmentAssociation newAssociation =
        new FulfillmentAssociation(storeId, productId, warehouseBusinessUnitCode);
    repository.persist(newAssociation);
    return newAssociation;
  }

  @Transactional
  public boolean disassociate(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    FulfillmentAssociation existing =
        repository.findSpecific(storeId, productId, warehouseBusinessUnitCode);
    if (existing != null) {
      repository.delete(existing);
      return true;
    }
    return false;
  }

  public List<FulfillmentAssociation> getAssociationsForStore(Long storeId) {
    return repository.findByStore(storeId);
  }

  public List<FulfillmentAssociation> getAssociationsForWarehouse(String warehouseBuCode) {
    return repository.findByWarehouse(warehouseBuCode);
  }
}
