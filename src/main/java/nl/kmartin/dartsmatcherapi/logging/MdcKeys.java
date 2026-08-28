package nl.kmartin.dartsmatcherapi.logging;

/**
 * Constants for keys stored in the Mapped Diagnostic Context (MDC).
 */
public final class MdcKeys {

    private MdcKeys() {
    }

    public static final String CORRELATION_ID = "correlationId";
    public static final String CLIENT_IP = "clientIpKey";
}
