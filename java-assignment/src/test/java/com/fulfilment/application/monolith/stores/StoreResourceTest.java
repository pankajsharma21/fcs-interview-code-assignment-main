package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreResourceTest {

  @InjectSpy
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  @Order(1)
  public void testListStores() {
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  @Order(2)
  public void testGetSingleStore() {
    given()
        .when()
        .get("/store/1")
        .then()
        .statusCode(200)
        .body("name", equalTo("TONSTAD"), "quantityProductsInStock", equalTo(10));
  }

  @Test
  @Order(3)
  public void testGetSingleStoreNotFound() {
    given()
        .when()
        .get("/store/99999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateStoreSuccessAndTriggersLegacySync() {
    Store newStore = new Store("HEMNES");
    newStore.quantityProductsInStock = 25;

    given()
        .contentType(ContentType.JSON)
        .body(newStore)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("name", equalTo("HEMNES"), "quantityProductsInStock", equalTo(25));

    verify(legacyStoreManagerGateway, atLeastOnce()).createStoreOnLegacySystem(any(Store.class));
  }

  @Test
  @Order(5)
  public void testCreateStoreWithIdShouldFail() {
    Store invalidStore = new Store("INVALID");
    invalidStore.id = 555L;

    given()
        .contentType(ContentType.JSON)
        .body(invalidStore)
        .when()
        .post("/store")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(6)
  public void testUpdateStoreSuccessAndTriggersLegacySync() {
    Store updatedStore = new Store("KALLAX_UPDATED");
    updatedStore.quantityProductsInStock = 50;

    given()
        .contentType(ContentType.JSON)
        .body(updatedStore)
        .when()
        .put("/store/2")
        .then()
        .statusCode(200)
        .body("name", equalTo("KALLAX_UPDATED"), "quantityProductsInStock", equalTo(50));

    verify(legacyStoreManagerGateway, atLeastOnce()).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  @Order(7)
  public void testUpdateStoreNotFound() {
    Store updatedStore = new Store("NON_EXISTING");
    updatedStore.quantityProductsInStock = 50;

    given()
        .contentType(ContentType.JSON)
        .body(updatedStore)
        .when()
        .put("/store/9999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(8)
  public void testPatchStoreSuccess() {
    Store patch = new Store("BESTÅ_PATCHED");
    patch.quantityProductsInStock = 99;

    given()
        .contentType(ContentType.JSON)
        .body(patch)
        .when()
        .patch("/store/3")
        .then()
        .statusCode(200)
        .body("name", equalTo("BESTÅ_PATCHED"), "quantityProductsInStock", equalTo(99));

    verify(legacyStoreManagerGateway, atLeastOnce()).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  @Order(9)
  public void testDeleteStore() {
    given()
        .when()
        .delete("/store/3")
        .then()
        .statusCode(204);

    given()
        .when()
        .get("/store/3")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(10)
  public void testDeleteStoreNotFound() {
    given()
        .when()
        .delete("/store/99999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(11)
  public void testErrorMapperDirect() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    jakarta.ws.rs.core.Response resp500 =
        mapper.toResponse(new RuntimeException("Simulated error"));
    org.junit.jupiter.api.Assertions.assertEquals(500, resp500.getStatus());

    jakarta.ws.rs.core.Response resp404 =
        mapper.toResponse(new jakarta.ws.rs.WebApplicationException("Not found", 404));
    org.junit.jupiter.api.Assertions.assertEquals(404, resp404.getStatus());
  }
}
