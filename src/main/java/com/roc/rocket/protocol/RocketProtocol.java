package com.roc.rocket.protocol;

import lombok.Data;

@Data
public class RocketProtocol extends ProtocolHeader {

    private String id;

    private RocketProtocolBody rocketProtocolBody;

    public RocketProtocol() {
    }

    public RocketProtocol(RocketProtocolBody rocketProtocolBody) {
        this.rocketProtocolBody = rocketProtocolBody;
    }

    public RocketProtocol(String id, RocketProtocolBody rocketProtocolBody) {
        this.id = id;
        this.rocketProtocolBody = rocketProtocolBody;
    }
}
