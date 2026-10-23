package com.fulfilment.application.monolith.fulfillment.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FulfillmentValidatorTest {

  private FulfillmentValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new FulfillmentValidator();
  }

  @Test
  public void testValidateInputSuccess() {
    assertDoesNotThrow(() -> validator.validateInput(1L, 2L, "MWH.001"));
  }

  @Test
  public void testValidateInputNullStoreId() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateInput(null, 2L, "MWH.001"));
    assertEquals("storeId cannot be null.", ex.getMessage());
  }

  @Test
  public void testValidateInputNullProductId() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateInput(1L, null, "MWH.001"));
    assertEquals("productId cannot be null.", ex.getMessage());
  }

  @Test
  public void testValidateInputBlankWarehouseBu() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> validator.validateInput(1L, 2L, "   "));
    assertEquals("warehouseBusinessUnitCode cannot be blank.", ex.getMessage());
  }

  @Test
  public void testValidateEntitiesExistSuccess() {
    Warehouse warehouse = new Warehouse("MWH.001", "ZWOLLE-001", 50, 10);

    assertDoesNotThrow(
        () -> validator.validateEntitiesExist(true, 1L, true, 1L, warehouse, "MWH.001"));
  }

  @Test
  public void testValidateEntitiesExistNullStoreThrows404() {
    Warehouse warehouse = new Warehouse("MWH.001", "ZWOLLE-001", 50, 10);

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> validator.validateEntitiesExist(false, 99L, true, 1L, warehouse, "MWH.001"));
    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Store with id 99 not found"));
  }

  @Test
  public void testValidateEntitiesExistNullProductThrows404() {
    Warehouse warehouse = new Warehouse("MWH.001", "ZWOLLE-001", 50, 10);

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> validator.validateEntitiesExist(true, 1L, false, 88L, warehouse, "MWH.001"));
    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Product with id 88 not found"));
  }

  @Test
  public void testValidateEntitiesExistNullWarehouseThrows404() {
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> validator.validateEntitiesExist(true, 1L, true, 1L, null, "MWH.MISSING"));
    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("MWH.MISSING not found"));
  }

  @Test
  public void testConstraint1Max2WarehousesPerProductViolation() {
    List<FulfillmentAssociation> productStoreAssocs =
        List.of(
            new FulfillmentAssociation(1L, 1L, "MWH.001"),
            new FulfillmentAssociation(1L, 1L, "MWH.002"));

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                validator.validateConstraints(
                    1L, 1L, "MWH.003", productStoreAssocs, List.of(), List.of()));
    assertTrue(ex.getMessage().contains("already fulfilled by 2 different warehouses"));
  }

  @Test
  public void testConstraint1ExistingWarehouseAllowed() {
    List<FulfillmentAssociation> productStoreAssocs =
        List.of(
            new FulfillmentAssociation(1L, 1L, "MWH.001"),
            new FulfillmentAssociation(1L, 1L, "MWH.002"));

    assertDoesNotThrow(
        () ->
            validator.validateConstraints(
                1L, 1L, "MWH.001", productStoreAssocs, List.of(), List.of()));
  }

  @Test
  public void testConstraint2Max3WarehousesPerStoreViolation() {
    List<FulfillmentAssociation> storeAssocs =
        List.of(
            new FulfillmentAssociation(1L, 1L, "MWH.001"),
            new FulfillmentAssociation(1L, 2L, "MWH.002"),
            new FulfillmentAssociation(1L, 3L, "MWH.003"));

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                validator.validateConstraints(
                    1L, 4L, "MWH.004", List.of(), storeAssocs, List.of()));
    assertTrue(ex.getMessage().contains("already fulfilled by 3 different warehouses"));
  }

  @Test
  public void testConstraint2ExistingWarehouseAllowed() {
    List<FulfillmentAssociation> storeAssocs =
        List.of(
            new FulfillmentAssociation(1L, 1L, "MWH.001"),
            new FulfillmentAssociation(1L, 2L, "MWH.002"),
            new FulfillmentAssociation(1L, 3L, "MWH.003"));

    assertDoesNotThrow(
        () ->
            validator.validateConstraints(
                1L, 4L, "MWH.002", List.of(), storeAssocs, List.of()));
  }

  @Test
  public void testConstraint3Max5ProductsPerWarehouseViolation() {
    List<FulfillmentAssociation> warehouseAssocs =
        List.of(
            new FulfillmentAssociation(1L, 1L, "MWH.001"),
            new FulfillmentAssociation(1L, 2L, "MWH.001"),
            new FulfillmentAssociation(1L, 3L, "MWH.001"),
            new FulfillmentAssociation(1L, 4L, "MWH.001"),
            new FulfillmentAssociation(1L, 5L, "MWH.001"));

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                validator.validateConstraints(
                    1L, 6L, "MWH.001", List.of(), List.of(), warehouseAssocs));
    assertTrue(ex.getMessage().contains("maximally store 5 types of products"));
  }

  @Test
  public void testConstraint3ExistingProductAllowed() {
    List<FulfillmentAssociation> warehouseAssocs =
        List.of(
            new FulfillmentAssociation(1L, 1L, "MWH.001"),
            new FulfillmentAssociation(1L, 2L, "MWH.001"),
            new FulfillmentAssociation(1L, 3L, "MWH.001"),
            new FulfillmentAssociation(1L, 4L, "MWH.001"),
            new FulfillmentAssociation(1L, 5L, "MWH.001"));

    assertDoesNotThrow(
        () ->
            validator.validateConstraints(
                1L, 3L, "MWH.001", List.of(), List.of(), warehouseAssocs));
  }
}
