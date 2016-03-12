package nl.martijndwars.webpush;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PushServiceTest {
    @BeforeClass
    public static void addSecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testEncryptedContentEncoding() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        SecretKey key = new SecretKeySpec(new byte[16], 0, 16, "AES");
        byte[] salt = new byte[16];
        byte[] cipher = PushService.encrypt(key, salt, new byte[1]);

        Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("d0590276a830b327a7c84b3b4afc4815a56d"), cipher);
    }

//    @Test
//    public void testEncrypt() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException, InvalidKeySpecException {
//        Encrypted encrypted = PushService.encrypt(null, new byte[1]);
//
//        Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("d097d77d70eac08845119c594cd7b3821b8d"), encrypted.getCiphertext());
//    }

    @Test
    public void testPush() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        Security.addProvider(new BouncyCastleProvider());

        KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECPoint point = ecSpec.getCurve().decodePoint(Base64.getDecoder().decode("BJ2VFQNZcyzdnIMNvtVz8ciMnfoQU4dGVQXx+5ebuw+i2YWF0VUSCHRUTwLSctKe4Q3LyrI27pHzJp+dRIbe3PQ="));
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
        PublicKey userPublicKey = kf.generatePublic(pubSpec);

        Notification notification = new Notification(
            "https://updates.push.services.mozilla.com/push/gAAAAABW0v7lVIWfioQS9IosPTDphc04psNhhw9wghX2w-W0dNKv7Pe1s31f7QDT_8w57ilBDEhiwGP52RewKthNbgV_6KxIL3H5p2RrUM45zRZySEoOEG-ZczkI3as4E3f8ay6x7yIyEHQbKbuskQQh7dmJB-FHEPJFt-cD-qeOUkuGSxNC9W0=",
            userPublicKey,
            new byte[1]
        );

        PushService pushService = new PushService("");
        HttpResponse httpResponse = pushService.send(notification);

        System.out.println(httpResponse.getStatusLine().getStatusCode());
    }
}
