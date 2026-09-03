package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.api;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01Checkout;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.rest.RestEndpoints;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes REST endpoints for retrieving X01 checkout information.
 */
@RestController
public class X01CheckoutRestController {

    private final IX01CheckoutService checkoutService;

    public X01CheckoutRestController(IX01CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Returns all available X01 checkouts.
     *
     * @return the available checkout configurations
     */
    @GetMapping(path = RestEndpoints.X01.CHECKOUTS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<X01Checkout> getCheckouts() {
        return checkoutService.getCheckoutsAsList();
    }

    /**
     * Returns the checkout configuration for the given remaining score.
     *
     * @param remaining the remaining score
     * @return the checkout configuration
     * @throws ResourceNotFoundException when no checkout exists for the remaining score
     */
    @GetMapping(path = RestEndpoints.X01.CHECKOUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Checkout getCheckout(@PathVariable int remaining) {
        return checkoutService
                .getCheckout(remaining)
                .orElseThrow(() -> new ResourceNotFoundException(X01Checkout.class, remaining));
    }
}