package be.idamf.android.tamperdetection.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to check if su can be called.
 * See http://www.simonroses.com/2013/06/appsec-build-rooted-detection-in-your-app/
 * and
 * http://stackoverflow.com/questions/1101380/determine-if-running-on-a-rooted-device
 */
public class ExecShell {

    protected static enum SHELL_CMD {
        check_su_binary(new String[] { "/system/xbin/which","su" });

        String[] commands;

        SHELL_CMD(final String[] commands) {
            this.commands = commands;
        }
    }

    public List<String> executeCommand(SHELL_CMD shellCmd) {
        String line = null;
        List<String> fullResponse = new ArrayList<String>();
        Process process = null;

        try {
            process = Runtime.getRuntime().exec(shellCmd.commands);
        } catch (IOException e) {
            return null;
        }

        BufferedReader processComm = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            while ((line = processComm.readLine()) != null) {
                fullResponse.add(line);
            }
        } catch (IOException e) {
            fullResponse = null;
        } finally {
            if (processComm != null) {
                try {
                    processComm.close();
                } catch (IOException e) {
                    // failed to close inputStream
                }
            }
        }
        process.destroy();

        return fullResponse;
    }
}
