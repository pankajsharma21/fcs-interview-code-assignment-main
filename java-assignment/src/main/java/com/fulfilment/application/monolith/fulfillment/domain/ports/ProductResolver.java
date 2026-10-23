package com.fulfilment.application.monolith.fulfillment.domain.ports;

/**
 * Port for checking product existence from the fulfillment domain.
 */
public interface ProductResolver {

  boolean existsById(Long productId);
}
