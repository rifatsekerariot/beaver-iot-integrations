package com.milesight.beaveriot.integrations.chirpstack.service;

import com.milesight.beaveriot.context.api.DeviceServiceProvider;
import com.milesight.beaveriot.integrations.chirpstack.constant.ChirpstackConstants;
import com.milesight.beaveriot.integrations.chirpstack.entity.ChirpstackIntegrationEntities;
import com.milesight.beaveriot.context.integration.model.Device;
import com.milesight.beaveriot.context.integration.model.DeviceBuilder;
import com.milesight.beaveriot.context.integration.model.event.ExchangeEvent;
import com.milesight.beaveriot.eventbus.annotations.EventSubscribe;
import com.milesight.beaveriot.eventbus.api.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Handles add/delete device from Beaver UI (Device → Add → ChirpStack HTTP).
 * Add: creates a device with identifier = devEui so webhook can find it via
 * findByIdentifier(devEui, INTEGRATION_ID).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChirpstackDeviceService {

    private final DeviceServiceProvider deviceServiceProvider;

    @EventSubscribe(payloadKeyExpression = ChirpstackConstants.INTEGRATION_ID + ".integration.add_device.*", eventType = ExchangeEvent.EventType.CALL_SERVICE)
    public void onAddDevice(Event<ChirpstackIntegrationEntities.AddDevice> event) {
        ChirpstackIntegrationEntities.AddDevice addDevice = event.getPayload();
        String deviceName = addDevice.getAddDeviceName();
        String devEui = addDevice.getDevEui();
        if (!StringUtils.hasText(devEui)) {
            log.warn("ChirpStack add_device: devEui is required");
            throw new IllegalArgumentException("External Device ID (DevEUI) is required");
        }
        devEui = devEui.trim();
        Device device = new DeviceBuilder(ChirpstackConstants.INTEGRATION_ID)
                .name(deviceName != null ? deviceName : devEui)
                .identifier(devEui)
                .build();
        deviceServiceProvider.save(device);
        log.info("ChirpStack add_device: created device name={} devEui={}", device.getName(), devEui);
    }

    @EventSubscribe(payloadKeyExpression = ChirpstackConstants.INTEGRATION_ID + ".integration.delete_device", eventType = ExchangeEvent.EventType.CALL_SERVICE)
    public void onDeleteDevice(Event<ChirpstackIntegrationEntities.DeleteDevice> event) {
        Device device = event.getPayload().getDeletedDevice();
        if (device != null) {
            deviceServiceProvider.deleteById(device.getId());
            log.info("ChirpStack delete_device: deleted device id={} identifier={}", device.getId(), device.getIdentifier());
        }
    }
}
