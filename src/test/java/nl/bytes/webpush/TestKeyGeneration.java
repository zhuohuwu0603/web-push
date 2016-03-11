package nl.bytes.webpush;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.junit.Assert;
import org.junit.Test;

import java.security.*;
import java.security.spec.ECPoint;

public class TestKeyGeneration {
    /**
     * http://stackoverflow.com/questions/6665353/public-key-length
     */
    @Test
    public void testKeyGeneration() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());

        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyPairGenerator.initialize(parameterSpec);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();


        //---
//        System.out.println(((BCECPublicKey) keyPair.getPublic()).getW().length);

        byte[] Q = ((BCECPublicKey) keyPair.getPublic()).getQ().getEncoded(false);
        System.out.println(Q.length);
//
//        byte[] Y = normed.YCoord.GetEncoded();
//
//        {
//            byte[] PO = new byte[X.Length + Y.Length + 1];
//            PO[0] = 0x04;
//            Array.Copy(X, 0, PO, 1, X.Length);
//            Array.Copy(Y, 0, PO, X.Length + 1, Y.Length);
//            return PO;
//        }
            //---


        Assert.assertEquals(150, keyPair.getPrivate().getEncoded().length);
        Assert.assertEquals(91, keyPair.getPublic().getEncoded().length);
    }
}
