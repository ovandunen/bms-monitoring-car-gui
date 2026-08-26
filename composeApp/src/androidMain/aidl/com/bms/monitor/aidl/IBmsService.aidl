package com.bms.monitor.aidl;

import com.bms.monitor.aidl.ChargingStationSnapshot;
import com.bms.monitor.aidl.IBmsCallback;
import com.bms.monitor.aidl.VehicleLocationSnapshot;

interface IBmsService {
    void registerCallback(IBmsCallback callback);
    void unregisterCallback(IBmsCallback callback);
    void setChargeLimit(float maxVoltage);
    void requestBalancing(boolean enabled);
    void refreshChargingStations(String vehicleId, double latitude, double longitude, double radiusMeters);
    ChargingStationSnapshot[] getCachedChargingStations();
    String getVehicleId();
    void publishSwapFeedback(String correlationId, String state, String stationId);

    // new method
    VehicleLocationSnapshot getCurrentLocation();
}
