package net.fhirfactory.pegacorn.ladon.edge.forward;

import net.fhirfactory.pegacorn.ladon.edge.forward.common.LadonEdgeForwarderWUPTemplate;

public class IrisForwarderFHIRAllWUP extends LadonEdgeForwarderWUPTemplate {
    @Override
    protected String specifyTargetSubsystem() {
        return (getSubsystemComponentNamesService().getCommunicateIrisDefault());
    }

    @Override
    protected String specifyTargetSubsystemVersion() {
        return ("1.0.0"); // TODO put this in the Constants
    }

    @Override
    protected String specifyTargetEndpointName() {
        return (getSubsystemComponentNamesService().getCommunicateIrisEdgeReceiveFhirAllFromLadon());
    }

    @Override
    protected String specifyTargetEndpointVersion() {
        return ("1.0.0"); // TODO put this in the Constants
    }

    @Override
    protected String specifyEgressTopologyEndpointName() {
        return (getSubsystemComponentNamesService().getLadonEdgeForwardFhirAllToIris());
    }

    @Override
    protected String specifyWUPInstanceName() {
        return ("Ladon-Edge-Forward-ToIris-FHIR-All");
    }

    @Override
    protected String specifyWUPVersion() {
        return ("1.0.0");
    }
}
