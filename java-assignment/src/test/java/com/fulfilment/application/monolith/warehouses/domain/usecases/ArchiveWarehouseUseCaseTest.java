package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  public void testArchiveWarehouseSuccess() {
    Warehouse warehouse = new Warehouse("MWH.001", "ZWOLLE-001", 100, 10);

    useCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  public void testArchiveWarehouseNullShouldFail() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));
    assertEquals("Warehouse to archive cannot be null.", ex.getMessage());
  }
}
