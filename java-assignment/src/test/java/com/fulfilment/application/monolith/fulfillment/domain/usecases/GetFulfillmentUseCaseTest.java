package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetFulfillmentUseCaseTest {

  private FulfillmentStore fulfillmentStore;
  private GetFulfillmentUseCase useCase;

  @BeforeEach
  public void setUp() {
    fulfillmentStore = mock(FulfillmentStore.class);
    useCase = new GetFulfillmentUseCase(fulfillmentStore);
  }

  @Test
  public void testGetByStore() {
    List<FulfillmentAssociation> list =
        List.of(new FulfillmentAssociation(1L, 1L, 1L, "MWH.001"));
    when(fulfillmentStore.findByStore(1L)).thenReturn(list);

    List<FulfillmentAssociation> result = useCase.getByStore(1L);
    assertEquals(1, result.size());
  }

  @Test
  public void testGetByWarehouse() {
    List<FulfillmentAssociation> list =
        List.of(new FulfillmentAssociation(1L, 1L, 1L, "MWH.001"));
    when(fulfillmentStore.findByWarehouse("MWH.001")).thenReturn(list);

    List<FulfillmentAssociation> result = useCase.getByWarehouse("MWH.001");
    assertEquals(1, result.size());
  }
}
