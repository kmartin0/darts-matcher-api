package nl.kmartin.dartsmatcherapi.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves localized application messages using the current request locale.
 */
@Component
public class MessageResolver {
    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolves a localized message using the current locale.
     *
     * @param messageKey the message key
     * @param args       the message arguments
     * @return the resolved message, or a fallback message when the key cannot be found
     */
    public String getMessage(String messageKey, Object... args) {
        try {
            return messageSource.getMessage(
                    messageKey,
                    args,
                    LocaleContextHolder.getLocale()
            );
        } catch (NoSuchMessageException exception) {
            return "No message found.";
        }
    }
}