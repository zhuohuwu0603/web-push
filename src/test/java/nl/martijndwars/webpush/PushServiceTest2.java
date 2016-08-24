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

public class PushServiceTest2 {
    @BeforeClass
    public static void addSecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testPushChrome() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, ExecutionException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());

        String gcmApiKey = "AIzaSyBqAYb9n4EnMkRIKWnd3adZc4r7txBkXPw";
        //String endpoint = "https://android.googleapis.com/gcm/send/fXYSGjgKDKA:APA91bE-sqRL3us1BfdzaF0E3DKh8fwVgEFEyT7BaUH7d6j-UauS_4cSRvhrdsbYFM4-6CWLfndYLU-FBqrlX_8xa793fSVUZxzHFz7Yec_cbtM-8J0NqKF0dUjh0mVBYWAwkWZYkmRU";
        String endpoint = "https://android.googleapis.com/gcm/send/fvTQzr9DFQM:APA91bHdgHWrGNDwBv54g3gm1qov7DcMSysDVoPQ8EqaSNbGeAZd9mvN9FcXuQkVaUSCT_ZPXnIMYSNIqihN-onlpuonKdxpIs-ooIEWscPbIzYKX_e_MGIf4vzoOy7ljgtRjjOnpVAr";
        //String encodedUserPublicKey = "BB5bKjcRawntzacxKXRVMhfS60h_48ZVHWZDTEbrVufrtwsol4dMNxKvGw8HSpd770MkWi76ovbBj_mJBiLQ1SA=";
        String encodedUserPublicKey = "BFibu83wALJYOqCWIzSKK5Ak6qSHcievsHZEsQv8sfLPPMVJC3oQp9ur64oHOl3mpplZEpNKHnXgPvPspCHclZM=";

        //String encodedUserAuth = "px9ZH3w7m8tk8zuJxmeEng==";
        String encodedUserAuth = "NAjTEJAvuwMPM_ihAmIl-g==";

        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);
        byte[] userAuth = BaseEncoding.base64Url().decode(encodedUserAuth);

        Notification notification = new GcmNotification(
            endpoint,
            userPublicKey,
            userAuth,
            "{\"title\": \"Hello\", \"message\": \"World\"}".getBytes()
        );

        PushService pushService = new PushService(gcmApiKey);
        Future<Content> httpResponse = pushService.send(notification);

        System.out.println(httpResponse.get().asString());

        int aaa = 1;
    }

    @Test
    public void testPushFirefox() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, ExecutionException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());

        String endpoint = "https://updates.push.services.mozilla.com/push/v1/gAAAAABXS0Nhothwqf0Je2mmjuRgyXjgVylY0yZ4qmP3cglrFoneY-XOLdJuGZOsv5Eh7ndhe8mMvge3VcLhpgbQ3w6_vWK7FZkSXhzjlaIxikL6cbW6Gok5BVw1tL1jqruy5Y-deSoz";
        String encodedUserPublicKey = "BNP6uzB5yqQDltCnO1snr-Qx3wLUPgeznuUQjfFbmehRHJK3s4eaqy04nOnm9796mceidVJPlFaobd94yjwtmpU=";

        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);

        Notification notification = new Notification(
            endpoint,
            userPublicKey,
            null,
            "{\"title\": \"Hello\", \"message\": \"World\"}".getBytes()
        );

        PushService pushService = new PushService();
        Future<Content> httpResponse = pushService.send(notification);

        System.out.println(httpResponse.get().asString());
    }
}
