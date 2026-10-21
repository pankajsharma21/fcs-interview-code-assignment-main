package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

public class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  public void testReplaceWarehouseSuccessSameLocation() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 35, 10);
    Location location = new Location("ZWOLLE-001", 1, 40);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of(previous));

    useCase.replace(replacement);

    assertNotNull(previous.archivedAt);
    verify(warehouseStore).update(previous);
    verify(warehouseStore).create(replacement);
  }

  @Test
  public void testReplaceWarehouseSuccessDifferentLocation() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "AMSTERDAM-001", 60, 10);
    Location amsterdam = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(amsterdam);
    when(warehouseStore.findActiveByLocation("AMSTERDAM-001")).thenReturn(List.of());

    useCase.replace(replacement);

    assertNotNull(previous.archivedAt);
    verify(warehouseStore).update(previous);
    verify(warehouseStore).create(replacement);
  }

  @Test
  public void testReplaceWarehouseNullShouldFail() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(null));
    assertEquals("Replacement warehouse data cannot be null.", ex.getMessage());
  }

  @Test
  public void testReplaceWarehouseBlankBusinessUnitCodeShouldFail() {
    Warehouse replacement = new Warehouse("", "ZWOLLE-001", 35, 10);
    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceWarehousePreviousNotFoundShouldFail() {
    Warehouse replacement = new Warehouse("MWH.UNKNOWN", "ZWOLLE-001", 35, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.UNKNOWN")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals(
        "Active warehouse with business unit code MWH.UNKNOWN not found to replace.",
        ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouseStockMismatchShouldFail() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 35, 15);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals(
        "Stock of the new warehouse (15) does not match stock of previous warehouse (10).",
        ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouseCapacityCannotAccommodateStockShouldFail() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 25);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 20, 25);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals(
        "New warehouse capacity (20) cannot accommodate the stock of the warehouse being replaced (25).",
        ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouseInvalidLocationShouldFail() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "NON-EXISTENT", 35, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("NON-EXISTENT")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals(
        "Location NON-EXISTENT is invalid or does not exist.", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouseExceedsSameLocationCapacityLimitShouldFail() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 45, 10);
    Location location = new Location("ZWOLLE-001", 1, 40);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.findActiveByLocation("ZWOLLE-001")).thenReturn(List.of(previous));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals(
        "Warehouse capacity (45) exceeds location max capacity (40).", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouseDifferentLocationMaxWarehousesReachedShouldFail() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "TILBURG-001", 35, 10);
    Location tilburg = new Location("TILBURG-001", 1, 40);
    Warehouse existingInTilburg = new Warehouse("MWH.023", "TILBURG-001", 30, 27);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("TILBURG-001")).thenReturn(tilburg);
    when(warehouseStore.findActiveByLocation("TILBURG-001")).thenReturn(List.of(existingInTilburg));

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> useCase.replace(replacement));
    assertEquals(
        "Maximum number of warehouses (1) reached for location TILBURG-001", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouseNullLocationShouldFail() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", null, 35, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals("New warehouse location is required.", ex.getMessage());
  }

  @Test
  public void testReplaceWarehouseCapacityExceedsLocationMaxDirectly() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 100, 10);
    Location zwolle = new Location("ZWOLLE-001", 1, 40);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(zwolle);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals("Warehouse capacity (100) exceeds location max capacity (40).", ex.getMessage());
  }

  @Test
  public void testReplaceWarehouseDifferentLocationCapacityLimitExceeded() {
    Warehouse previous = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "AMSTERDAM-002", 50, 10);
    Location amsterdam = new Location("AMSTERDAM-002", 3, 75);
    Warehouse existingInAmsterdam = new Warehouse("MWH.AMS1", "AMSTERDAM-002", 40, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(previous);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(amsterdam);
    when(warehouseStore.findActiveByLocation("AMSTERDAM-002")).thenReturn(List.of(existingInAmsterdam));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals("Total warehouse capacity at location (90) exceeds location max capacity (75).", ex.getMessage());
  }
}
