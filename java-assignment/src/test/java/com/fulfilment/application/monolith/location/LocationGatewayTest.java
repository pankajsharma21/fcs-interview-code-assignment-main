package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  public void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    String identifier = "ZWOLLE-001";

    // when
    Location location = locationGateway.resolveByIdentifier(identifier);

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveAmsterdamLocationShouldReturn() {
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  public void testWhenResolveWithDifferentCaseShouldReturnMatch() {
    Location location = locationGateway.resolveByIdentifier("zwolle-001");
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    Location location = locationGateway.resolveByIdentifier("NON-EXISTING-999");
    assertNull(location);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  public void testWhenResolveBlankIdentifierShouldReturnNull(String blankIdentifier) {
    Location location = locationGateway.resolveByIdentifier(blankIdentifier);
    assertNull(location);
  }

  @Test
  public void testWhenResolveNullIdentifierShouldReturnNull() {
    Location location = locationGateway.resolveByIdentifier(null);
    assertNull(location);
  }
}
