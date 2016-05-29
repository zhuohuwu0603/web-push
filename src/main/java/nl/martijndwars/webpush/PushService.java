package nl.martijndwars.webpush;

import com.google.common.io.BaseEncoding;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.json.JSONObject;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PushService {
    private ExecutorService threadpool = Executors.newFixedThreadPool(1);

    private String gcmApiKey;

    public PushService() {
    }

    public PushService(String gcmApiKey) {
        this.gcmApiKey = gcmApiKey;
    }

    /**
     * Encrypt the payload using the user's public key using Elliptic Curve
     * Diffie Hellman cryptography over the prime256v1 curve.
     *
     * @return An Encrypted object containing the public key, salt, and
     * ciphertext, which can be sent to the other party.
     */
    public static Encrypted encrypt(byte[] buffer, PublicKey userPublicKey, byte[] userAuth) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
        keyPairGenerator.initialize(parameterSpec);

        KeyPair serverKey = keyPairGenerator.generateKeyPair();

        Map<String, KeyPair> keys = new HashMap<>();
        keys.put("server-key-id", serverKey);

        Map<String, String> labels = new HashMap<>();
        labels.put("server-key-id", "P-256");

        byte[] salt = SecureRandom.getSeed(16);

        HttpEce httpEce = new HttpEce(keys, labels);
        byte[] ciphertext = httpEce.encrypt(buffer, salt, null, "server-key-id", userPublicKey, userAuth, 2);

        return new Encrypted.Builder()
            .withSalt(salt)
            .withPublicKey(serverKey.getPublic())
            .withCiphertext(ciphertext)
            .build();
    }

    /**
     * Send a notification
     */
    public Future<Content> send(Notification notification) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        BaseEncoding base64url = BaseEncoding.base64Url();
        BaseEncoding base64 = BaseEncoding.base64();

        Encrypted encrypted = encrypt(
            notification.getPayload(),
            notification.getUserPublicKey(),
            notification.getUserAuth()
        );

        byte[] dh = Utils.savePublicKey((ECPublicKey) encrypted.getPublicKey());
        byte[] salt = encrypted.getSalt();

        Request request = Request
            .Post(notification.getEndpoint())
            .addHeader("TTL", String.valueOf(notification.getTTL()));

        if (notification instanceof GcmNotification) {
            if (null == gcmApiKey) {
                throw new IllegalStateException("GCM API key required for using Google Cloud Messaging");
            }

            String body = new JSONObject()
                .put("registration_ids", Collections.singletonList(((GcmNotification) notification).getRegistrationId()))
                .put("raw_data", base64.encode(encrypted.getCiphertext()))
                .toString();

            request
                .addHeader("Authorization", "key=" + gcmApiKey)
                .addHeader("Encryption", "keyid=p256dh;salt=" + base64url.encode(salt))
                .addHeader("Crypto-Key", "dh=" + base64url.encode(dh))
                .addHeader("Content-Encoding", "aesgcm")
                .bodyString(body, ContentType.APPLICATION_JSON);
        } else {
            request
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Content-Encoding", "aesgcm128")
                .addHeader("Encryption-Key", "keyid=p256dh;dh=" + base64url.encode(dh))
                .addHeader("Encryption", "keyid=p256dh;salt=" + base64url.encode(salt))
                .bodyByteArray(encrypted.getCiphertext());
        }

        Async async = Async.newInstance().use(threadpool);

        return async.execute(request);
    }
}
