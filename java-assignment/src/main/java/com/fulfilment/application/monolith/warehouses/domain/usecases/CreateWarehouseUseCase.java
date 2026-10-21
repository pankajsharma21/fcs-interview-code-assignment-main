package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data cannot be null.");
    }

    // 1. Business Unit Code Verification
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required.");
    }
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " already exists.");
    }

    // 2. Location Validation
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location is required.");
    }
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException(
          "Location " + warehouse.location + " is invalid or does not exist.");
    }

    // 3. Warehouse Creation Feasibility (Count of active warehouses at location)
    List<Warehouse> activeInLocation = warehouseStore.findActiveByLocation(location.identification);
    if (activeInLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalStateException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") reached for location "
              + location.identification);
    }

    // 4. Capacity and Stock Validation
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
    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity ("
              + warehouse.capacity
              + ") exceeds location max capacity ("
              + location.maxCapacity
              + ").");
    }

    int currentTotalCapacity =
        activeInLocation.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0).sum();
    if (currentTotalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Total warehouse capacity at location ("
              + (currentTotalCapacity + warehouse.capacity)
              + ") exceeds location max capacity ("
              + location.maxCapacity
              + ").");
    }

    // If all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
