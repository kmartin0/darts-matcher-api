package nl.kmartin.dartsmatcherapi.features;

import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.X01CheckoutServiceImpl;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class X01CheckoutTests {

    private IX01CheckoutService checkoutService;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setup() {
        // 1. Manually load the checkouts resource
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");

        // 2. Create the checkout service
        this.checkoutService = new X01CheckoutServiceImpl(checkoutsResource, messageResolver);
    }

    @Test
    void testReadCheckouts() {
        List<X01Checkout> checkouts = checkoutService.getCheckoutsAsList();
        Assertions.assertEquals(162, checkouts.size());
    }

}
