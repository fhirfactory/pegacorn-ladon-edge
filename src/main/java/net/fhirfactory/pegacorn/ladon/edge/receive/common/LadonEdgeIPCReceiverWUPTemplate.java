package net.fhirfactory.pegacorn.ladon.edge.receive.common;

import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverPacketDecoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverRegistrationBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverResponseEncoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverResponseGenerationBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverUoWExtractionBean;
import net.fhirfactory.pegacorn.petasos.ipc.codecs.IPCPacketDecoderInitializerFactory;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.EdgeIngresMessagingGatewayWUP;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.netty.ServerInitializerFactory;
import org.apache.camel.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public abstract class LadonEdgeIPCReceiverWUPTemplate extends EdgeIngresMessagingGatewayWUP {

    @Override
    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return ("tcp");
    }


    private String getWUPContinuityRoute() {
        return ("seda:" + this.getNameSet().getRouteCoreWUP() + ".InnerWUP.Continuity");
    }

    @Override
    public void configure() throws Exception {
        
        getLogger().debug("EdgeIPCReceiverWUPTemplate :: WUPIngresPoint/ingresFeed --> {}", this.ingresFeed());
        getLogger().debug("EdgeIPCReceiverWUPTemplate :: WUPEgressPoint/egressFeed --> {}", this.egressFeed());

        if (this.getIngresTopologyEndpointElement() == null) {
            getLogger().error("EdgeIPCReceiverWUPTemplate::configure(): Guru Software Meditation Error --> No Ingres Point Specified to consider!!!");
        }

        getLogger().debug("EdgeIPCReceiverWUPTemplate::configure(): http provider --> {}", this.specifyEndpointComponentDefinition());
        getLogger().debug("EdgeIPCReceiverWUPTemplate::configure(): hostname --> {}", this.getIngresTopologyEndpointElement().getHostname());
        getLogger().debug("EdgeIPCReceiverWUPTemplate::configure(): port --> {}", this.getIngresTopologyEndpointElement().getExposedPort());

        from(this.ingresFeed())
                .routeId(this.getNameSet().getRouteCoreWUP())
                .transform(simple("${bodyAs(String)}"))
                .log(LoggingLevel.INFO, "Incoming Message --> ${body}")
                .bean(InterProcessingPlantHandoverPacketDecoderBean.class, "handoverPacketDecode(*)")
                .bean(InterProcessingPlantHandoverRegistrationBean.class, "ipcReceiverActivityStart(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .to(ExchangePattern.InOnly, getWUPContinuityRoute())
                .bean(InterProcessingPlantHandoverResponseGenerationBean.class, "generateInterProcessingPlantHandoverResponse(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(InterProcessingPlantHandoverResponseEncoderBean.class, "responseEncoder(*)");

        from(getWUPContinuityRoute())
                .bean(InterProcessingPlantHandoverUoWExtractionBean.class, "extractUoW(*, Exchange)")
                .to(egressFeed());
    }

    @Override
    protected String specifyWUPWorkshop() {
        return ("Edge");
    }

}
