package com.milesight.beaveriot.integrations.chirpstack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milesight.beaveriot.context.api.DeviceServiceProvider;
import com.milesight.beaveriot.context.api.DeviceStatusServiceProvider;
import com.milesight.beaveriot.integrations.chirpstack.constant.ChirpstackConstants;
import com.milesight.beaveriot.integrations.chirpstack.model.JoinEvent;
import com.milesight.beaveriot.integrations.chirpstack.model.StatusEvent;
import com.milesight.beaveriot.integrations.chirpstack.model.UplinkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles ChirpStack HTTP integration events. No token or password validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChirpstackWebhookService {

    private final ObjectMapper objectMapper;
    private final DeviceServiceProvider deviceServiceProvider;
    private final DeviceStatusServiceProvider deviceStatusServiceProvider;

    public void handle(String event, JsonNode body) {
        if (event == null || event.isBlank()) {
            log.warn("ChirpStack webhook: missing event");
            return;
        }
        switch (event.toLowerCase()) {
            case "up" -> handleUplink(body);
            case "join" -> handleJoin(body);
            case "status" -> handleStatus(body);
            case "ack", "txack", "log", "location", "integration" -> log.debug("ChirpStack webhook: event={} (logged only)", event);
            default -> log.debug("ChirpStack webhook: unknown event={}", event);
        }
    }

    private void handleUplink(JsonNode body) {
        UplinkEvent evt;
        try {
            evt = objectMapper.treeToValue(body, UplinkEvent.class);
        } catch (Exception e) {
            log.error("ChirpStack webhook: failed to parse uplink event", e);
            return;
        }
        if (evt == null || evt.getDeviceInfo() == null) {
            log.warn("ChirpStack webhook: uplink missing deviceInfo");
            return;
        }
        String devEui = evt.getDeviceInfo().getDevEui();
        if (devEui == null || devEui.isBlank()) {
            log.warn("ChirpStack webhook: uplink missing devEui");
            return;
        }
        var device = deviceServiceProvider.findByIdentifier(devEui, ChirpstackConstants.INTEGRATION_ID);
        if (device == null) {
            log.debug("ChirpStack webhook: device not found for devEui={}, skip", devEui);
            return;
        }
        deviceStatusServiceProvider.online(device);
        int fPort = evt.getFPort() != null ? evt.getFPort() : 0;
        Integer rssi = null;
        Double snr = null;
        if (evt.getRxInfo() != null && !evt.getRxInfo().isEmpty()) {
            var rx = evt.getRxInfo().get(0);
            rssi = rx.getRssi();
            snr = rx.getSnr();
        }
        log.debug("ChirpStack uplink: devEui={}, fPort={}, rssi={}, snr={}", devEui, fPort, rssi, snr);
    }

    private void handleJoin(JsonNode body) {
        JoinEvent evt;
        try {
            evt = objectMapper.treeToValue(body, JoinEvent.class);
        } catch (Exception e) {
            log.error("ChirpStack webhook: failed to parse join event", e);
            return;
        }
        if (evt != null && evt.getDeviceInfo() != null) {
            log.info("ChirpStack join: devEui={}, devAddr={}", evt.getDeviceInfo().getDevEui(), evt.getDevAddr());
        }
    }

    private void handleStatus(JsonNode body) {
        StatusEvent evt;
        try {
            evt = objectMapper.treeToValue(body, StatusEvent.class);
        } catch (Exception e) {
            log.error("ChirpStack webhook: failed to parse status event", e);
            return;
        }
        if (evt != null && evt.getDeviceInfo() != null) {
            log.debug("ChirpStack status: devEui={}, margin={}, batteryLevel={}",
                    evt.getDeviceInfo().getDevEui(), evt.getMargin(), evt.getBatteryLevel());
        }
    }
}
