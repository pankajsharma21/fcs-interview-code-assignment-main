package com.fulfilment.application.monolith.stores;

public class StoreCreatedEvent {

  private final Store store;

  public StoreCreatedEvent(Store store) {
    Store copy = new Store();
    copy.id = store.id;
    copy.name = store.name;
    copy.quantityProductsInStock = store.quantityProductsInStock;
    this.store = copy;
  }

  public Store getStore() {
    return store;
  }
}
