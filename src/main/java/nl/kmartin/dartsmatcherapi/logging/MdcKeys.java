package nl.kmartin.dartsmatcherapi.logging;

/**
 * Defines the keys stored in the Mapped Diagnostic Context.
 */
public final class MdcKeys {
    private MdcKeys() {
    }

    public static final String CORRELATION_ID = "correlationId";
    public static final String CLIENT_IP = "clientIp";
}