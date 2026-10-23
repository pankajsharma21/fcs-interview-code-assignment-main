package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.events.FulfillmentDisassociatedEvent;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DisassociateProductUseCaseTest {

  private FulfillmentStore fulfillmentStore;
  private Event<FulfillmentDisassociatedEvent> eventPublisher;
  private DisassociateProductUseCase useCase;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    fulfillmentStore = mock(FulfillmentStore.class);
    eventPublisher = mock(Event.class);
    useCase = new DisassociateProductUseCase(fulfillmentStore, eventPublisher);
  }

  @Test
  public void testDisassociateWhenFound() {
    when(fulfillmentStore.delete(1L, 2L, "MWH.001")).thenReturn(true);

    boolean result = useCase.disassociate(1L, 2L, "MWH.001");

    assertTrue(result);
    verify(eventPublisher).fire(any(FulfillmentDisassociatedEvent.class));
  }

  @Test
  public void testDisassociateWhenNotFound() {
    when(fulfillmentStore.delete(1L, 2L, "MWH.001")).thenReturn(false);

    boolean result = useCase.disassociate(1L, 2L, "MWH.001");

    assertFalse(result);
    verify(eventPublisher, never()).fire(any());
  }
}
