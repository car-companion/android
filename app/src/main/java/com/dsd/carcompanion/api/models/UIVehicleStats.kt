package com.dsd.carcompanion.api.models

class UIVehicleStats {

    var carVin: String = "";
    var carColor: String = "";

    var enabledRightDoor: Boolean = false;
    var isRightDoorOpen: Boolean = false;
    var rightDoorValue: Float = 0.0f

    var enabledLeftDoor: Boolean = false;
    var isLeftDoorOpen: Boolean = false;
    var leftDoorValue: Float = 0.0f

    var enabledRightWindow: Boolean = false;
    var isRightWindowUp: Boolean = true;
    var rightWindowValue: Float = 0.0f

    var enabledLeftWindow: Boolean = false;
    var isLeftWindowUp: Boolean = true;
    var leftWindowValue: Float = 0.0f

    var enabledLights: Boolean = false;
    var areLightsTurnedOff: Boolean = true;
    var lightsValue: Float = 0.0f

    var enabledTemperature: Boolean = false;
    var temperatureValue: Float = 0.24f

    var enabledLocks: Boolean = false;
    var locksValue: Float = 0.0f

    var chargingEnabled: Boolean = false;
    var batteryStatus: Float = 0.0f
    var isCarCharging: Boolean = false

    var isCarDriving: Boolean = false;

    var isItSnowing: Boolean = false;
}