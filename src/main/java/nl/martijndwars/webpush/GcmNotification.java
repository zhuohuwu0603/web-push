package nl.martijndwars.webpush;

import org.json.JSONObject;

import java.util.Collections;

public class GcmNotification extends Notification {
    public GcmNotification(String endpoint) {
        super(endpoint, null, null);
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

    /**
     * Compute the body according to the downstream message syntax. For more
     * info, see https://goo.gl/ruf12B.
     *
     * @return
     */
    public String getBody() {
        return new JSONObject()
            .put("registration_ids", Collections.singletonList(getRegistrationId()))
            .toString();
    }
}
