package be.idamf.android.tamperdetection.pinning;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Don't use an X509TrustManager that accepts all certificates.
 */
public class TrustAllTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // trust all!
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // trust all!
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
