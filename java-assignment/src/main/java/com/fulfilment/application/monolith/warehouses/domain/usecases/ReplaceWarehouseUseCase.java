package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validator.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator validator;

  @Inject
  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore,
      LocationResolver locationResolver,
      WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.validator = validator != null ? validator : new WarehouseValidator();
  }

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this(warehouseStore, locationResolver, new WarehouseValidator());
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new IllegalArgumentException("Replacement warehouse data cannot be null.");
    }

    Warehouse current =
        newWarehouse.businessUnitCode != null
            ? warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)
            : null;

    Location location =
        newWarehouse.location != null
            ? locationResolver.resolveByIdentifier(newWarehouse.location)
            : null;

    List<Warehouse> activeInNewLocation =
        location != null
            ? warehouseStore.findActiveByLocation(location.identification)
            : List.of();

    // Delegate domain invariants and replacement constraints to dedicated validator
    validator.validateForReplacement(newWarehouse, current, location, activeInNewLocation);

    // Archive the previous warehouse
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    // Persist new replacement warehouse
    warehouseStore.create(newWarehouse);
  }
}
