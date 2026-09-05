package com.bms.monitor.aidl;

import com.bms.monitor.aidl.BmsData;
import com.bms.monitor.aidl.ChargingStationSnapshot;
import com.bms.monitor.aidl.BmsVehicleLocation;

oneway interface IBmsCallback {
    void onDataUpdate(in BmsData data);
    void onAlert(int level, String message);
    void onChargingStationsUpdate(in ChargingStationSnapshot[] stations);
    void onLocationChanged(in BmsVehicleLocation location);
}
