package com.milesight.beaveriot.integrations.chirpstack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * ChirpStack uplink event (event=up) – JSON mapping.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UplinkEvent {

    @JsonProperty("deduplicationId")
    private String deduplicationId;

    @JsonProperty("time")
    private String time;

    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;

    @JsonProperty("devAddr")
    private String devAddr;

    @JsonProperty("dr")
    private Integer dr;

    @JsonProperty("fPort")
    private Integer fPort;

    @JsonProperty("data")
    private String data; // base64

    @JsonProperty("rxInfo")
    private List<UplinkRxInfo> rxInfo;

    @JsonProperty("txInfo")
    private Object txInfo; // optional, structure varies
}
