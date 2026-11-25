package com.oplus.app;

import android.os.Bundle;

public class OplusAppExitInfo {

    public Bundle extension = new Bundle();
    public boolean hasResumingActivity;
    public boolean isResumingFirstStart;
    public boolean isResumingMultiApp;
    public String resumingActivityName;
    public String resumingPackageName;
    public int resumingWindowMode;
    public String targetName;

    public OplusAppExitInfo() {}
}
