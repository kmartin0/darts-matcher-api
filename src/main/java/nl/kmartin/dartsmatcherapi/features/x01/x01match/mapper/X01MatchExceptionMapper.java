package nl.kmartin.dartsmatcherapi.features.x01.x01match.mapper;

import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.response.ErrorTargets;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegAlreadyWonException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import org.springframework.stereotype.Component;

/**
 * Maps supported X01 match domain exceptions to localized request-field errors.
 * Unrecognized exceptions are returned unchanged.
 */
@Component
public class X01MatchExceptionMapper {
    private final MessageResolver messageResolver;

    public X01MatchExceptionMapper(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    /**
     * Converts a supported domain exception to an invalid-arguments exception.
     *
     * @param exception the exception to map
     * @return the mapped exception, or the original exception when no mapping applies
     */
    public RuntimeException map(RuntimeException exception) {
        if (exception instanceof X01TurnAlreadyExistsException) {
            return mapTurnAlreadyExistsException();
        }

        if (exception instanceof X01CheckoutInsufficientDartsException e) {
            return mapCheckoutInsufficientDartsException(e);
        }

        if (exception instanceof X01LegAlreadyWonException) {
            return mapLegAlreadyWonException();
        }

        return exception;
    }

    /**
     * Maps an existing-turn domain error to the corresponding request-field error.
     *
     * @return the mapped invalid-arguments exception
     */
    private InvalidArgumentsException mapTurnAlreadyExistsException() {
        return new InvalidArgumentsException(
                new TargetError(
                        ErrorTargets.SCORE,
                        messageResolver.getMessage(MessageKeys.MESSAGE_X01_TURN_ALREADY_EXISTS)
                )
        );
    }

    /**
     * Maps an already-won leg error to the corresponding request-field error.
     *
     * @return the mapped invalid-arguments exception
     */
    private InvalidArgumentsException mapLegAlreadyWonException() {
        return new InvalidArgumentsException(
                new TargetError(
                        ErrorTargets.SCORE,
                        messageResolver.getMessage(MessageKeys.MESSAGE_LEG_ALREADY_WON)
                )
        );
    }

    /**
     * Maps an insufficient checkout dart count to the corresponding request-field error.
     *
     * @param exception the checkout validation exception
     * @return the mapped invalid-arguments exception
     */
    private InvalidArgumentsException mapCheckoutInsufficientDartsException(X01CheckoutInsufficientDartsException exception) {
        return new InvalidArgumentsException(
                new TargetError(
                        ErrorTargets.CHECKOUT_DARTS_USED,
                        messageResolver.getMessage(
                                MessageKeys.MESSAGE_IMPOSSIBLE_CHECKOUT_MIN_DARTS,
                                exception.getScore(),
                                exception.getDartsUsed()
                        )
                )
        );
    }
}
