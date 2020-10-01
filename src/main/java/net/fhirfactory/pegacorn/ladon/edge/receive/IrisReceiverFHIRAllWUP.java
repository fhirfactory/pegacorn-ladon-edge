package net.fhirfactory.pegacorn.ladon.edge.receive;

import net.fhirfactory.pegacorn.ladon.edge.receive.common.LadonEdgeIPCReceiverWUPTemplate;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IrisReceiverFHIRAllWUP extends LadonEdgeIPCReceiverWUPTemplate {

    private static final String WUP_NAME = "FHIR-Base-Entities-Receiver";
    private static final String WUP_VERSION = "1.0.0";

    @Override
    public String specifyWUPInstanceName() {
        return (WUP_NAME);
    }

    @Override
    public String specifyWUPVersion() {
        return (WUP_VERSION);
    }

    @Override
    protected String specifyIngresTopologyEndpointName() {
        return ("endpoint no no");
    }

    @Override
    protected String specifyIngresEndpointVersion() {
        return ("1.0.0");
    }
}
