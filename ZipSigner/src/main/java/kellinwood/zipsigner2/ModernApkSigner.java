package kellinwood.zipsigner2;

import android.util.Log;

import com.android.apksig.ApkSigner;
import com.android.apksig.apk.ApkFormatException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modern APK signing using Google's apksig library.
 * Supports v1 (JAR), v2, v3, and v4 signature schemes required by modern Android.
 * 
 * This replaces the old v1-only JAR signing for apps targeting API 30+.
 */
public class ModernApkSigner {
    private static final String TAG = "ModernApkSigner";

    /**
     * Sign an APK with v1+v2+v3 signatures.
     * 
     * @param inputApk Input APK file path
     * @param outputApk Output APK file path
     * @param keystoreFile Keystore file (JKS/BKS/PKCS12)
     * @param keystorePassword Keystore password
     * @param keyAlias Key alias in the keystore
     * @param keyPassword Key password (can be same as keystore password)
     * @throws Exception if signing fails
     */
    public static void sign(
            File inputApk,
            File outputApk,
            File keystoreFile,
            String keystorePassword,
            String keyAlias,
            String keyPassword
    ) throws Exception {
        if (!inputApk.exists()) {
            throw new IOException("Input APK does not exist: " + inputApk);
        }

        // Load the keystore
        KeyStore keyStore = loadKeyStore(keystoreFile, keystorePassword);

        // Get the private key and certificate chain
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
        if (privateKey == null) {
            throw new GeneralSecurityException("Key not found for alias: " + keyAlias);
        }

        java.security.cert.Certificate[] certChain = keyStore.getCertificateChain(keyAlias);
        if (certChain == null || certChain.length == 0) {
            throw new GeneralSecurityException("Certificate chain not found for alias: " + keyAlias);
        }

        // Convert certificate chain to X509Certificate list
        List<X509Certificate> certs = new ArrayList<>();
        for (java.security.cert.Certificate cert : certChain) {
            if (cert instanceof X509Certificate) {
                certs.add((X509Certificate) cert);
            }
        }

        // Configure the signer
        ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder(
                keyAlias,
                privateKey,
                certs
        ).build();

        // Build and configure the ApkSigner
        ApkSigner.Builder signerBuilder = new ApkSigner.Builder(Collections.singletonList(signerConfig))
                .setInputApk(inputApk)
                .setOutputApk(outputApk)
                .setV1SigningEnabled(true)   // JAR signature (required for old Android)
                .setV2SigningEnabled(true)   // APK Signature Scheme v2 (Android 7.0+)
                .setV3SigningEnabled(true)   // APK Signature Scheme v3 (Android 9.0+)
                .setV4SigningEnabled(false); // v4 is optional, for incremental installs

        ApkSigner signer = signerBuilder.build();

        // Sign the APK
        Log.i(TAG, "Signing APK with v1+v2+v3 signatures: " + inputApk + " -> " + outputApk);
        signer.sign();
        Log.i(TAG, "APK signed successfully: " + outputApk);
    }

    /**
     * Load a keystore from file.
     * Auto-detects JKS, BKS, and PKCS12 formats.
     */
    private static KeyStore loadKeyStore(File keystoreFile, String password) throws Exception {
        if (!keystoreFile.exists()) {
            throw new IOException("Keystore file does not exist: " + keystoreFile);
        }

        // Try common keystore types
        String[] types = {"JKS", "PKCS12", "BKS"};
        Exception lastException = null;

        for (String type : types) {
            try {
                KeyStore keyStore;
                if ("BKS".equals(type)) {
                    // BKS requires BouncyCastle provider
                    keyStore = KeyStore.getInstance(type, "BC");
                } else {
                    keyStore = KeyStore.getInstance(type);
                }

                try (FileInputStream fis = new FileInputStream(keystoreFile)) {
                    keyStore.load(fis, password.toCharArray());
                }
                
                Log.i(TAG, "Loaded keystore as " + type + ": " + keystoreFile);
                return keyStore;
            } catch (Exception e) {
                lastException = e;
                // Try next type
            }
        }

        // All types failed
        throw new GeneralSecurityException(
                "Could not load keystore (tried JKS, PKCS12, BKS): " + keystoreFile,
                lastException
        );
    }
}
