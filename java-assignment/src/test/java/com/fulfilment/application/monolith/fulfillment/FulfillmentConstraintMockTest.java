package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.validator.FulfillmentValidator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FulfillmentConstraintMockTest {

  private FulfillmentValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new FulfillmentValidator();
  }

  @Test
  public void testConstraint2Max3WarehousesPerStoreWithMock() {
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
                    1L, 1L, "MWH.004", List.of(), storeAssocs, List.of()));
    assertTrue(ex.getMessage().contains("already fulfilled by 3 different warehouses"));
  }

  @Test
  public void testConstraint3Max5ProductsPerWarehouseWithMock() {
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
}
