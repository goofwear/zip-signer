package kellinwood.security.zipsigner.optional;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Helper class for dealing with the distinguished name RDNs.
 * Updated: replaced deprecated org.spongycastle with org.bouncycastle and
 * replaced deprecated X509Principal with X500Name (modern BouncyCastle API).
 */
public class DistinguishedNameValues extends LinkedHashMap<ASN1ObjectIdentifier, String> {

    public DistinguishedNameValues() {
        put(BCStyle.C, null);
        put(BCStyle.ST, null);
        put(BCStyle.L, null);
        put(BCStyle.STREET, null);
        put(BCStyle.O, null);
        put(BCStyle.OU, null);
        put(BCStyle.CN, null);
    }

    public String put(ASN1ObjectIdentifier oid, String value) {
        if (value != null && value.equals("")) value = null;
        if (containsKey(oid)) super.put(oid, value); // preserve original ordering
        else super.put(oid, value);
        return value;
    }

    public void setCountry(String country)                   { put(BCStyle.C, country); }
    public void setState(String state)                       { put(BCStyle.ST, state); }
    public void setLocality(String locality)                 { put(BCStyle.L, locality); }
    public void setStreet(String street)                     { put(BCStyle.STREET, street); }
    public void setOrganization(String organization)         { put(BCStyle.O, organization); }
    public void setOrganizationalUnit(String ou)             { put(BCStyle.OU, ou); }
    public void setCommonName(String commonName)             { put(BCStyle.CN, commonName); }

    @Override
    public int size() {
        int result = 0;
        for (String value : values()) {
            if (value != null) result += 1;
        }
        return result;
    }

    /** Returns a modern X500Name (replaces deprecated X509Principal). */
    public X500Name getX500Name() {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        for (Map.Entry<ASN1ObjectIdentifier, String> entry : entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                builder.addRDN(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    /** @deprecated Use {@link #getX500Name()} instead. Kept for binary compatibility. */
    @Deprecated
    public Object getPrincipal() {
        return getX500Name();
    }
}
