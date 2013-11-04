package be.idamf.android.tamperdetection.task;

import android.content.Context;
import android.os.AsyncTask;

import be.idamf.android.tamperdetection.ui.EnvironmentCheckFragment;


/**
 * Environment-checking AsyncTask.
 */
public abstract class EnvironmentChecker extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    protected EnvironmentCheckFragment mCallbackFragment;

    public EnvironmentChecker(final Context context, final EnvironmentCheckFragment fragment) {
        mContext = context;
        mCallbackFragment = fragment;
    }

    public void setCallbackFragment(EnvironmentCheckFragment callbackFragment) {
        mCallbackFragment = callbackFragment;
    }

    public Context getContext() {
        return mContext;
    }

    public EnvironmentCheckFragment getCallbackFragment() {
        return mCallbackFragment;
    }
}
