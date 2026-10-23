package com.fulfilment.application.monolith.warehouses.domain.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WarehouseValidatorTest {

  private WarehouseValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new WarehouseValidator();
  }

  @Test
  public void testValidateForCreationSuccess() {
    Warehouse w = new Warehouse("MWH.VALID", "ZWOLLE-001", 30, 10);
    Location loc = new Location("ZWOLLE-001", 2, 50);

    assertDoesNotThrow(() -> validator.validateForCreation(w, null, loc, List.of()));
  }

  @Test
  public void testValidateForCreationNullData() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(null, null, null, null));
    assertEquals("Warehouse data cannot be null.", ex.getMessage());
  }

  @Test
  public void testValidateForCreationBlankBu() {
    Warehouse w = new Warehouse("  ", "ZWOLLE-001", 30, 10);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(w, null, null, null));
    assertEquals("Business unit code is required.", ex.getMessage());
  }

  @Test
  public void testValidateForCreationExistingBu() {
    Warehouse w = new Warehouse("MWH.DUP", "ZWOLLE-001", 30, 10);
    Warehouse existing = new Warehouse("MWH.DUP", "ZWOLLE-001", 40, 20);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(w, existing, null, null));
    assertEquals("Warehouse with business unit code MWH.DUP already exists.", ex.getMessage());
  }

  @Test
  public void testValidateForCreationMissingLocation() {
    Warehouse w = new Warehouse("MWH.001", null, 30, 10);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(w, null, null, null));
    assertEquals("Location is required.", ex.getMessage());
  }

  @Test
  public void testValidateForCreationInvalidLocation() {
    Warehouse w = new Warehouse("MWH.001", "NON-EXISTENT", 30, 10);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(w, null, null, null));
    assertEquals("Location NON-EXISTENT is invalid or does not exist.", ex.getMessage());
  }

  @Test
  public void testValidateForCreationMaxWarehousesReached() {
    Warehouse w = new Warehouse("MWH.002", "ZWOLLE-001", 30, 10);
    Location loc = new Location("ZWOLLE-001", 1, 100);
    Warehouse existing = new Warehouse("MWH.001", "ZWOLLE-001", 20, 5);

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> validator.validateForCreation(w, null, loc, List.of(existing)));
    assertEquals(
        "Maximum number of warehouses (1) reached for location ZWOLLE-001", ex.getMessage());
  }

  @Test
  public void testValidateForCreationCapacityExceedsMaxDirectly() {
    Warehouse w = new Warehouse("MWH.003", "ZWOLLE-001", 100, 10);
    Location loc = new Location("ZWOLLE-001", 5, 50);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(w, null, loc, List.of()));
    assertEquals("Warehouse capacity (100) exceeds location max capacity (50).", ex.getMessage());
  }

  @Test
  public void testValidateForCreationCumulativeCapacityExceeded() {
    Warehouse w = new Warehouse("MWH.004", "ZWOLLE-001", 30, 10);
    Location loc = new Location("ZWOLLE-001", 5, 50);
    Warehouse existing = new Warehouse("MWH.OLD", "ZWOLLE-001", 30, 5);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForCreation(w, null, loc, List.of(existing)));
    assertEquals(
        "Total warehouse capacity at location (60) exceeds location max capacity (50).",
        ex.getMessage());
  }

  @Test
  public void testValidateForReplacementSuccessSameLocation() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 35, 10);
    Location loc = new Location("ZWOLLE-001", 2, 50);

    assertDoesNotThrow(
        () -> validator.validateForReplacement(replacement, current, loc, List.of(current)));
  }

  @Test
  public void testValidateForReplacementSuccessDifferentLocation() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "AMSTERDAM-001", 35, 10);
    Location ams = new Location("AMSTERDAM-001", 3, 100);

    assertDoesNotThrow(
        () -> validator.validateForReplacement(replacement, current, ams, List.of()));
  }

  @Test
  public void testValidateForReplacementNullData() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(null, null, null, null));
    assertEquals("Replacement warehouse data cannot be null.", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementBlankBu() {
    Warehouse replacement = new Warehouse("", "ZWOLLE-001", 35, 10);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, null, null, null));
    assertEquals("Business unit code is required.", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementCurrentNotFound() {
    Warehouse replacement = new Warehouse("MWH.999", "ZWOLLE-001", 35, 10);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, null, null, null));
    assertEquals(
        "Active warehouse with business unit code MWH.999 not found to replace.", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementStockMismatch() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 35, 12);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, current, null, null));
    assertEquals(
        "Stock of the new warehouse (12) does not match stock of previous warehouse (10).",
        ex.getMessage());
  }

  @Test
  public void testValidateForReplacementCapacityCannotAccommodateStock() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 20);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 15, 20);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, current, null, null));
    assertEquals(
        "New warehouse capacity (15) cannot accommodate the stock of the warehouse being replaced (20).",
        ex.getMessage());
  }

  @Test
  public void testValidateForReplacementMissingLocation() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", null, 35, 10);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, current, null, null));
    assertEquals("New warehouse location is required.", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementInvalidLocation() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "INVALID", 35, 10);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, current, null, null));
    assertEquals("Location INVALID is invalid or does not exist.", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementDiffLocationMaxWarehouses() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "TILBURG-001", 35, 10);
    Location tilburg = new Location("TILBURG-001", 1, 50);
    Warehouse existingInTilburg = new Warehouse("MWH.TIL", "TILBURG-001", 30, 10);

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                validator.validateForReplacement(
                    replacement, current, tilburg, List.of(existingInTilburg)));
    assertEquals(
        "Maximum number of warehouses (1) reached for location TILBURG-001", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementDiffLocationCapacityExceeded() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "TILBURG-001", 35, 10);
    Location tilburg = new Location("TILBURG-001", 2, 50);
    Warehouse existingInTilburg = new Warehouse("MWH.TIL", "TILBURG-001", 30, 10);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                validator.validateForReplacement(
                    replacement, current, tilburg, List.of(existingInTilburg)));
    assertEquals(
        "Total warehouse capacity at location (65) exceeds location max capacity (50).",
        ex.getMessage());
  }

  @Test
  public void testValidateForReplacementExceedsLocationMaxDirectly() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 100, 10);
    Location loc = new Location("ZWOLLE-001", 2, 50);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateForReplacement(replacement, current, loc, List.of(current)));
    assertEquals("Warehouse capacity (100) exceeds location max capacity (50).", ex.getMessage());
  }

  @Test
  public void testValidateForReplacementSameLocationOtherWarehousesCapacityExceeded() {
    Warehouse current = new Warehouse("MWH.001", "ZWOLLE-001", 20, 10);
    Warehouse other = new Warehouse("MWH.002", "ZWOLLE-001", 25, 5);
    Warehouse replacement = new Warehouse("MWH.001", "ZWOLLE-001", 30, 10);
    Location loc = new Location("ZWOLLE-001", 2, 50);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                validator.validateForReplacement(
                    replacement, current, loc, List.of(current, other)));
    assertEquals("Warehouse capacity (30) exceeds location max capacity (50).", ex.getMessage());
  }

  @Test
  public void testValidateCapacityAndStockNullCapacity() {
    Warehouse w = new Warehouse("MWH.001", "ZWOLLE-001", null, 10);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateCapacityAndStock(w));
    assertEquals("Capacity must be greater than zero.", ex.getMessage());
  }

  @Test
  public void testValidateCapacityAndStockNegativeCapacity() {
    Warehouse w = new Warehouse("MWH.001", "ZWOLLE-001", -5, 10);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateCapacityAndStock(w));
    assertEquals("Capacity must be greater than zero.", ex.getMessage());
  }

  @Test
  public void testValidateCapacityAndStockNullStock() {
    Warehouse w = new Warehouse("MWH.001", "ZWOLLE-001", 50, null);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateCapacityAndStock(w));
    assertEquals("Stock cannot be negative.", ex.getMessage());
  }

  @Test
  public void testValidateCapacityAndStockNegativeStock() {
    Warehouse w = new Warehouse("MWH.001", "ZWOLLE-001", 50, -1);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateCapacityAndStock(w));
    assertEquals("Stock cannot be negative.", ex.getMessage());
  }

  @Test
  public void testValidateCapacityAndStockStockGreaterThanCapacity() {
    Warehouse w = new Warehouse("MWH.001", "ZWOLLE-001", 20, 30);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateCapacityAndStock(w));
    assertEquals("Warehouse capacity (20) cannot be less than stock (30).", ex.getMessage());
  }
}
