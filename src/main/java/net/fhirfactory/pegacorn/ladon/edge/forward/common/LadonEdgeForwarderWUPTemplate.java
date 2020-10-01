package net.fhirfactory.pegacorn.ladon.edge.forward.common;

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.EdgeEgressMessagingGatewayWUP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public abstract class LadonEdgeForwarderWUPTemplate extends EdgeEgressMessagingGatewayWUP {
    private static final Logger LOG = LoggerFactory.getLogger(LadonEdgeForwarderWUPTemplate.class);

    @Override
    public void configure() throws Exception {

    }

    @Override
    protected String specifyEgressEndpointVersion() {
        return(specifyTargetEndpointVersion());
    }

    @Override
    protected String specifyEgressEndpoint(){
        LOG.debug(".specifyEgressEndpoint(): Entry");
        String egressEndPoint = null;
        LOG.debug(".specifyIngresEndpoint(): Exit, egressEndPoint --> {}", egressEndPoint);
        return(egressEndPoint);
    }

    @Override
    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return "tcp";
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return "//";
    }

    @Override
    protected String specifyEndpointProtocolLeadout() {
        return null;
    }

    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        HashSet<TopicToken> myTopicSet = new HashSet<TopicToken>();
        String[] entityNames = { "Bundle" };
        for(String entityName: entityNames){
            TopicToken topicId = getFHIRTopicIDBuilder().createTopicToken(entityName, "4.0.1");
            myTopicSet.add(topicId);
        }
        return(myTopicSet);
    }

    abstract protected String specifyTargetSubsystem();

    abstract protected String specifyTargetSubsystemVersion();

    abstract protected String specifyTargetEndpointName();

    abstract protected String specifyTargetEndpointVersion();

    private String deriveTargetEndpointDetails(){
        LOG.debug(".deriveTargetEndpointDetails(): Entry");
        NodeElement targetSubsystem = getTopologyServer().getNode(specifyTargetEndpointName(), NodeElementTypeEnum.SUBSYSTEM, specifyTargetSubsystemVersion());
        NodeElement targetNode;
        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_CLUSTERED:{
                targetNode = getTopologyServer().getNode(specifyTargetEndpointName(), NodeElementTypeEnum.SERVICE, specifyTargetSubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = getTopologyServer().getNode(specifyTargetEndpointName(), NodeElementTypeEnum.PROCESSING_PLANT, specifyTargetSubsystemVersion());
            }
        }
        EndpointElement endpoint = getTopologyServer().getEndpoint(targetNode, specifyTargetEndpointName(), specifyTargetEndpointVersion());
        String endpointDetails = endpoint.getHostname() + ":" + endpoint.getExposedPort();
        LOG.debug(".deriveTargetEndpointDetails(): Exit");
        return(endpointDetails);
    }

    @Override
    protected String specifyWUPWorkshop(){
        return("Edge");
    }
}
