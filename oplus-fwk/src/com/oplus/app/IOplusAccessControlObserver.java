package com.oplus.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IOplusAccessControlObserver extends IInterface {

    public static class Default implements IOplusAccessControlObserver {
        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOplusAccessControlObserver {
        public Stub() {
            this.attachInterface(this, "com.oplus.app.IOplusAccessControlObserver");
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
