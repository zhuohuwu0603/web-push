package nl.bytes.webpush;

import java.security.PublicKey;

public class Notification {
    private final String endpoint;
    private final PublicKey userPublicKey;
    private final byte[] payload;

    public Notification(final String endpoint, final PublicKey userPublicKey, final byte[] payload) {
        this.endpoint = endpoint;
        this.userPublicKey = userPublicKey;
        this.payload = payload;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public PublicKey getUserPublicKey() {
        return userPublicKey;
    }

    public byte[] getPayload() {
        return payload;
    }
}
