package at.oenb.dltrouting.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Service {
    SDD_CORE("SDD Core"), SDD_B2B("SDD B2B"), SEPA_CT("SEPA CT"), SCT_INST("SCT Inst");

    private String serviceName;

    Service(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonValue
    public String getServiceName() {
        return serviceName;
    }
}