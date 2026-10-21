package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new IllegalArgumentException("Replacement warehouse data cannot be null.");
    }
    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required.");
    }

    // 1. Locate current active warehouse
    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new IllegalArgumentException(
          "Active warehouse with business unit code "
              + newWarehouse.businessUnitCode
              + " not found to replace.");
    }

    // 2. Stock Matching validation
    if (!Objects.equals(newWarehouse.stock, current.stock)) {
      throw new IllegalArgumentException(
          "Stock of the new warehouse ("
              + newWarehouse.stock
              + ") does not match stock of previous warehouse ("
              + current.stock
              + ").");
    }

    // 3. Capacity Accommodation validation
    if (newWarehouse.capacity == null || newWarehouse.capacity < current.stock) {
      throw new IllegalArgumentException(
          "New warehouse capacity ("
              + newWarehouse.capacity
              + ") cannot accommodate the stock of the warehouse being replaced ("
              + current.stock
              + ").");
    }

    // 4. Location Validation
    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new IllegalArgumentException("New warehouse location is required.");
    }
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException(
          "Location " + newWarehouse.location + " is invalid or does not exist.");
    }

    if (newWarehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity ("
              + newWarehouse.capacity
              + ") exceeds location max capacity ("
              + location.maxCapacity
              + ").");
    }

    // Check location constraints depending on whether replacement is in the same or different location
    boolean isSameLocation = location.identification.equalsIgnoreCase(current.location);
    List<Warehouse> activeInLocation =
        warehouseStore.findActiveByLocation(location.identification);

    if (isSameLocation) {
      int currentTotalCapacity =
          activeInLocation.stream()
              .mapToInt(w -> w.capacity != null ? w.capacity : 0)
              .sum();
      int currentWarehouseCapacity = current.capacity != null ? current.capacity : 0;
      int adjustedTotal = currentTotalCapacity - currentWarehouseCapacity + newWarehouse.capacity;

      if (adjustedTotal > location.maxCapacity) {
        throw new IllegalArgumentException(
            "Total warehouse capacity at location ("
                + adjustedTotal
                + ") exceeds location max capacity ("
                + location.maxCapacity
                + ").");
      }
    } else {
      if (activeInLocation.size() >= location.maxNumberOfWarehouses) {
        throw new IllegalStateException(
            "Maximum number of warehouses ("
                + location.maxNumberOfWarehouses
                + ") reached for location "
                + location.identification);
      }
      int currentTotalCapacity =
          activeInLocation.stream()
              .mapToInt(w -> w.capacity != null ? w.capacity : 0)
              .sum();
      if (currentTotalCapacity + newWarehouse.capacity > location.maxCapacity) {
        throw new IllegalArgumentException(
            "Total warehouse capacity at location ("
                + (currentTotalCapacity + newWarehouse.capacity)
                + ") exceeds location max capacity ("
                + location.maxCapacity
                + ").");
      }
    }

    // 5. Execute Archival of old warehouse and Creation of new warehouse
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
