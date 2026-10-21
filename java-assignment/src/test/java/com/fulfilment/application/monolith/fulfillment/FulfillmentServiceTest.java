package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentServiceTest {

  @Inject FulfillmentService fulfillmentService;
  @Inject FulfillmentAssociationRepository repository;

  @BeforeEach
  @Transactional
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  public void testAssociateSuccess() {
    FulfillmentAssociation association = fulfillmentService.associate(1L, 1L, "MWH.001");
    assertNotNull(association);
    assertNotNull(association.id);
    assertEquals(1L, association.storeId);
    assertEquals(1L, association.productId);
    assertEquals("MWH.001", association.warehouseBusinessUnitCode);
  }

  @Test
  public void testAssociateNonExistentStoreShouldThrow404() {
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> fulfillmentService.associate(99999L, 1L, "MWH.001"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void testAssociateNonExistentProductShouldThrow404() {
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> fulfillmentService.associate(1L, 99999L, "MWH.001"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void testAssociateNonExistentWarehouseShouldThrow404() {
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> fulfillmentService.associate(1L, 1L, "MWH.NONEXISTENT"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void testConstraint1Max2WarehousesPerProductPerStore() {
    // Associate Product 1 to Store 1 from MWH.001 and MWH.012 (2 warehouses)
    fulfillmentService.associate(1L, 1L, "MWH.001");
    fulfillmentService.associate(1L, 1L, "MWH.012");

    // Attempting to associate a 3rd warehouse (MWH.023) for same (Store 1, Product 1) must violate Constraint 1
    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.associate(1L, 1L, "MWH.023"));
    assertTrue(ex.getMessage().contains("already fulfilled by 2 different warehouses"));
  }

  @Inject WarehouseStore warehouseStore;
  @Inject com.fulfilment.application.monolith.products.ProductRepository productRepository;

  @Test
  public void testConstraint2Max3WarehousesPerStore() {
    // Store 2 is fulfilled by MWH.001 (product 1), MWH.012 (product 2), MWH.023 (product 3)
    fulfillmentService.associate(2L, 1L, "MWH.001");
    fulfillmentService.associate(2L, 2L, "MWH.012");
    fulfillmentService.associate(2L, 3L, "MWH.023");

    // Create a 4th warehouse
    Warehouse w4 = new Warehouse("MWH.EXTRA", "AMSTERDAM-002", 50, 10);
    warehouseStore.create(w4);

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.associate(2L, 1L, "MWH.EXTRA"));
    assertTrue(ex.getMessage().contains("already fulfilled by 3 different warehouses"));
  }

  @Test
  @Transactional
  public void testConstraint3Max5ProductsPerWarehouse() {
    // Create products 4, 5, 6
    for (long i = 4; i <= 6; i++) {
      com.fulfilment.application.monolith.products.Product p =
          new com.fulfilment.application.monolith.products.Product();
      p.name = "PRODUCT_" + i;
      p.stock = 10;
      productRepository.persist(p);
    }

    // Associate products 1 to 5 to MWH.012
    fulfillmentService.associate(1L, 1L, "MWH.012");
    fulfillmentService.associate(1L, 2L, "MWH.012");
    fulfillmentService.associate(1L, 3L, "MWH.012");
    fulfillmentService.associate(1L, 4L, "MWH.012");
    fulfillmentService.associate(1L, 5L, "MWH.012");

    // Attempting to associate product 6 to MWH.012 violates constraint 3
    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.associate(1L, 6L, "MWH.012"));
    assertTrue(ex.getMessage().contains("maximally store 5 types of products"));
  }

  @Test
  public void testDisassociateSuccess() {
    fulfillmentService.associate(1L, 1L, "MWH.001");
    boolean removed = fulfillmentService.disassociate(1L, 1L, "MWH.001");
    assertTrue(removed);

    List<FulfillmentAssociation> associations = fulfillmentService.getAssociationsForStore(1L);
    assertTrue(associations.isEmpty());
  }
}
