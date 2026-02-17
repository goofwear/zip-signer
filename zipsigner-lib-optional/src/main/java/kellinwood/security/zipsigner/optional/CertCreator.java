package kellinwood.security.zipsigner.optional;

import kellinwood.security.zipsigner.KeySet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;


/**
 * All methods create self-signed certificates.
 * Updated: replaced deprecated org.spongycastle + X509V3CertificateGenerator with
 * modern org.bouncycastle JcaX509v3CertificateBuilder API.
 */
public class CertCreator {

    /**
     * Creates a new keystore and self-signed key. RSA 2048, SHA256withRSA, 30 year validity.
     */
    public static void createKeystoreAndKey(String storePath, char[] password,
                                            String keyName,
                                            DistinguishedNameValues distinguishedNameValues) {
        createKeystoreAndKey(storePath, password, "RSA", 2048, keyName, password,
                "SHA256withRSA", 30, distinguishedNameValues);
    }

    public static KeySet createKeystoreAndKey(String storePath, char[] storePass,
                                              String keyAlgorithm, int keySize,
                                              String keyName, char[] keyPass,
                                              String certSignatureAlgorithm, int certValidityYears,
                                              DistinguishedNameValues distinguishedNameValues) {
        try {
            KeySet keySet = createKey(keyAlgorithm, keySize, keyName,
                    certSignatureAlgorithm, certValidityYears, distinguishedNameValues);

            KeyStore privateKS = KeyStoreFileManager.createKeyStore(storePath, storePass);
            privateKS.setKeyEntry(keyName, keySet.getPrivateKey(), keyPass,
                    new java.security.cert.Certificate[]{keySet.getPublicKey()});

            File sfile = new File(storePath);
            if (sfile.exists()) throw new IOException("File already exists: " + storePath);
            KeyStoreFileManager.writeKeyStore(privateKS, storePath, storePass);
            return keySet;
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new RuntimeException(x.getMessage(), x);
        }
    }

    public static KeySet createKey(String storePath, char[] storePass,
                                   String keyAlgorithm, int keySize,
                                   String keyName, char[] keyPass,
                                   String certSignatureAlgorithm, int certValidityYears,
                                   DistinguishedNameValues distinguishedNameValues) {
        try {
            KeySet keySet = createKey(keyAlgorithm, keySize, keyName,
                    certSignatureAlgorithm, certValidityYears, distinguishedNameValues);

            KeyStore privateKS = KeyStoreFileManager.loadKeyStore(storePath, storePass);
            privateKS.setKeyEntry(keyName, keySet.getPrivateKey(), keyPass,
                    new java.security.cert.Certificate[]{keySet.getPublicKey()});
            KeyStoreFileManager.writeKeyStore(privateKS, storePath, storePass);
            return keySet;
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new RuntimeException(x.getMessage(), x);
        }
    }

    public static KeySet createKey(String keyAlgorithm, int keySize, String keyName,
                                   String certSignatureAlgorithm, int certValidityYears,
                                   DistinguishedNameValues distinguishedNameValues) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
            keyPairGenerator.initialize(keySize);
            KeyPair kPair = keyPairGenerator.generateKeyPair();

            // Build subject X500Name from DistinguishedNameValues
            X500Name subject = distinguishedNameValues.getX500Name();

            // Generate a positive serial number
            BigInteger serialNumber = BigInteger.valueOf(Math.abs(new SecureRandom().nextInt()))
                    .add(BigInteger.ONE);

            Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L * 30L);
            Date notAfter  = new Date(System.currentTimeMillis() +
                    (1000L * 60L * 60L * 24L * 366L * certValidityYears));

            // Modern BouncyCastle certificate builder (replaces deprecated X509V3CertificateGenerator)
            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject,       // issuer  (self-signed → same as subject)
                    serialNumber,
                    notBefore,
                    notAfter,
                    subject,       // subject
                    kPair.getPublic()
            );

            ContentSigner signer = new JcaContentSignerBuilder(certSignatureAlgorithm)
                    .setProvider("BC")
                    .build(kPair.getPrivate());

            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certBuilder.build(signer));

            KeySet keySet = new KeySet();
            keySet.setName(keyName);
            keySet.setPrivateKey(kPair.getPrivate());
            keySet.setPublicKey(certificate);
            return keySet;

        } catch (Exception x) {
            throw new RuntimeException(x.getMessage(), x);
        }
    }
}
