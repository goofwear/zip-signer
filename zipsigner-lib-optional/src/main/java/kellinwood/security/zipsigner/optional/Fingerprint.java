package kellinwood.security.zipsigner.optional;

import kellinwood.logging.Logger;
import kellinwood.security.zipsigner.Base64;

import java.security.MessageDigest;

/**
 * Updated: removed org.spongycastle.util.encoders.HexTranslator dependency —
 * replaced with standard Java hex encoding (no external library needed).
 */
public class Fingerprint {

    static Logger logger = Logger.getLogger(Fingerprint.class);

    static byte[] calcDigest(String algorithm, byte[] encodedCert) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(encodedCert);
            return md.digest();
        } catch (Exception x) {
            logger.error(x.getMessage(), x);
            return null;
        }
    }

    /** Returns a colon-separated uppercase hex fingerprint, e.g. "AB:CD:EF:..." */
    public static String hexFingerprint(String algorithm, byte[] encodedCert) {
        try {
            byte[] digest = calcDigest(algorithm, encodedCert);
            if (digest == null) return null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                sb.append(String.format("%02X", digest[i] & 0xff));
                if (i != digest.length - 1) sb.append(':');
            }
            return sb.toString();
        } catch (Exception x) {
            logger.error(x.getMessage(), x);
            return null;
        }
    }

    public static String base64Fingerprint(String algorithm, byte[] encodedCert) {
        try {
            byte[] digest = calcDigest(algorithm, encodedCert);
            if (digest == null) return null;
            return Base64.encode(digest);
        } catch (Exception x) {
            logger.error(x.getMessage(), x);
            return null;
        }
    }
}
