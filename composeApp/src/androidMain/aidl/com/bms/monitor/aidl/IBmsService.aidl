package com.bms.monitor.aidl;

import com.bms.monitor.aidl.ChargingStationSnapshot;
import com.bms.monitor.aidl.IBmsCallback;

interface IBmsService {
    void registerCallback(IBmsCallback callback);
    void unregisterCallback(IBmsCallback callback);
    void setChargeLimit(float maxVoltage);
    void requestBalancing(boolean enabled);
    void refreshChargingStations(String driverId, double latitude, double longitude, double radiusMeters);
    ChargingStationSnapshot[] getCachedChargingStations();
    String getCsmsDriverId();
}
