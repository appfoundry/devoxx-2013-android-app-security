package be.idamf.android.tamperdetection.util;

import android.os.Build;

import java.io.File;

/**
 * Some common root detection utilities.
 */
public class RootDetectionUtils {

    /**
     * Try to determine whether running on a rooted device.
     *
     * @return true when the app seems to run on a rooted device, otherwise false
     */
    public static boolean isRooted() {
        return isRooted(false);
    }

    public static boolean isRooted(final boolean extendedChecks) {
        if (isRootedSigningKeys()) {
            return true;
        } else if (isRootedBinariesPresent()) {
            return true;
        }
        if (extendedChecks == true) {
            if (isRootedRunCommand()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to determine whether running on a rooted device by looking at the build tags,
     * and checking for 'test-keys', which marks a rom build with the test keys of aosp.
     * Most roms nowadays are signed with other private keys that may or may not still use this
     * label for the keys.
     *
     * @return true when the app seems to run on a rooted device, otherwise false
     */
    public static boolean isRootedSigningKeys() {
        final String buildTags = Build.TAGS;

        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        return false;
    }

    /**
     * Try to determine whether running on a rooted device by trying to call su and run
     * a command.
     *
     * @return true when the app seems to run on a rooted device, otherwise false
     */
    public static boolean isRootedRunCommand() {
        if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Try to determine whether running on a rooted device by checking for the existence for
     * binaries.
     * su, busybox
     *
     * @return true when the app seems to run on a rooted device, otherwise false
     */
    public static boolean isRootedBinariesPresent() {
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/",
                "/system/app/"};
        String[] binaries = {"Superuser", "su", "busybox"};
        for (String binary : binaries) {
            if (binaryExists(places, binary)) {
                return true;
            }
        }
        return false;
    }

    private static boolean binaryExists(final String[] paths, final String binary) {
        for (String path : paths) {
            if (fileExists(path + binary)) {
                return true;
            }
        }
        return false;
    }

    private static boolean fileExists(final String filePath) {
        try {
            final File file = new File(filePath);
            if (file.exists()) {
                return true;
            }
        } catch (NullPointerException npe) {
            // return false
        }
        return false;
    }
}
