package com.stronans.sensors;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "Distance"
})
public class SensorMessage {

    @JsonProperty("Distance")
    private Distance distance;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public SensorMessage() {
    }

    /**
     * @param distance
     */
    @JsonCreator
    public SensorMessage(@JsonProperty("Distance")Distance distance) {
        this.distance = distance;
    }

    /**
     * @return The distance
     */
    @JsonProperty("Distance")
    public Distance getDistance() {
        return distance;
    }

    /**
     * @param distance The Distance
     */
    @JsonProperty("Distance")
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "SensorMessage{" +
                "distance=" + distance +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}