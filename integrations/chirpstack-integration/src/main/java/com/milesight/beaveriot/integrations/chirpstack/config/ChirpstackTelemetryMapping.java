package com.milesight.beaveriot.integrations.chirpstack.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Defines supported telemetry types and their payload key aliases.
 * Add new entries here to support more sensor types; device entities and
 * uplink mapping are derived from this list.
 */
public final class ChirpstackTelemetryMapping {

    private ChirpstackTelemetryMapping() {
    }

    @Getter
    @AllArgsConstructor
    public static class Spec {
        private final String entityId;
        private final String displayName;
        private final String unit;
        private final List<String> payloadKeyAliases;

        public static Spec of(String entityId, String displayName, String unit, String... aliases) {
            return new Spec(entityId, displayName, unit, List.of(aliases));
        }
    }

    /** All supported telemetry specs. Order defines entity creation order. */
    public static final List<Spec> ALL = List.of(
            Spec.of("temperature", "Temperature", "\u00b0C", "temperature", "temp", "tmp"),
            Spec.of("humidity", "Humidity", "%", "humidity", "hum", "rh"),
            Spec.of("co2", "CO2", "ppm", "co2", "carbonDioxide", "carbon_dioxide"),
            Spec.of("pressure", "Pressure", "hPa", "pressure", "barometricPressure", "barometric_pressure", "press"),
            Spec.of("battery", "Battery", "%", "battery", "batteryLevel", "battery_level", "bat", "batt"),
            Spec.of("pm25", "PM2.5", "\u00b5g/m\u00b3", "pm25", "pm2_5", "pm2.5"),
            Spec.of("pm10", "PM10", "\u00b5g/m\u00b3", "pm10"),
            Spec.of("luminosity", "Luminosity", "lux", "luminosity", "light", "lux", "illuminance"),
            Spec.of("voltage", "Voltage", "V", "voltage", "volt", "v"),
            Spec.of("rssi", "RSSI", "dBm", "rssi"),
            Spec.of("snr", "SNR", "dB", "snr"),
            Spec.of("margin", "Margin", "dB", "margin")
    );
}
