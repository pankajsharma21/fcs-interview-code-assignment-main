package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductEndpointTest {

  final String path = "product";

  @Test
  @Order(1)
  public void testListAllProducts() {
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  @Order(2)
  public void testGetSingleProductSuccess() {
    given()
        .when()
        .get(path + "/2")
        .then()
        .statusCode(200)
        .body("name", equalTo("KALLAX"), "stock", equalTo(5));
  }

  @Test
  @Order(3)
  public void testGetSingleProductNotFound() {
    given()
        .when()
        .get(path + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateProductSuccess() {
    Product product = new Product();
    product.name = "POÄNG";
    product.description = "Armchair";
    product.price = new BigDecimal("79.99");
    product.stock = 15;

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(path)
        .then()
        .statusCode(201)
        .body("name", equalTo("POÄNG"), "stock", equalTo(15));
  }

  @Test
  @Order(5)
  public void testCreateProductWithIdShouldFail() {
    Product product = new Product();
    product.id = 123L;
    product.name = "MALM";

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(path)
        .then()
        .statusCode(422);
  }

  @Test
  @Order(6)
  public void testUpdateProductSuccess() {
    Product updated = new Product();
    updated.name = "KALLAX_NEW";
    updated.description = "Updated shelving unit";
    updated.price = new BigDecimal("49.99");
    updated.stock = 20;

    given()
        .contentType(ContentType.JSON)
        .body(updated)
        .when()
        .put(path + "/2")
        .then()
        .statusCode(200)
        .body("name", equalTo("KALLAX_NEW"), "stock", equalTo(20));
  }

  @Test
  @Order(7)
  public void testUpdateProductWithoutNameShouldFail() {
    Product updated = new Product();
    updated.description = "Missing name";

    given()
        .contentType(ContentType.JSON)
        .body(updated)
        .when()
        .put(path + "/2")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(8)
  public void testUpdateProductNotFound() {
    Product updated = new Product();
    updated.name = "NONEXISTENT";

    given()
        .contentType(ContentType.JSON)
        .body(updated)
        .when()
        .put(path + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  public void testDeleteProductSuccess() {
    // Delete the TONSTAD (id = 1)
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")));
  }

  @Test
  @Order(10)
  public void testDeleteProductNotFound() {
    given()
        .when()
        .delete(path + "/99999")
        .then()
        .statusCode(404);
  }
}
