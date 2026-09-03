package nl.kmartin.dartsmatcherapi.util;

/**
 * Provides utility methods for working with strings.
 */
public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Converts a PascalCase string to camelCase.
     *
     * @param input the string to convert
     * @return the camelCase string, or the original value when null or empty
     */
    public static String pascalToCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }
}