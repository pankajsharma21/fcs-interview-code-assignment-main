package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.AssociateProductOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.DisassociateProductOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.GetFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * High-level application facade providing a backward-compatible programmatic API for Fulfillment operations.
 * Delegates to hexagonal use cases and inbound ports.
 */
@ApplicationScoped
public class FulfillmentService {

  private final AssociateProductOperation associateOperation;
  private final DisassociateProductOperation disassociateOperation;
  private final GetFulfillmentOperation getFulfillmentOperation;

  @Inject
  public FulfillmentService(
      AssociateProductOperation associateOperation,
      DisassociateProductOperation disassociateOperation,
      GetFulfillmentOperation getFulfillmentOperation) {
    this.associateOperation = associateOperation;
    this.disassociateOperation = disassociateOperation;
    this.getFulfillmentOperation = getFulfillmentOperation;
  }

  public FulfillmentAssociation associate(
      Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return associateOperation.associate(storeId, productId, warehouseBusinessUnitCode);
  }

  public boolean disassociate(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return disassociateOperation.disassociate(storeId, productId, warehouseBusinessUnitCode);
  }

  public List<FulfillmentAssociation> getAssociationsForStore(Long storeId) {
    return getFulfillmentOperation.getByStore(storeId);
  }

  public List<FulfillmentAssociation> getAssociationsForWarehouse(String warehouseBuCode) {
    return getFulfillmentOperation.getByWarehouse(warehouseBuCode);
  }
}
