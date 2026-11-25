package com.oplus.screenshot;

public class OplusLongshotViewInfo {

    private boolean mIsUnsupported;

    public OplusLongshotViewInfo() {}

    public boolean isUnsupported() {
        return mIsUnsupported;
    }

    public void setUnsupported() {
        mIsUnsupported = true;
    }

    public void reset() {
        mIsUnsupported = false;
    }
}
