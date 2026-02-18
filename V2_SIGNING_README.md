# APK Signature Scheme v2/v3 Support

## Current Status
ZipSigner currently only supports **APK Signature Scheme v1** (JAR signing). This was the only scheme available when ZipSigner was created in 2010.

**Android 7.0 (API 24)** introduced APK Signature Scheme v2, and **Android 11 (API 30+)** requires v2 or v3 for apps targeting SDK 30+.

## Why is this a problem?
When you build an APK with `targetSdk 35`, Android requires v2 or v3 signatures. ZipSigner's v1-only signature will cause this error:

```
Target SDK version 35 requires a minimum of signature scheme v2;
the APK is not signed with this or a later signature scheme
```

## Workaround: Use `apksigner` After ZipSigner

**Step 1**: Sign with ZipSigner (creates v1 signature)

**Step 2**: Add v2/v3 signature using `apksigner` from Android SDK:

```bash
# If you have Android SDK installed:
apksigner sign --ks your-keystore.jks \
  --ks-key-alias your-alias \
  --ks-pass pass:your-password \
  --key-pass pass:your-key-password \
  --v1-signing-enabled true \
  --v2-signing-enabled true \
  --v3-signing-enabled true \
  your-app.apk
```

**Or use the standalone apksigner.jar:**

Download from: https://github.com/patrickfav/uber-apk-signer/releases

```bash
java -jar uber-apk-signer.jar --apks your-app.apk \
  --ks your-keystore.jks \
  --ksAlias your-alias \
  --ksPass your-password \
  --ksKeyPass your-key-password
```

## For Custom Key Generation
When creating a key in ZipSigner, always choose **SHA256withRSA** or higher (not SHA1withRSA) from the signature algorithm dropdown. SHA1 is deprecated and may be rejected by app stores.

## Future Plans
Adding v2/v3 signing to ZipSigner requires integrating Google's `apksig` library — a major rewrite of the signing engine. This is planned for a future version but requires substantial development time.

## Alternative Tools
If you need v2/v3 signing directly on Android:
- **MT Manager** (file manager with APK signing)
- **APK Editor Studio** (desktop tool)
- **Universal ReVanced Manager** (https://github.com/Jman-Github/universal-revanced-manager)
