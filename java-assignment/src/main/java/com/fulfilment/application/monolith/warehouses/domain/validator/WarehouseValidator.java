package com.fulfilment.application.monolith.warehouses.domain.validator;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Objects;

/**
 * Dedicated domain validator for Warehouse operations.
 * Separates validation logic from use cases, enforcing domain invariants,
 * business unit rules, capacity constraints, location limits, and replacement rules.
 */
@ApplicationScoped
public class WarehouseValidator {

  /**
   * Validates warehouse payload and domain constraints for creation.
   *
   * @param warehouse the warehouse to create
   * @param existingWithSameBu existing warehouse with same BU code (if any)
   * @param location the resolved location
   * @param activeInLocation list of currently active warehouses at this location
   */
  public void validateForCreation(
      Warehouse warehouse,
      Warehouse existingWithSameBu,
      Location location,
      List<Warehouse> activeInLocation) {

    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data cannot be null.");
    }

    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required.");
    }

    if (existingWithSameBu != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " already exists.");
    }

    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location is required.");
    }

    if (location == null) {
      throw new IllegalArgumentException(
          "Location " + warehouse.location + " is invalid or does not exist.");
    }

    if (activeInLocation != null && activeInLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalStateException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") reached for location "
              + location.identification);
    }

    validateCapacityAndStock(warehouse);

    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity ("
              + warehouse.capacity
              + ") exceeds location max capacity ("
              + location.maxCapacity
              + ").");
    }

    int currentTotalCapacity =
        activeInLocation == null
            ? 0
            : activeInLocation.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0).sum();

    if (currentTotalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Total warehouse capacity at location ("
              + (currentTotalCapacity + warehouse.capacity)
              + ") exceeds location max capacity ("
              + location.maxCapacity
              + ").");
    }
  }

  /**
   * Validates warehouse payload and domain constraints for replacement.
   *
   * @param newWarehouse the new warehouse payload
   * @param current the existing warehouse being replaced
   * @param newLocation the resolved location for the replacement
   * @param activeInNewLocation list of currently active warehouses in the target location
   */
  public void validateForReplacement(
      Warehouse newWarehouse,
      Warehouse current,
      Location newLocation,
      List<Warehouse> activeInNewLocation) {

    if (newWarehouse == null) {
      throw new IllegalArgumentException("Replacement warehouse data cannot be null.");
    }

    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required.");
    }

    if (current == null) {
      throw new IllegalArgumentException(
          "Active warehouse with business unit code "
              + newWarehouse.businessUnitCode
              + " not found to replace.");
    }

    if (!Objects.equals(newWarehouse.stock, current.stock)) {
      throw new IllegalArgumentException(
          "Stock of the new warehouse ("
              + newWarehouse.stock
              + ") does not match stock of previous warehouse ("
              + current.stock
              + ").");
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity < current.stock) {
      throw new IllegalArgumentException(
          "New warehouse capacity ("
              + newWarehouse.capacity
              + ") cannot accommodate the stock of the warehouse being replaced ("
              + current.stock
              + ").");
    }

    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new IllegalArgumentException("New warehouse location is required.");
    }

    if (newLocation == null) {
      throw new IllegalArgumentException(
          "Location " + newWarehouse.location + " is invalid or does not exist.");
    }

    if (newWarehouse.capacity > newLocation.maxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity ("
              + newWarehouse.capacity
              + ") exceeds location max capacity ("
              + newLocation.maxCapacity
              + ").");
    }

    boolean isSameLocation = Objects.equals(current.location, newWarehouse.location);

    if (!isSameLocation) {
      if (activeInNewLocation != null
          && activeInNewLocation.size() >= newLocation.maxNumberOfWarehouses) {
        throw new IllegalStateException(
            "Maximum number of warehouses ("
                + newLocation.maxNumberOfWarehouses
                + ") reached for location "
                + newLocation.identification);
      }

      int targetCapacity =
          activeInNewLocation == null
              ? 0
              : activeInNewLocation.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0).sum();

      if (targetCapacity + newWarehouse.capacity > newLocation.maxCapacity) {
        throw new IllegalArgumentException(
            "Total warehouse capacity at location ("
                + (targetCapacity + newWarehouse.capacity)
                + ") exceeds location max capacity ("
                + newLocation.maxCapacity
                + ").");
      }
    } else {
      int otherWarehousesCapacity =
          activeInNewLocation == null
              ? 0
              : activeInNewLocation.stream()
                  .filter(w -> !Objects.equals(w.businessUnitCode, current.businessUnitCode))
                  .mapToInt(w -> w.capacity != null ? w.capacity : 0)
                  .sum();

      if (otherWarehousesCapacity + newWarehouse.capacity > newLocation.maxCapacity) {
        throw new IllegalArgumentException(
            "Warehouse capacity ("
                + newWarehouse.capacity
                + ") exceeds location max capacity ("
                + newLocation.maxCapacity
                + ").");
      }
    }
  }

  /**
   * Validates capacity and stock attributes.
   */
  public void validateCapacityAndStock(Warehouse warehouse) {
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than zero.");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative.");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity ("
              + warehouse.capacity
              + ") cannot be less than stock ("
              + warehouse.stock
              + ").");
    }
  }
}
