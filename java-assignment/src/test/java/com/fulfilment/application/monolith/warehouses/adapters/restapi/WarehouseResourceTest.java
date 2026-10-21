package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseResourceTest {

  final String path = "/warehouse";

  @Test
  @Order(1)
  public void testListAllWarehouses() {
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("MWH.012"),
            containsString("MWH.023"));
  }

  @Test
  @Order(2)
  public void testGetWarehouseByIdSuccess() {
    given()
        .when()
        .get(path + "/2")
        .then()
        .statusCode(200)
        .body(
            "businessUnitCode", equalTo("MWH.012"),
            "location", equalTo("AMSTERDAM-001"),
            "capacity", equalTo(50),
            "stock", equalTo(5));
  }

  @Test
  @Order(3)
  public void testGetWarehouseByIdNotFound() {
    given()
        .when()
        .get(path + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateWarehouseSuccess() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("MWH.999");
    warehouse.setLocation("HELMOND-001");
    warehouse.setCapacity(40);
    warehouse.setStock(15);

    given()
        .contentType(ContentType.JSON)
        .body(warehouse)
        .when()
        .post(path)
        .then()
        .statusCode(201)
        .body(
            "businessUnitCode", equalTo("MWH.999"),
            "location", equalTo("HELMOND-001"),
            "capacity", equalTo(40),
            "stock", equalTo(15));
  }

  @Test
  @Order(5)
  public void testCreateWarehouseDuplicateBusinessUnitCodeShouldFail() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("MWH.012");
    warehouse.setLocation("ZWOLLE-002");
    warehouse.setCapacity(30);
    warehouse.setStock(10);

    given()
        .contentType(ContentType.JSON)
        .body(warehouse)
        .when()
        .post(path)
        .then()
        .statusCode(400);
  }

  @Test
  @Order(6)
  public void testCreateWarehouseInvalidLocationShouldFail() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("MWH.INV");
    warehouse.setLocation("UNKNOWN-CITY");
    warehouse.setCapacity(30);
    warehouse.setStock(10);

    given()
        .contentType(ContentType.JSON)
        .body(warehouse)
        .when()
        .post(path)
        .then()
        .statusCode(400);
  }

  @Test
  @Order(7)
  public void testCreateWarehouseStockExceedsCapacityShouldFail() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("MWH.OVER");
    warehouse.setLocation("ZWOLLE-002");
    warehouse.setCapacity(20);
    warehouse.setStock(30);

    given()
        .contentType(ContentType.JSON)
        .body(warehouse)
        .when()
        .post(path)
        .then()
        .statusCode(400);
  }

  @Test
  @Order(8)
  public void testReplaceWarehouseSuccess() {
    // Replace MWH.012 (located at AMSTERDAM-001 with capacity 50, stock 5)
    Warehouse replacement = new Warehouse();
    replacement.setLocation("AMSTERDAM-001");
    replacement.setCapacity(60);
    replacement.setStock(5); // must match previous stock

    given()
        .contentType(ContentType.JSON)
        .body(replacement)
        .when()
        .post(path + "/MWH.012/replacement")
        .then()
        .statusCode(200)
        .body(
            "businessUnitCode", equalTo("MWH.012"),
            "location", equalTo("AMSTERDAM-001"),
            "capacity", equalTo(60),
            "stock", equalTo(5));
  }

  @Test
  @Order(9)
  public void testReplaceWarehouseStockMismatchShouldFail() {
    Warehouse replacement = new Warehouse();
    replacement.setLocation("AMSTERDAM-001");
    replacement.setCapacity(70);
    replacement.setStock(99); // does not match stock 5

    given()
        .contentType(ContentType.JSON)
        .body(replacement)
        .when()
        .post(path + "/MWH.012/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(10)
  public void testReplaceWarehouseNotFoundShouldFail() {
    Warehouse replacement = new Warehouse();
    replacement.setLocation("AMSTERDAM-001");
    replacement.setCapacity(50);
    replacement.setStock(5);

    given()
        .contentType(ContentType.JSON)
        .body(replacement)
        .when()
        .post(path + "/MWH.NONEXISTENT/replacement")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(11)
  public void testArchiveWarehouseSuccess() {
    // Archive warehouse 1 (MWH.001 at ZWOLLE-001)
    given()
        .when()
        .delete(path + "/1")
        .then()
        .statusCode(204);

    // After archiving, GET /warehouse should no longer list ZWOLLE-001 / MWH.001
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            not(containsString("MWH.001")),
            containsString("MWH.012"),
            containsString("MWH.023"));
  }

  @Test
  @Order(12)
  public void testArchiveWarehouseNotFound() {
    given()
        .when()
        .delete(path + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(13)
  public void testReplaceWarehouseInvalidLocation() {
    Warehouse replacement = new Warehouse();
    replacement.setLocation("NON-EXISTING-LOC");
    replacement.setCapacity(50);
    replacement.setStock(27);

    given()
        .contentType(ContentType.JSON)
        .body(replacement)
        .when()
        .post(path + "/MWH.023/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(14)
  public void testReplaceWarehouseCapacityExceedsLimit() {
    Warehouse replacement = new Warehouse();
    replacement.setLocation("TILBURG-001");
    replacement.setCapacity(9999);
    replacement.setStock(27);

    given()
        .contentType(ContentType.JSON)
        .body(replacement)
        .when()
        .post(path + "/MWH.023/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(15)
  public void testCreateWarehouseExceedsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("MWH.HUGE");
    warehouse.setLocation("ZWOLLE-001");
    warehouse.setCapacity(5000);
    warehouse.setStock(10);

    given()
        .contentType(ContentType.JSON)
        .body(warehouse)
        .when()
        .post(path)
        .then()
        .statusCode(400);
  }
}
