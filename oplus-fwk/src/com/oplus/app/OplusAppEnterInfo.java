package com.oplus.app;

import android.content.Intent;
import android.os.Bundle;

public class OplusAppEnterInfo {

    public Bundle extension = new Bundle();
    public boolean firstStart;
    public Intent intent;
    public String launchedFromPackage;
    public boolean multiApp;
    public String targetName;
    public int windowMode;

    public OplusAppEnterInfo() {}
}
