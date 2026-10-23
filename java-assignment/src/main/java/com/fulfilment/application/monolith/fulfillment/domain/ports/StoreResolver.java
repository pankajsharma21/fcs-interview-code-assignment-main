package com.fulfilment.application.monolith.fulfillment.domain.ports;

/**
 * Port for checking store existence from the fulfillment domain.
 */
public interface StoreResolver {

  boolean existsById(Long storeId);
}
