package nl.kmartin.dartsmatcherapi.validator.validx01dartbotsettings;

import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class X01DartBotSettingsValidationTests {

    private static final String FIELD_DART_BOT_SETTINGS = "x01DartBotSettings";

    private ValidX01CreateDartBotSettingsValidator createDartBotSettingsValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private HibernateConstraintValidatorContext hibernateContext;

    @Mock
    private HibernateConstraintViolationBuilder hibernateConstraintViolationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderContext;

    @BeforeEach
    void setup() {
        createDartBotSettingsValidator = new ValidX01CreateDartBotSettingsValidator();
    }

    @Test
    public void testDartBotWithSettings_returnValid() {
        // Given
        X01DartBotSettings dartBotSettings = new X01DartBotSettings(26);

        // When
        String messageKey = X01DartBotSettingsValidation.getValidationMessage(
                PlayerType.DART_BOT,
                dartBotSettings
        );

        // Then
        assertNull(messageKey);
    }

    @Test
    public void testDartBotWithoutSettings_returnInvalid() {
        // When
        String messageKey = X01DartBotSettingsValidation.getValidationMessage(
                PlayerType.DART_BOT,
                null
        );

        // Then
        assertEquals(MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING, messageKey);
    }

    @Test
    public void testHumanWithoutSettings_returnValid() {
        // When
        String messageKey = X01DartBotSettingsValidation.getValidationMessage(
                PlayerType.HUMAN,
                null
        );

        // Then
        assertNull(messageKey);
    }

    @Test
    public void testHumanWithSettings_returnInvalid() {
        // Given
        X01DartBotSettings dartBotSettings = new X01DartBotSettings(26);

        // When
        String messageKey = X01DartBotSettingsValidation.getValidationMessage(
                PlayerType.HUMAN,
                dartBotSettings
        );

        // Then
        assertEquals(MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_HUMAN, messageKey);
    }

    @Test
    public void testCreatePlayerAdapterWithValidSettings_returnTrue() {
        // Given
        X01CreateMatchRequest.Player player = new X01CreateMatchRequest.Player(
                "Bot",
                PlayerType.DART_BOT,
                new X01DartBotSettings(26)
        );

        // When
        boolean result = createDartBotSettingsValidator.isValid(player, constraintValidatorContext);

        // Then
        assertTrue(result);
    }

    @Test
    public void testCreatePlayerAdapterWithInvalidSettings_addFieldViolation() {
        // Given
        X01CreateMatchRequest.Player player = new X01CreateMatchRequest.Player(
                "Bot",
                PlayerType.DART_BOT,
                null
        );

        when(constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class))
                .thenReturn(hibernateContext);
        when(hibernateContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(hibernateConstraintViolationBuilder);
        when(hibernateConstraintViolationBuilder.addPropertyNode(FIELD_DART_BOT_SETTINGS))
                .thenReturn(nodeBuilderContext);
        when(nodeBuilderContext.addConstraintViolation()).thenReturn(hibernateContext);

        // When
        boolean result = createDartBotSettingsValidator.isValid(player, constraintValidatorContext);

        // Then
        assertFalse(result);
        verify(hibernateContext).disableDefaultConstraintViolation();
        verify(hibernateContext).buildConstraintViolationWithTemplate(
                "{" + MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING + "}"
        );
        verify(hibernateConstraintViolationBuilder).addPropertyNode(FIELD_DART_BOT_SETTINGS);
        verify(nodeBuilderContext).addConstraintViolation();
    }
}