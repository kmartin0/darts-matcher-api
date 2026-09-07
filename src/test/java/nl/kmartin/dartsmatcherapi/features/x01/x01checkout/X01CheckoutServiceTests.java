package nl.kmartin.dartsmatcherapi.features.x01.x01checkout;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.X01CheckoutServiceImpl;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class X01CheckoutServiceTests {

    private IX01CheckoutService checkoutService;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setup() {
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");
        checkoutService = new X01CheckoutServiceImpl(checkoutsResource, messageResolver);
    }

    @Test
    void getCheckoutsAsListReturnsAllConfiguredCheckouts() {
        List<X01Checkout> checkouts = checkoutService.getCheckoutsAsList();

        Assertions.assertEquals(162, checkouts.size());
    }

    @ParameterizedTest
    @CsvSource({
            "170, true",
            "169, false"
    })
    void getCheckoutReturnsExpectedResult(int remaining, boolean expectedPresent) {
        Optional<X01Checkout> checkout = checkoutService.getCheckout(remaining);

        Assertions.assertEquals(expectedPresent, checkout.isPresent());

        checkout.ifPresent(value ->
                Assertions.assertEquals(remaining, value.checkout())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "2, true",
            "40, true",
            "170, true",
            "1, false",
            "169, false",
            "171, false"
    })
    void isScoreCheckoutReturnsExpectedResult(int score, boolean expected) {
        Assertions.assertEquals(expected, checkoutService.isScoreCheckout(score));
    }

    @ParameterizedTest
    @CsvSource({
            "40, 1, true",
            "170, 3, true",
            "40, 0, false",
            "40, 4, false",
            "169, 3, false"
    })
    void isScoreCheckoutWithDartsUsedReturnsExpectedResult(int score, int dartsUsed, boolean expected) {
        Assertions.assertEquals(expected, checkoutService.isScoreCheckout(score, dartsUsed));
    }

    @Test
    void isScoreCheckoutWithDartsUsedThrowsWhenTooFewDartsWereUsed() {
        Assertions.assertThrows(
                X01CheckoutInsufficientDartsException.class,
                () -> checkoutService.isScoreCheckout(170, 2)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "-1, false",
            "0, true",
            "1, false"
    })
    void isRemainingZeroReturnsExpectedResult(int remaining, boolean expected) {
        Assertions.assertEquals(expected, checkoutService.isRemainingZero(remaining));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, true",
            "0, false",
            "1, true",
            "2, false"
    })
    void isRemainingBustReturnsExpectedResult(int remaining, boolean expected) {
        Assertions.assertEquals(expected, checkoutService.isRemainingBust(remaining));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, true",
            "0, true",
            "1, true",
            "2, false"
    })
    void isRemainingZeroOrBustReturnsExpectedResult(int remaining, boolean expected) {
        Assertions.assertEquals(expected, checkoutService.isRemainingZeroOrBust(remaining));
    }

    @ParameterizedTest
    @CsvSource({
            "0, DOUBLE, true",
            "0, DOUBLE_BULL, true",
            "20, DOUBLE, false",
            "0, OUTER_SINGLE, false"
    })
    void isValidCheckoutReturnsExpectedResult(int remaining, DartboardSectionArea area, boolean expected) {
        Dart lastDart = mock(Dart.class);
        when(lastDart.area()).thenReturn(area);

        Assertions.assertEquals(expected, checkoutService.isValidCheckout(remaining, lastDart));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "60, 441, null, true",
            "180, -39, null, false",
            "141, 0, 3, true",
            "141, 0, null, false",
            "169, 0, 3, false"
    }, nullValues = "null")
    void isTurnResultLegalReturnsExpectedResult(int score, int remaining, Integer checkoutDartsUsed, boolean expected) {
        Assertions.assertEquals(
                expected,
                checkoutService.isTurnResultLegal(score, remaining, checkoutDartsUsed)
        );
    }

    @Test
    void isTurnResultLegalThrowsWhenCheckoutUsesTooFewDarts() {
        Assertions.assertThrows(
                X01CheckoutInsufficientDartsException.class,
                () -> checkoutService.isTurnResultLegal(170, 0, 2)
        );
    }
}