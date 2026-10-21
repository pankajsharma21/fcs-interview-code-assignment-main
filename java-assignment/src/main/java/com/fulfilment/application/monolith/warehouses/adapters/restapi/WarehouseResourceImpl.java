package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Inject private io.vertx.core.http.HttpServerResponse response;

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    if (data == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Warehouse data must not be null").build());
    }

    var domainWarehouse = toDomainWarehouse(data);

    try {
      createWarehouseOperation.create(domainWarehouse);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new WebApplicationException(
          Response.status(400).entity(e.getMessage()).build());
    }

    if (response != null) {
      response.setStatusCode(201);
    }

    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    if (id == null || id.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("Warehouse ID must not be blank").build());
    }

    var domainWarehouse = warehouseRepository.findById(id);
    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }

    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    if (id == null || id.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("Warehouse ID must not be blank").build());
    }

    var domainWarehouse = warehouseRepository.findById(id);
    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }

    archiveWarehouseOperation.archive(domainWarehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("businessUnitCode is required").build());
    }
    if (data == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Replacement data must not be null").build());
    }

    var existing = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    if (existing == null) {
      throw new WebApplicationException(
          "Warehouse with business unit code " + businessUnitCode + " does not exist.", 404);
    }

    var domainWarehouse = toDomainWarehouse(data);
    domainWarehouse.businessUnitCode = businessUnitCode;

    try {
      replaceWarehouseOperation.replace(domainWarehouse);
    } catch (IllegalArgumentException | IllegalStateException e) {
      org.jboss.logging.Logger.getLogger(WarehouseResourceImpl.class)
          .error("Replace failed: " + e.getMessage(), e);
      throw new WebApplicationException(
          Response.status(400).entity(e.getMessage()).build());
    }

    return toWarehouseResponse(domainWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    if (warehouse.id != null) {
      response.setId(String.valueOf(warehouse.id));
    }
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse bean) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = bean.getBusinessUnitCode();
    warehouse.location = bean.getLocation();
    warehouse.capacity = bean.getCapacity();
    warehouse.stock = bean.getStock();
    return warehouse;
  }
}
