// notificationAidlInterface.aidl
package net.donkeyandperi.zotplanner;

// Declare any non-default types here with import statements

interface INotificationService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    String getMessage();
    void setCheckingInterval(int ci);
}
