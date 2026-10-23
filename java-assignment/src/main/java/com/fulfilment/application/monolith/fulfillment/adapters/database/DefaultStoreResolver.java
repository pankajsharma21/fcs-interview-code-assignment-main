package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.ports.StoreResolver;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Adapter implementing StoreResolver using Panache Active Record.
 */
@ApplicationScoped
public class DefaultStoreResolver implements StoreResolver {

  @Override
  public boolean existsById(Long storeId) {
    if (storeId == null) {
      return false;
    }
    return Store.findById(storeId) != null;
  }
}
