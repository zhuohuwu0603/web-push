package nl.martijndwars.webpush;

import java.security.PublicKey;

public class GcmNotification extends Notification {
    public GcmNotification(String endpoint, PublicKey userPublicKey, byte[] userAuth, byte[] payload) {
        super(endpoint, userPublicKey, userAuth, payload);
    }

    @Override
    public String getEndpoint() {
        return "https://android.googleapis.com/gcm/send";
    }

    /**
     * Extract registration ID from a Google Cloud Messaging endpoint
     *
     * @return
     */
    public String getRegistrationId() {
        return super.getEndpoint().substring(super.getEndpoint().lastIndexOf("/") + 1);
    }

    @Override
    public int getPadSize() {
        return 2;
    }
}
