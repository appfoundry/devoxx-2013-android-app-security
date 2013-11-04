package be.idamf.android.tamperdetection.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import be.idamf.android.tamperdetection.R;
import be.idamf.android.tamperdetection.task.RootDetector;
import be.idamf.android.tamperdetection.util.RootDetectionUtils;


/**
 * Fragment for root detection.
 */
public class RootCheckFragment extends Fragment {

    private CheckBox mRootedCheckBox;
    private CheckBox mRootedSigningKeysCheckBox;
    private CheckBox mRootedBinariesCheckBox;
    private CheckBox mRootedProcessCheckBox;
    private RootedDetector mRootedDetector;
    private RootedSigningKeysDetector mRootedSigningKeysDetector;
    private RootedBinariesDetector mRootedBinariesDetector;
    private RootedProcessDetector mRootedProcessDetector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooted, container, false);
        Button button = (Button) view.findViewById(R.id.rooted_check);
        button.setOnClickListener(rootDetectionButtonListener);
        mRootedCheckBox = (CheckBox) view.findViewById(R.id.rooted_indicator);
        mRootedSigningKeysCheckBox = (CheckBox) view.findViewById(R.id.rooted_signing_keys);
        mRootedBinariesCheckBox = (CheckBox) view.findViewById(R.id.rooted_binaries);
        mRootedProcessCheckBox = (CheckBox) view.findViewById(R.id.rooted_process);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRootedDetector != null) {
            mRootedDetector.setCallbackFragment(null);
        }
        if (mRootedSigningKeysDetector != null) {
            mRootedSigningKeysDetector.setCallbackFragment(null);
        }
        if (mRootedBinariesDetector != null) {
            mRootedBinariesDetector.setCallbackFragment(null);
        }
        if (mRootedProcessDetector != null) {
            mRootedProcessDetector.setCallbackFragment(null);
        }
    }

    private void setRootedCheckResult(final Boolean rooted) {
        setCheckResult(rooted, mRootedCheckBox);
    }

    private void setRootedBinariesCheckResult(final Boolean rooted) {
        setCheckResult(rooted, mRootedBinariesCheckBox);
    }

    private void setRootedSigningKeysCheckResult(final Boolean rooted) {
        setCheckResult(rooted, mRootedSigningKeysCheckBox);
    }

    private void setRootedProcessCheckResult(final Boolean rooted) {
        setCheckResult(rooted, mRootedProcessCheckBox);
    }

    private void setCheckResult(final Boolean enabled, final CheckBox checkBox) {
        checkBox.setEnabled(true);
        if (enabled) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
    }

    private void doRootDetection() {
        mRootedDetector = new RootedDetector(RootCheckFragment.this);
        mRootedDetector.execute();
        mRootedSigningKeysDetector = new RootedSigningKeysDetector(RootCheckFragment.this);
        mRootedSigningKeysDetector.execute();
        mRootedBinariesDetector = new RootedBinariesDetector(RootCheckFragment.this);
        mRootedBinariesDetector.execute();
        mRootedProcessDetector = new RootedProcessDetector(RootCheckFragment.this);
        mRootedProcessDetector.execute();
    }

    View.OnClickListener rootDetectionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            doRootDetection();
        }
    };

    static class RootedDetector extends RootDetector {

        public RootedDetector(RootCheckFragment fragment) {
            super(fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean rooted = RootDetectionUtils.isRooted(true);
            return rooted;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setRootedCheckResult(result);
            }
        }
    }

    static class RootedSigningKeysDetector extends RootDetector {

        public RootedSigningKeysDetector(RootCheckFragment fragment) {
            super(fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean rooted = RootDetectionUtils.isRootedSigningKeys();
            return rooted;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setRootedSigningKeysCheckResult(result);
            }
        }
    }

    static class RootedBinariesDetector extends RootDetector {

        public RootedBinariesDetector(RootCheckFragment fragment) {
            super(fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean rooted = RootDetectionUtils.isRootedBinariesPresent();
            return rooted;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setRootedBinariesCheckResult(result);
            }
        }
    }

    static class RootedProcessDetector extends RootDetector {

        public RootedProcessDetector(RootCheckFragment fragment) {
            super(fragment);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean rooted = RootDetectionUtils.isRootedRunCommand();
            return rooted;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getCallbackFragment() != null) {
                mCallbackFragment.setRootedProcessCheckResult(result);
            }
        }
    }
}
