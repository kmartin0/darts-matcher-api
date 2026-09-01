package nl.kmartin.dartsmatcherapi.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

/**
 * Configures application localization, validation messages and the system clock.
 */
@Configuration
public class LocaleConfig {
    private static final Locale ENGLISH_LOCALE = Locale.ENGLISH;
    private static final Locale DUTCH_LOCALE = Locale.forLanguageTag("nl");

    private static final Locale DEFAULT_LOCALE = ENGLISH_LOCALE;

    private static final List<Locale> SUPPORTED_LOCALES = List.of(
            ENGLISH_LOCALE,
            DUTCH_LOCALE
    );

    /**
     * Creates the locale resolver using the configured supported and default locales.
     *
     * @return the configured locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(SUPPORTED_LOCALES);
        resolver.setDefaultLocale(DEFAULT_LOCALE);
        return resolver;
    }

    /**
     * Creates the message source used to resolve application messages.
     *
     * @return the configured message source
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Creates the validator using the application's localized validation messages.
     *
     * @param messageSource the application message source
     * @return the configured validator
     */
    @Bean
    @Primary
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    /**
     * Creates the clock used for application time calculations.
     *
     * @return the system clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}