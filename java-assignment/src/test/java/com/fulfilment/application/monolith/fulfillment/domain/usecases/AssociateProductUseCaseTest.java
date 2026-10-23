package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.ProductResolver;
import com.fulfilment.application.monolith.fulfillment.domain.ports.StoreResolver;
import com.fulfilment.application.monolith.fulfillment.events.FulfillmentAssociatedEvent;
import com.fulfilment.application.monolith.fulfillment.validator.FulfillmentValidator;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.event.Event;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssociateProductUseCaseTest {

  private FulfillmentStore fulfillmentStore;
  private WarehouseStore warehouseStore;
  private StoreResolver storeResolver;
  private ProductResolver productResolver;
  private FulfillmentValidator validator;
  private Event<FulfillmentAssociatedEvent> eventPublisher;
  private AssociateProductUseCase useCase;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    fulfillmentStore = mock(FulfillmentStore.class);
    warehouseStore = mock(WarehouseStore.class);
    storeResolver = mock(StoreResolver.class);
    productResolver = mock(ProductResolver.class);
    validator = mock(FulfillmentValidator.class);
    eventPublisher = mock(Event.class);
    useCase =
        new AssociateProductUseCase(
            fulfillmentStore,
            warehouseStore,
            storeResolver,
            productResolver,
            validator,
            eventPublisher);
  }

  @Test
  public void testAssociateWhenAlreadyExists() {
    when(storeResolver.existsById(1L)).thenReturn(true);
    when(productResolver.existsById(2L)).thenReturn(true);

    FulfillmentAssociation existing = new FulfillmentAssociation(10L, 1L, 2L, "MWH.001");
    when(fulfillmentStore.findSpecific(1L, 2L, "MWH.001")).thenReturn(existing);

    FulfillmentAssociation result = useCase.associate(1L, 2L, "MWH.001");

    assertNotNull(result);
    assertEquals(10L, result.id);
  }

  @Test
  public void testAssociateNewSuccess() {
    when(storeResolver.existsById(1L)).thenReturn(true);
    when(productResolver.existsById(2L)).thenReturn(true);

    when(fulfillmentStore.findSpecific(1L, 2L, "MWH.001")).thenReturn(null);
    when(fulfillmentStore.findByProductAndStore(2L, 1L)).thenReturn(List.of());
    when(fulfillmentStore.findByStore(1L)).thenReturn(List.of());
    when(fulfillmentStore.findByWarehouse("MWH.001")).thenReturn(List.of());

    FulfillmentAssociation created = new FulfillmentAssociation(99L, 1L, 2L, "MWH.001");
    when(fulfillmentStore.save(any(FulfillmentAssociation.class))).thenReturn(created);

    FulfillmentAssociation result = useCase.associate(1L, 2L, "MWH.001");

    assertNotNull(result);
    assertEquals(99L, result.id);
    verify(validator).validateInput(1L, 2L, "MWH.001");
    verify(fulfillmentStore).save(any(FulfillmentAssociation.class));
    verify(eventPublisher).fire(any(FulfillmentAssociatedEvent.class));
  }
}
