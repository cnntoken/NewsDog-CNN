package com.xiaomi.mipush.sdk;

import java.util.Collections;
import java.util.List;

/**
 * Created by newsdog on 21/2/17.
 */

public class MiPushCommandMessage {
    public String getCommand() {
        return "";
    }

    public List<String> getCommandArguments() {
        return Collections.EMPTY_LIST;
    }

    public int getResultCode() {
        return -1;
    }

    public String getReason() {
        return "reason";
    }
}
