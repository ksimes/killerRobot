package com.stronans.sensors;

import com.fasterxml.jackson.annotation.*;
import org.apache.log4j.Logger;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;


/**
 * Type of JSON messages sent to the Robot from sensors
 * <p>
 * Created by S.King on 22/10/2016.
 * <p>
 * Send the distances to the computer using Serial protocol in JSON format
 * {
 *      "Distance": {
 *      "left": 22,
 *      "centre": 15,
 *      "right": 60
 *      }
 * }
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "left",
        "centre",
        "right"
})
public final class Distance {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(Distance.class);

    @JsonProperty("left")
    private Long left;
    @JsonProperty("centre")
    private Long centre;
    @JsonProperty("right")
    private Long right;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public Distance() {
    }

    /**
     * @param centre
     * @param left
     * @param right
     */
    @JsonCreator
    public Distance(@JsonProperty("left")Long left, @JsonProperty("centre")Long centre, @JsonProperty("right")Long right) {
        this.left = left;
        this.centre = centre;
        this.right = right;
    }

    /**
     * @return The left
     */
    @JsonProperty("left")
    public Long getLeft() {
        return left;
    }

    /**
     * @return The centre
     */
    @JsonProperty("centre")
    public Long getCentre() {
        return centre;
    }

    /**
     * @return The right
     */
    @JsonProperty("right")
    public Long getRight() {
        return right;
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
        return "Distance{" +
                "left=" + left +
                ", centre=" + centre +
                ", right=" + right +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
