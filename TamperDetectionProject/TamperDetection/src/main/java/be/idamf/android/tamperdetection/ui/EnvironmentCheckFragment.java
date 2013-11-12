package be.idamf.android.tamperdetection.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import be.idamf.android.tamperdetection.R;
import be.idamf.android.tamperdetection.tampering.PublicKeyInfo;
import be.idamf.android.tamperdetection.task.EnvironmentChecker;
import be.idamf.android.tamperdetection.util.TamperDetectionUtils;


/**
 * Fragment for environment checks.
 */
public class EnvironmentCheckFragment extends Fragment {

    /**
     * SHA1 of the debug signing certificate (obtained through keytool)
     */
    private static final String SIGNING_CERTIFICATE_SHA1 = "80D8648557CF1D3BA3C7C2F21ACD2A423F1223AD";

    private CheckBox mPlaystoreCheckBox;
    private CheckBox mDebuggableCheckBox;
    private CheckBox mRunningInEmulator;
    private CheckBox mSigningKeyCheckBox;
    private LinearLayout mSignatureInfo;
    private TextView mSignatureInfoSubject;
    private TextView mSignatureInfoIssuer;
    private TextView mSignatureInfoValidity;
    private PlayStoreChecker mPlayStoreChecker;
    private DebuggingChecker mDebuggingChecker;
    private SigningKeyChecker mSigningKeyChecker;
    private SignatureInfoDumper mSignatureInfoDumper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment, container, false);
        Button button = (Button) view.findViewById(R.id.environment_check);
        button.setOnClickListener(environmentCheckButtonListener);
        mPlaystoreCheckBox = (CheckBox) view.findViewById(R.id.environment_playstore);
        mDebuggableCheckBox = (CheckBox) view.findViewById(R.id.environment_debuggable);
        mRunningInEmulator = (CheckBox) view.findViewById(R.id.environment_emulator);
        mSigningKeyCheckBox = (CheckBox) view.findViewById(R.id.environment_signing_key);
        mSignatureInfo = (LinearLayout) view.findViewById(R.id.environment_signature_info);
        mSignatureInfoSubject = (TextView) view.findViewById(R.id.environment_signature_subject);
        mSignatureInfoIssuer = (TextView) view.findViewById(R.id.environment_signature_issuer);
        mSignatureInfoValidity = (TextView) view.findViewById(R.id.environment_signature_validity);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayStoreChecker != null) {
            mPlayStoreChecker.setCallbackFragment(null);
        }
        if (mDebuggingChecker != null) {
            mDebuggingChecker.setCallbackFragment(null);
        }
        if (mSigningKeyChecker != null) {
            mSigningKeyChecker.setCallbackFragment(null);
        }
    }

    private void setPlaystoreCheckResult(final Boolean playstoreInstalled) {
        setCheckResult(playstoreInstalled, mPlaystoreCheckBox);
    }

    private void setDebuggableCheckResult(final Boolean debuggable) {
        setCheckResult(debuggable, mDebuggableCheckBox);
    }

    private void setSigningKeyCheckResult(final Boolean validSigningKey) {
        setCheckResult(validSigningKey, mSigningKeyCheckBox);
    }

    private void setCheckResult(final Boolean enabled, final CheckBox checkBox) {
        checkBox.setEnabled(true);
        if (enabled) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
    }

    private void checkIfRunningInEmulator() {
        boolean runningInEmulator = TamperDetectionUtils.isRunningInEmulator();
        setCheckResult(runningInEmulator, mRunningInEmulator);
    }

    private void setPublicKeyInfo(final PublicKeyInfo publicKeyInfo) {
        mSignatureInfo.setVisibility(View.VISIBLE);
        mSignatureInfoSubject.setText(publicKeyInfo.getSubject());
        mSignatureInfoIssuer.setText(publicKeyInfo.getIssuer());
        StringBuilder sb = new StringBuilder("Valid from ");
        sb.append(DateFormat.format("dd-MM-yyyy", publicKeyInfo.getValidFrom()));
        sb.append(" to ");
        sb.append(DateFormat.format("dd-MM-yyyy", publicKeyInfo.getValidUntil()));
        mSignatureInfoValidity.setText(sb.toString());
    }

    private void doEnvironmentCheck() {
        mPlayStoreChecker = new PlayStoreChecker(getActivity(), EnvironmentCheckFragment.this);
        mPlayStoreChecker.execute();
        mDebuggingChecker = new DebuggingChecker(getActivity(), EnvironmentCheckFragment.this);
        mDebuggingChecker.execute();
        mSigningKeyChecker = new SigningKeyChecker(getActivity(), EnvironmentCheckFragment.this);
        mSigningKeyChecker.execute();
        mSignatureInfoDumper = new SignatureInfoDumper(getActivity(), EnvironmentCheckFragment.this);
        mSignatureInfoDumper.execute();
        checkIfRunningInEmulator();
    }

    View.OnClickListener environmentCheckButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            doEnvironmentCheck();
        }
    };

    static class PlayStoreChecker extends EnvironmentChecker {

        public PlayStoreChecker(Context context, EnvironmentCheckFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean playstore = TamperDetectionUtils.isInstalledThroughPlayStore(getContext());
            return playstore;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setPlaystoreCheckResult(result);
            }
        }
    }

    static class DebuggingChecker extends EnvironmentChecker {

        public DebuggingChecker(Context context, EnvironmentCheckFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean debuggable = TamperDetectionUtils.isDebuggable(getContext(), true);
            return debuggable;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setDebuggableCheckResult(result);
            }
        }
    }

    static class SigningKeyChecker extends EnvironmentChecker {

        public SigningKeyChecker(Context context, EnvironmentCheckFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean validSigningKey = TamperDetectionUtils.isValidSigningKey(getContext(), SIGNING_CERTIFICATE_SHA1);
            return validSigningKey;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setSigningKeyCheckResult(result);
            }
        }
    }

    static class SignatureInfoDumper extends AsyncTask<Void, Void, PublicKeyInfo> {
        private Context mContext;
        protected EnvironmentCheckFragment mCallbackFragment;

        public SignatureInfoDumper(Context context, EnvironmentCheckFragment fragment) {
            mContext = context;
            mCallbackFragment = fragment;
        }

        public void setCallbackFragment(EnvironmentCheckFragment callbackFragment) {
            mCallbackFragment = callbackFragment;
        }

        @Override
        protected PublicKeyInfo doInBackground(Void... params) {
            PublicKeyInfo debugInfo = TamperDetectionUtils.getAppPublicKeyInfo(mContext);
            return debugInfo;
        }

        @Override
        protected void onPostExecute(PublicKeyInfo result) {
            if (mCallbackFragment != null) {
                mCallbackFragment.setPublicKeyInfo(result);
            }
        }
    }
}
