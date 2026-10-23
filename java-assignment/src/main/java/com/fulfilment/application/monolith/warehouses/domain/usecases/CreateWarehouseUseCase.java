package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validator.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator validator;

  @Inject
  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore,
      LocationResolver locationResolver,
      WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.validator = validator != null ? validator : new WarehouseValidator();
  }

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this(warehouseStore, locationResolver, new WarehouseValidator());
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data cannot be null.");
    }

    Warehouse existingWithSameBu =
        warehouse.businessUnitCode != null
            ? warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode)
            : null;

    Location location =
        warehouse.location != null
            ? locationResolver.resolveByIdentifier(warehouse.location)
            : null;

    List<Warehouse> activeInLocation =
        location != null
            ? warehouseStore.findActiveByLocation(location.identification)
            : List.of();

    // Delegate domain invariants and constraint checks to dedicated validator
    validator.validateForCreation(warehouse, existingWithSameBu, location, activeInLocation);

    // If validation succeeded, persist warehouse
    warehouseStore.create(warehouse);
  }
}
