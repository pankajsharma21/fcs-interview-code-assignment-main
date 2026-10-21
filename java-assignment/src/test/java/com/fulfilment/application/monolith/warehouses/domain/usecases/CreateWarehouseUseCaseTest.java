package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  public void testCreateWarehouseSuccess() {
    Warehouse warehouse = new Warehouse("MWH.NEW", "ZWOLLE-001", 30, 10);
    Location location = new Location("ZWOLLE-001", 2, 50);

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of());

    useCase.create(warehouse);

    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouseNullDataShouldFail() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(null));
    assertEquals("Warehouse data cannot be null.", ex.getMessage());
  }

  @Test
  public void testCreateWarehouseBlankBusinessUnitCodeShouldFail() {
    Warehouse warehouse = new Warehouse("", "ZWOLLE-001", 30, 10);
    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseDuplicateBusinessUnitCodeShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse existing = new Warehouse("MWH.001", "ZWOLLE-001", 40, 20);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals(
        "Warehouse with business unit code MWH.001 already exists.", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseInvalidLocationShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.002", "INVALID-LOC", 30, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.002")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Location INVALID-LOC is invalid or does not exist.", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseMaxWarehousesReachedShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.003", "ZWOLLE-001", 30, 10);
    Location location = new Location("ZWOLLE-001", 1, 40);
    Warehouse currentActive = new Warehouse("MWH.001", "ZWOLLE-001", 10, 5);

    when(warehouseStore.findByBusinessUnitCode("MWH.003")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of(currentActive));

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> useCase.create(warehouse));
    assertEquals(
        "Maximum number of warehouses (1) reached for location ZWOLLE-001", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseCapacityLessThanStockShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.004", "AMSTERDAM-001", 20, 50);
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.004")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("AMSTERDAM-001")).thenReturn(List.of());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse capacity (20) cannot be less than stock (50).", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseCapacityExceedsLocationMaxCapacityShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.005", "ZWOLLE-001", 100, 10);
    Location location = new Location("ZWOLLE-001", 2, 50);

    when(warehouseStore.findByBusinessUnitCode("MWH.005")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals(
        "Warehouse capacity (100) exceeds location max capacity (50).", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseCumulativeCapacityExceedsLocationLimitShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.006", "ZWOLLE-002", 35, 10);
    Location location = new Location("ZWOLLE-002", 2, 50);
    Warehouse existing = new Warehouse("MWH.OLD", "ZWOLLE-002", 25, 5);

    when(warehouseStore.findByBusinessUnitCode("MWH.006")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-002")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("ZWOLLE-002")).thenReturn(List.of(existing));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals(
        "Total warehouse capacity at location (60) exceeds location max capacity (50).",
        ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseNullLocationShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.007", null, 30, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.007")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Location is required.", ex.getMessage());
  }

  @Test
  public void testCreateWarehouseZeroOrNegativeCapacityShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.008", "ZWOLLE-001", 0, 0);
    Location location = new Location("ZWOLLE-001", 2, 50);
    when(warehouseStore.findByBusinessUnitCode("MWH.008")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Capacity must be greater than zero.", ex.getMessage());
  }

  @Test
  public void testCreateWarehouseNegativeStockShouldFail() {
    Warehouse warehouse = new Warehouse("MWH.009", "ZWOLLE-001", 30, -5);
    Location location = new Location("ZWOLLE-001", 2, 50);
    when(warehouseStore.findByBusinessUnitCode("MWH.009")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Stock cannot be negative.", ex.getMessage());
  }
}
