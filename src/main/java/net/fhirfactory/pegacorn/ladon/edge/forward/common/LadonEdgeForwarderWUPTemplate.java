package net.fhirfactory.pegacorn.ladon.edge.forward.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverFinisherBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverPacketEncoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverPacketGenerationBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverPacketResponseDecoder;
import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;
import net.fhirfactory.pegacorn.petasos.model.processingplant.DefaultWorkshopSetEnum;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.EdgeEgressMessagingGatewayWUP;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.netty.codec.DelimiterBasedFrameDecoder;
import org.apache.camel.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class LadonEdgeForwarderWUPTemplate extends EdgeEgressMessagingGatewayWUP {

    @Override
    public void configure() throws Exception {
        getLogger().info("EdgeIPCForwarderWUPTemplate :: WUPIngresPoint/ingresFeed --> {}", this.ingresFeed());
        getLogger().info("EdgeIPCForwarderWUPTemplate :: WUPEgressPoint/egressFeed --> {}", this.egressFeed());

        from(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .log(LoggingLevel.INFO, "Incoming Raw Message --> ${body}")
                .bean(InterProcessingPlantHandoverPacketGenerationBean.class, "constructInterProcessingPlantHandoverPacket(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(InterProcessingPlantHandoverPacketEncoderBean.class, "handoverPacketEncode")
                .to(specifyEgressEndpoint())
                .transform(simple("${bodyAs(String)}"))
                .log(LoggingLevel.INFO, "Response Raw Message --> ${body}")
                .bean(InterProcessingPlantHandoverPacketResponseDecoder.class, "contextualiseInterProcessingPlantHandoverResponsePacket(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(InterProcessingPlantHandoverFinisherBean.class, "ipcSenderNotifyActivityFinished(*, Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")");
    }

    protected abstract String specifyTargetSubsystem();
    protected abstract String specifyTargetSubsystemVersion();
    protected abstract String specifyTargetEndpointName();
    protected abstract String specifyTargetEndpointVersion();
    protected abstract String specifyTargetService();
    protected abstract String specifyTargetProcessingPlant();

    @Override
    protected String deriveTargetEndpointDetails(){
        getLogger().debug(".deriveTargetEndpointDetails(): Entry");
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem Name --> {}, Target Subsystem Version --> {}", specifyTargetSubsystem(), specifyTargetSubsystemVersion());
        NodeElement targetSubsystem = getTopologyServer().getNode(specifyTargetSubsystem(), NodeElementTypeEnum.SUBSYSTEM, specifyTargetSubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem (NodeElement) --> {}", targetSubsystem);
        NodeElement targetNode;
        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_CLUSTERED:{
                targetNode = getTopologyServer().getNode(specifyTargetService(), NodeElementTypeEnum.SERVICE, specifyTargetSubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = getTopologyServer().getNode(specifyTargetProcessingPlant(), NodeElementTypeEnum.PROCESSING_PLANT, specifyTargetSubsystemVersion());
            }
        }
        getLogger().trace(".deriveTargetEndpointDetails(): targetNode --> {}", targetNode);
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpointName --> {}, targetEndpointVersion --> {}", specifyTargetEndpointName(), specifyTargetEndpointVersion());
        EndpointElement endpoint = getTopologyServer().getEndpoint(targetNode, specifyTargetEndpointName(), specifyTargetEndpointVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpoint (EndpointElement) --> {}", endpoint);
        String endpointDetails = endpoint.getHostname() + ":" + endpoint.getExposedPort();
        getLogger().debug(".deriveTargetEndpointDetails(): Exit, endpointDetails --> {}", endpointDetails);
        return(endpointDetails);
    }

    @Override
    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    @Override
    protected String specifyWUPWorkshop(){
        return("Edge");
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return ("tcp");
    }

}
