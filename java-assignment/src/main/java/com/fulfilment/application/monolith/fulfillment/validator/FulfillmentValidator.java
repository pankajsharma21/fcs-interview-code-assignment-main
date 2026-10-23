package com.fulfilment.application.monolith.fulfillment.validator;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dedicated validator for Fulfillment operations and multi-entity supply chain constraints.
 * Separates validation logic from use cases and persistence adapters.
 */
@ApplicationScoped
public class FulfillmentValidator {

  /**
   * Validates non-null and non-blank input parameters.
   */
  public void validateInput(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    if (storeId == null) {
      throw new IllegalArgumentException("storeId cannot be null.");
    }
    if (productId == null) {
      throw new IllegalArgumentException("productId cannot be null.");
    }
    if (warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank()) {
      throw new IllegalArgumentException("warehouseBusinessUnitCode cannot be blank.");
    }
  }

  /**
   * Validates that Store, Product, and Warehouse entities exist and are active.
   */
  public void validateEntitiesExist(
      boolean storeExists,
      Long storeId,
      boolean productExists,
      Long productId,
      Warehouse warehouse,
      String warehouseBusinessUnitCode) {
    if (!storeExists) {
      throw new WebApplicationException("Store with id " + storeId + " not found.", 404);
    }
    if (!productExists) {
      throw new WebApplicationException("Product with id " + productId + " not found.", 404);
    }
    if (warehouse == null) {
      throw new WebApplicationException(
          "Active warehouse with businessUnitCode " + warehouseBusinessUnitCode + " not found.",
          404);
    }
  }

  /**
   * Validates all 3 fulfillment supply chain constraints atomically:
   * 1. Max 2 warehouses per product per store.
   * 2. Max 3 warehouses per store.
   * 3. Max 5 product types per warehouse.
   */
  public void validateConstraints(
      Long storeId,
      Long productId,
      String warehouseBusinessUnitCode,
      List<FulfillmentAssociation> productStoreAssocs,
      List<FulfillmentAssociation> storeAssocs,
      List<FulfillmentAssociation> warehouseAssocs) {

    // Constraint 1: Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
    Set<String> distinctWarehousesForProductInStore =
        productStoreAssocs == null
            ? Set.of()
            : productStoreAssocs.stream()
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
    Set<String> distinctWarehousesForStore =
        storeAssocs == null
            ? Set.of()
            : storeAssocs.stream()
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
    Set<Long> distinctProductsInWarehouse =
        warehouseAssocs == null
            ? Set.of()
            : warehouseAssocs.stream().map(a -> a.productId).collect(Collectors.toSet());

    if (!distinctProductsInWarehouse.contains(productId)
        && distinctProductsInWarehouse.size() >= 5) {
      throw new IllegalStateException(
          "Constraint violation: Warehouse "
              + warehouseBusinessUnitCode
              + " can maximally store 5 types of products (already has "
              + distinctProductsInWarehouse.size()
              + ")");
    }
  }
}
