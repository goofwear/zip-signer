package kellinwood.security.zipsigner.optional;

import kellinwood.security.zipsigner.KeySet;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * Updated: replaced deprecated org.spongycastle with org.bouncycastle.
 * Provider name changed from "SC" (SpongyCastle) to "BC" (BouncyCastle).
 */
public class SignatureBlockGenerator {

    /**
     * Sign the given content using the private and public keys from the keySet,
     * and return the encoded CMS (PKCS#7) data.
     * Use of direct signature and DER encoding produces a block verifiable by Android recovery.
     */
    public static byte[] generate(KeySet keySet, byte[] content) {
        try {
            List<Object> certList = new ArrayList<>();
            CMSTypedData msg = new CMSProcessableByteArray(content);
            certList.add(keySet.getPublicKey());

            Store<Object> certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            ContentSigner signer = new JcaContentSignerBuilder(keySet.getSignatureAlgorithm())
                    .setProvider("BC")   // was "SC" (SpongyCastle) — now "BC" (BouncyCastle)
                    .build(keySet.getPrivateKey());

            DigestCalculatorProvider digestProvider = new JcaDigestCalculatorProviderBuilder()
                    .setProvider("BC")
                    .build();

            JcaSignerInfoGeneratorBuilder signerBuilder =
                    new JcaSignerInfoGeneratorBuilder(digestProvider);
            signerBuilder.setDirectSignature(true);
            SignerInfoGenerator signerInfoGenerator =
                    signerBuilder.build(signer, keySet.getPublicKey());

            gen.addSignerInfoGenerator(signerInfoGenerator);
            gen.addCertificates(certs);

            CMSSignedData sigData = gen.generate(msg, false);
            return sigData.toASN1Structure().getEncoded("DER");

        } catch (Exception x) {
            throw new RuntimeException(x.getMessage(), x);
        }
    }
}
