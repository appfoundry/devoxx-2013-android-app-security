package be.idamf.android.tamperdetection.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import be.idamf.android.tamperdetection.R;

/**
 * Fragment for network checks.
 */
public class NetworkCheckFragment extends Fragment {

    public static final String HTTPS_TEST_URL = "https://android.maelbrancke.net";

    private static final String TAG = NetworkCheckFragment.class.getSimpleName();

    private NetworkChecker mNetworkChecker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        Button selfSignedCertButton = (Button) view.findViewById(R.id.network_self_signed_cert_button);
        selfSignedCertButton.setOnClickListener(networkSelfSignedCertCheck);
        Button regularNetworkCallButton = (Button) view.findViewById(R.id.network_regular_button);
        regularNetworkCallButton.setOnClickListener(networkRegularCallCheck);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNetworkChecker != null) {
            mNetworkChecker.setCallbackFragment(null);
        }
    }

    private void doNetworkingWithCustomCert() {
        mNetworkChecker = new NetworkChecker(getActivity(), NetworkCheckFragment.this);
        mNetworkChecker.execute(true);
    }

    private void doNetworkingWithoutCustomCert() {
        mNetworkChecker = new NetworkChecker(getActivity(), NetworkCheckFragment.this);
        mNetworkChecker.execute(false);
    }

    private void showNetworkingResult(final String result) {
        Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
    }

    private static SSLSocketFactory newSslSocketFactory(final Context context) {
        SSLSocketFactory sslSocketFactory = null;
        try {
            KeyStore trustSelfSignedKeyStore = KeyStore.getInstance("BKS");
            trustSelfSignedKeyStore.load( context.getResources().openRawResource(R.raw.truststore), "secretpassword".toCharArray() );
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustSelfSignedKeyStore);

            // create SslContext
            SSLContext selfSignedCertSslContext = SSLContext.getInstance("TLS");
            selfSignedCertSslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            sslSocketFactory = selfSignedCertSslContext.getSocketFactory();
        } catch (KeyStoreException e) {
            Log.d(TAG, "Keystore problem: " + e.getMessage());
        } catch (CertificateException e) {
            Log.d(TAG, "Certificate problem: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "Algorithm problem: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "IO problem: " + e.getMessage());
        } catch (KeyManagementException e) {
            Log.d(TAG, "Key problem: " + e.getMessage());
        }
        return sslSocketFactory;
    }

    public static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    View.OnClickListener networkSelfSignedCertCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doNetworkingWithCustomCert();
        }
    };

    View.OnClickListener networkRegularCallCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doNetworkingWithoutCustomCert();
        }
    };

    static class NetworkChecker extends AsyncTask<Boolean, Void, String> {
        private Context mContext;
        private NetworkCheckFragment mCallbackFragment;

        public NetworkChecker(Context context, NetworkCheckFragment fragment) {
            mContext = context;
            mCallbackFragment = fragment;
        }

        public void setCallbackFragment(NetworkCheckFragment callbackFragment) {
            mCallbackFragment = callbackFragment;
        }

        @Override
        protected String doInBackground(Boolean... params) {
            boolean customSslSocketFactory = params[0];
            InputStream in = null;
            String result = null;
            try {
                URL url = new URL(HTTPS_TEST_URL);
                OkHttpClient okHttpClient = new OkHttpClient();
                if (customSslSocketFactory) {
                    okHttpClient.setSslSocketFactory(newSslSocketFactory(mContext));
                }
                HttpURLConnection connection = okHttpClient.open(url);
                in = connection.getInputStream();
                byte[] response = readFully(in);
                result = new String(response, "UTF-8");
            } catch (Exception e) {
                return e.getMessage();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mCallbackFragment != null) {
                mCallbackFragment.showNetworkingResult(s);
            }
        }
    }
}
