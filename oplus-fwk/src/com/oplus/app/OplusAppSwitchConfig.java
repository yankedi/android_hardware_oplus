package com.oplus.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class OplusAppSwitchConfig {

    public HashSet<String> mActivitySet = new HashSet<>();
    public int observerFingerPrint;

    public void addAppConfig(int type, List<String> list) {}

    public List<String> getConfigs(int type) {
        return new ArrayList<>();
    }

    public void removeAppConfig(int type) {}
}
