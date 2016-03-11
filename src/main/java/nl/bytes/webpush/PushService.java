package nl.bytes.webpush;

import com.google.common.io.BaseEncoding;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static Encrypted encrypt(PublicKey userPublicKey, byte[] payload) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
        keyPairGenerator.initialize(parameterSpec);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
//        KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
//        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
//        ECPoint point = ecSpec.getCurve().decodePoint(DatatypeConverter.parseHexBinary("04437ccef153da153724d07e33daff32ee812819af30a04d10e1b7ba515056dc1bc87a716cc0b19d7a1241a54f25e414a0f35ff8388b758cde36c9f9d7265d930e"));
//        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
//        PublicKey publicKey = kf.generatePublic(pubSpec);

        SecretKey secretKey = computeSecret(keyPair.getPrivate(), userPublicKey);
//        byte[] secretKeyBytes = new byte[16];//DatatypeConverter.parseHexBinary("0000000000000000000000000000000002000000000000000000000000000200");
//        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");

        byte[] salt = generateSalt(16);
//        byte[] salt = DatatypeConverter.parseHexBinary("c5d2ae41c080e0d49297c0d9d10ec062");
        byte[] ciphertext = encrypt(secretKey, salt, payload);

        return new Encrypted.Builder()
            .withPublicKey(publicKey)
            .withSalt(salt)
            .withCiphertext(ciphertext)
            .build();
    }

    /**
     * Compute the shared secret
     *
     * @param privateKey
     * @param publicKey
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static SecretKey computeSecret(PrivateKey privateKey, PublicKey publicKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);

        return keyAgreement.generateSecret("AES");
    }

    /**
     * Encrypt payload according to Encrypted Content-Encoding for HTTP. That
     * is, use aesgcm-128 with given secret key and salt.
     *
     * @param secret
     * @param salt
     * @param payload
     * @return
     */
    public static byte[] encrypt(SecretKey secret, byte[] salt, byte[] payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        byte[] key = hkdfExpand(secret.getEncoded(), salt, info("aesgcm128"), 16);
        byte[] nonce = hkdfExpand(secret.getEncoded(), salt, info("nonce"), 12);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(16 * 8, nonce));
        cipher.update(new byte[1]);
        cipher.update(payload);

        return cipher.doFinal();
    }

    /**
     * Future versions might require a null-terminated info string?
     *
     * @param base
     * @return
     */
    protected static byte[] info(String base) {
        String prefix = "Content-Encoding: ";

        return (prefix + base).getBytes();

        //return Arrays.copyOf((prefix + base).getBytes(), prefix.length() + base.length() + 1);
    }

    /**
     * Generate a random salt of length bytes
     *
     * @param length
     * @return
     */
    protected static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        new Random().nextBytes(salt);

        return salt;
    }

    /**
     * Convenience method for computing the HMAC Key Derivation Function. The
     * real work is offloaded to BouncyCastle.
     */
    protected static byte[] hkdfExpand(byte[] ikm, byte[] salt, byte[] info, int length) throws InvalidKeyException, NoSuchAlgorithmException {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(ikm, salt, info));

        byte[] okm = new byte[length];
        hkdf.generateBytes(okm, 0, length);

        return okm;
    }

    /**
     * Send a notification
     */
    public HttpResponse send(Notification notification) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        Request request = Request
            .Post(notification.getEndpoint())
            .addHeader("TTL", String.valueOf(notification.getTTL()));

        if (notification instanceof GcmNotification) {
            if (null == gcmApiKey) {
                throw new IllegalStateException("GCM API key required for using Google Cloud Messaging");
            }

            GcmNotification gcmNotification = ((GcmNotification) notification);

            request
                .addHeader("Authorization", "key=" + gcmApiKey)
                .addHeader("Accept", "application/json")
                .bodyString(gcmNotification.getBody(), ContentType.APPLICATION_JSON);
        } else {
            Encrypted encrypted = encrypt(notification.getUserPublicKey(), notification.getPayload());
            BaseEncoding encoder = BaseEncoding.base64Url();

            byte[] dh = ((BCECPublicKey) encrypted.getPublicKey()).getQ().getEncoded(false);
            byte[] salt = encrypted.getSalt();

            request
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Content-Encoding", "aesgcm128")
                .addHeader("Encryption-Key", "keyid=p256dh;dh=" + encoder.encode(dh))
                .addHeader("Encryption", "keyid=p256dh;salt=" + encoder.encode(salt))
                .bodyByteArray(encrypted.getCiphertext());
        }

        return request.execute().returnResponse();
    }
}
