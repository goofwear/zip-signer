package kellinwood.zipsigner2.customkeys;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipboardManager;       // Modern (not deprecated android.text.ClipboardManager)
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;

import kellinwood.logging.android.AndroidLogManager;
import kellinwood.logging.android.AndroidLogger;
import kellinwood.security.zipsigner.optional.Fingerprint;
import kellinwood.security.zipsigner.optional.KeyStoreFileManager;
import kellinwood.zipsigner2.R;

/**
 * Updated:
 *  - Replaced deprecated org.spongycastle with org.bouncycastle
 *  - Replaced removed X509Principal / PrincipalUtil / X509Name with modern X500Name API
 *  - Replaced deprecated android.text.ClipboardManager with android.content.ClipboardManager
 */
public class KeysPropertiesActivity extends AppCompatActivity {

    AndroidLogger logger = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = AndroidLogManager.getAndroidLogger(KeysPropertiesActivity.class);
        logger.setToastContext(getBaseContext());
        logger.setInfoToastEnabled(true);

        setContentView(R.layout.key_properties);

        String keystorePath = getIntent().getStringExtra(KeyParameters.KEYSTORE_FILENAME);
        String keyName      = getIntent().getStringExtra(KeyParameters.KEY_NAME);
        String keyPass      = getIntent().getStringExtra(KeyParameters.KEY_PASSWORD);

        TextView v = (TextView) findViewById(R.id.KeyName);
        v.setText(keyName);

        v = (TextView) findViewById(R.id.KeystoreFilename);
        v.setText(keystorePath);

        try {
            KeyStore.PrivateKeyEntry entry =
                    (KeyStore.PrivateKeyEntry) KeyStoreFileManager.getKeyEntry(
                            keystorePath, null, keyName, keyPass);

            PrivateKey privateKey = entry.getPrivateKey();

            v = (TextView) findViewById(R.id.KeyType);
            v.setText(privateKey.getAlgorithm());

            v = (TextView) findViewById(R.id.KeyFormat);
            v.setText(privateKey.getFormat());

            v = (TextView) findViewById(R.id.KeySize);
            if (privateKey instanceof RSAPrivateCrtKey) {
                v.setText(Integer.toString(
                        ((RSAPrivateCrtKey) privateKey).getModulus().bitLength()));
            } else {
                v.setText("?");
            }

            Certificate certificate = entry.getCertificate();
            if (certificate instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate) certificate;

                v = (TextView) findViewById(R.id.Expires);
                v.setText(x509.getNotAfter().toString());

                // Modern API: parse subject via BouncyCastle X500Name
                X500Name subjectName = new X500Name(
                        x509.getSubjectX500Principal().getName());
                LinearLayout subjectDnLayout =
                        (LinearLayout) findViewById(R.id.SubjectDnLinearLayout);
                renderDN(subjectName, subjectDnLayout);

                X500Name issuerName = new X500Name(
                        x509.getIssuerX500Principal().getName());
                v = (TextView) findViewById(R.id.SelfSigned);
                if (subjectName.equals(issuerName)) {
                    v.setText(getResources().getString(R.string.CertIsSelfSigned));
                    findViewById(R.id.IssuerDnLinearLayout).setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.GONE);
                    renderDN(issuerName,
                            (ViewGroup) findViewById(R.id.IssuerDnLinearLayout));
                }
            }

            byte[] encodedCert = certificate.getEncoded();

            // Modern ClipboardManager (android.content, not deprecated android.text)
            final ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            View.OnCreateContextMenuListener copyContextMenuListener =
                    (menu, v2, menuInfo) -> {
                        menu.setHeaderTitle((String) v2.getTag());
                        menu.add(0, v2.getId(), 0, R.string.CopyToClipboardMenuItemLabel);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(
                                    ClipData.newPlainText("fingerprint",
                                            ((TextView) v2).getText()));
                        }
                    };

            TextView md5View = (TextView) findViewById(R.id.MD5Fingerprint);
            md5View.setText(Fingerprint.hexFingerprint("MD5", encodedCert));
            md5View.setTag(getResources().getString(R.string.MD5FingerprintLabel));
            md5View.setOnCreateContextMenuListener(copyContextMenuListener);

            TextView sha1View = (TextView) findViewById(R.id.SHA1Fingerprint);
            sha1View.setText(Fingerprint.hexFingerprint("SHA1", encodedCert));
            sha1View.setTag(getResources().getString(R.string.SHA1FingerprintLabel));
            sha1View.setOnCreateContextMenuListener(copyContextMenuListener);

            TextView sha1KeyHashView = (TextView) findViewById(R.id.SHA1KeyHash);
            sha1KeyHashView.setText(Fingerprint.base64Fingerprint("SHA1", encodedCert));
            sha1KeyHashView.setTag(getResources().getString(R.string.SHA1KeyHashLabel));
            sha1KeyHashView.setOnCreateContextMenuListener(copyContextMenuListener);

        } catch (Exception x) {
            logger.error(x.getMessage(), x);
        }

        Button button = (Button) findViewById(R.id.BackButton);
        button.setOnClickListener(bv -> finish());
    }

    /**
     * Renders each RDN of an X500Name into text views within the given layout.
     * Replaces the old X509Principal.getOIDs()/getValues() + X509Name.DefaultSymbols approach.
     */
    void renderDN(X500Name x500Name, ViewGroup layout) {
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (RDN rdn : x500Name.getRDNs()) {
            if (rdn.isMultiValued()) {
                for (org.bouncycastle.asn1.x500.AttributeTypeAndValue atv : rdn.getTypesAndValues()) {
                    addRdnView(inflater, layout, atv.getType(), atv.getValue().toString());
                }
            } else {
                org.bouncycastle.asn1.x500.AttributeTypeAndValue atv = rdn.getFirst();
                addRdnView(inflater, layout, atv.getType(), IETFUtils.valueToString(atv.getValue()));
            }
        }
    }

    private void addRdnView(LayoutInflater inflater, ViewGroup layout,
                            ASN1ObjectIdentifier oid, String value) {
        // Get a human-readable symbol for the OID (e.g. CN, O, C, ST, …)
        String symbol = BCStyle.INSTANCE.oidToDisplayName(oid);
        if (symbol == null) symbol = oid.getId();

        TextView tv = (TextView) inflater.inflate(R.layout.text_view, null);
        tv.setText(symbol + "=" + value);
        layout.addView(tv);
    }
}
