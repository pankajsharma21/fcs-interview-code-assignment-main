package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject WarehouseRepository repository;

  @org.junit.jupiter.api.AfterEach
  @Transactional
  public void cleanUp() {
    repository.delete("businessUnitCode like 'MWH.REPO.%' or businessUnitCode like 'MWH.UPDATE.%' or businessUnitCode like 'MWH.REMOVE.%'");
  }

  @Test
  public void testGetAllReturnsOnlyActive() {
    List<Warehouse> all = repository.getAll();
    assertNotNull(all);
    for (Warehouse w : all) {
      assertNull(w.archivedAt);
    }
  }

  @Test
  @Transactional
  public void testCreateAndFindWarehouse() {
    Warehouse w = new Warehouse("MWH.REPO.TEST", "ZWOLLE-001", 35, 10);
    repository.create(w);
    assertNotNull(w.id);
    assertNotNull(w.createdAt);

    Warehouse foundByCode = repository.findByBusinessUnitCode("MWH.REPO.TEST");
    assertNotNull(foundByCode);
    assertEquals("ZWOLLE-001", foundByCode.location);

    Warehouse foundById = repository.findById(String.valueOf(w.id));
    assertNotNull(foundById);
    assertEquals("MWH.REPO.TEST", foundById.businessUnitCode);
  }

  @Test
  @Transactional
  public void testUpdateWarehouse() {
    Warehouse w = new Warehouse("MWH.UPDATE.TEST", "AMSTERDAM-001", 40, 10);
    repository.create(w);

    w.capacity = 55;
    w.stock = 15;
    repository.update(w);

    Warehouse updated = repository.findByBusinessUnitCode("MWH.UPDATE.TEST");
    assertNotNull(updated);
    assertEquals(55, updated.capacity);
    assertEquals(15, updated.stock);
  }

  @Test
  @Transactional
  public void testRemoveWarehouseSetsArchivedAt() {
    Warehouse w = new Warehouse("MWH.REMOVE.TEST", "TILBURG-001", 30, 10);
    repository.create(w);

    repository.remove(w);
    assertNotNull(w.archivedAt);

    Warehouse afterRemoval = repository.findByBusinessUnitCode("MWH.REMOVE.TEST");
    assertNull(afterRemoval);
  }

  @Test
  public void testNullAndBlankQueries() {
    assertNull(repository.findByBusinessUnitCode(null));
    assertNull(repository.findById((String) null));
    assertNull(repository.findById(""));
    assertNull(repository.findById("NON_EXISTENT"));
    assertTrue(repository.findActiveByLocation(null).isEmpty());
  }

  @Test
  public void testFindActiveByLocation() {
    List<Warehouse> amsterdamWarehouses = repository.findActiveByLocation("AMSTERDAM-001");
    assertNotNull(amsterdamWarehouses);
  }
}
