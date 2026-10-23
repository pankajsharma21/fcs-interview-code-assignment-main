package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.ports.ProductResolver;
import com.fulfilment.application.monolith.products.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Adapter implementing ProductResolver using ProductRepository.
 */
@ApplicationScoped
public class DefaultProductResolver implements ProductResolver {

  private final ProductRepository productRepository;

  @Inject
  public DefaultProductResolver(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public boolean existsById(Long productId) {
    if (productId == null || productRepository == null) {
      return false;
    }
    return productRepository.findById(productId) != null;
  }
}
