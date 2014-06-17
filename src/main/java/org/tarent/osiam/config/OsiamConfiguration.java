package org.tarent.osiam.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by mley on 24.03.14.
 */
@Data
public class OsiamConfiguration {

    @JsonProperty
    private String endpoint;

    @JsonProperty
    private String clientId;

    @JsonProperty
    private String clientSecret;

    @JsonProperty
    private String clientRedirectUri;

}
