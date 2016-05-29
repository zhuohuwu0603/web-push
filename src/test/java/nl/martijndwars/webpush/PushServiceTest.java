package nl.martijndwars.webpush;

import com.google.common.io.BaseEncoding;
import org.apache.http.client.fluent.Content;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PushServiceTest {
    @BeforeClass
    public static void addSecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testPush() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, ExecutionException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());

        String gcmApiKey = "AIzaSyDSa2bw0b0UGOmkZRw-dqHGQRI_JqpiHug";
        String endpoint = "https://android.googleapis.com/gcm/send/fXYSGjgKDKA:APA91bE-sqRL3us1BfdzaF0E3DKh8fwVgEFEyT7BaUH7d6j-UauS_4cSRvhrdsbYFM4-6CWLfndYLU-FBqrlX_8xa793fSVUZxzHFz7Yec_cbtM-8J0NqKF0dUjh0mVBYWAwkWZYkmRU";
        String encodedUserPublicKey = "BB5bKjcRawntzacxKXRVMhfS60h_48ZVHWZDTEbrVufrtwsol4dMNxKvGw8HSpd770MkWi76ovbBj_mJBiLQ1SA=";
        String encodedUserAuth = "px9ZH3w7m8tk8zuJxmeEng==";

        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);
        byte[] userAuth = BaseEncoding.base64Url().decode(encodedUserAuth);

        Notification notification = new GcmNotification(
            endpoint,
            userPublicKey,
            userAuth,
            "Hello world!!".getBytes()
        );

        PushService pushService = new PushService(gcmApiKey);
        Future<Content> httpResponse = pushService.send(notification);

        System.out.println(httpResponse.get().asString());
    }
}
