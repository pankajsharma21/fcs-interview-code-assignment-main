package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FulfillmentResourceTest {

  @Test
  @Order(1)
  public void testAssociateEndpointSuccess() {
    FulfillmentRequest request = new FulfillmentRequest(1L, 1L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/fulfillment/associate")
        .then()
        .statusCode(201)
        .body("storeId", equalTo(1), "productId", equalTo(1), "warehouseBusinessUnitCode", equalTo("MWH.001"));
  }

  @Test
  @Order(2)
  public void testAssociateEndpointStoreNotFound() {
    FulfillmentRequest request = new FulfillmentRequest(99999L, 1L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/fulfillment/associate")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(3)
  public void testAssociateEndpointProductNotFound() {
    FulfillmentRequest request = new FulfillmentRequest(1L, 99999L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/fulfillment/associate")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testAssociateEndpointWarehouseNotFound() {
    FulfillmentRequest request = new FulfillmentRequest(1L, 1L, "MWH.NONEXISTENT");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/fulfillment/associate")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(5)
  public void testGetAssociationsByStore() {
    given()
        .when()
        .get("/fulfillment/store/1")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  @Order(6)
  public void testGetAssociationsByWarehouse() {
    given()
        .when()
        .get("/fulfillment/warehouse/MWH.001")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  @Order(7)
  public void testDisassociateEndpointSuccess() {
    FulfillmentRequest request = new FulfillmentRequest(1L, 1L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/fulfillment/disassociate")
        .then()
        .statusCode(204);
  }

  @Test
  @Order(8)
  public void testDisassociateEndpointNotFound() {
    FulfillmentRequest request = new FulfillmentRequest(1L, 1L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/fulfillment/disassociate")
        .then()
        .statusCode(404);
  }
}
