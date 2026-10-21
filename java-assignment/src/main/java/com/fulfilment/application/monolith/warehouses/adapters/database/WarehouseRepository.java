package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.find("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    dbWarehouse.archivedAt = warehouse.archivedAt;

    this.persist(dbWarehouse);

    warehouse.id = dbWarehouse.id;
    warehouse.createdAt = dbWarehouse.createdAt;
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = null;
    if (warehouse.id != null) {
      dbWarehouse = this.findById(warehouse.id);
    }
    if (dbWarehouse == null && warehouse.businessUnitCode != null) {
      dbWarehouse =
          this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
              .firstResult();
    }
    if (dbWarehouse != null) {
      dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.archivedAt = warehouse.archivedAt;
    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse dbWarehouse = null;
    if (warehouse.id != null) {
      dbWarehouse = this.findById(warehouse.id);
    }
    if (dbWarehouse == null && warehouse.businessUnitCode != null) {
      dbWarehouse =
          this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
              .firstResult();
    }
    if (dbWarehouse != null) {
      dbWarehouse.archivedAt = LocalDateTime.now();
      warehouse.archivedAt = dbWarehouse.archivedAt;
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      return null;
    }
    DbWarehouse dbWarehouse =
        this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  @Override
  public Warehouse findById(String id) {
    if (id == null || id.isBlank()) {
      return null;
    }
    try {
      Long numId = Long.parseLong(id);
      DbWarehouse dbWarehouse = this.findById(numId);
      if (dbWarehouse != null && dbWarehouse.archivedAt == null) {
        return dbWarehouse.toWarehouse();
      }
    } catch (NumberFormatException ignored) {
    }
    return findByBusinessUnitCode(id);
  }

  @Override
  public List<Warehouse> findActiveByLocation(String location) {
    if (location == null) {
      return List.of();
    }
    return this.find("location = ?1 and archivedAt is null", location).stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }
}
