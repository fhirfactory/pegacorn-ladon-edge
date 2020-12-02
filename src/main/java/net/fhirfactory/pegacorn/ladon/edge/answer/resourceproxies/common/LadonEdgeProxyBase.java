package net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common;

import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import net.fhirfactory.pegacorn.ladon.processingplant.LadonProcessingPlant;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.common.AccessorBase;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class LadonEdgeProxyBase {
    boolean isInitialised;

    public LadonEdgeProxyBase(){
        isInitialised = false;
    }

    @Inject
    private LadonProcessingPlant ladonPlant;

    @PostConstruct
    private void initialisePatientProxy(){
        if(!this.isInitialised()) {
            getLogger().info("LadonEdgeProxyBase::initialiseProxy(): Entry, Initialising Services");
            getProcessingPlant().initialisePlant();
            getVirtualDBAccessor().initialiseServices();
            this.setInitialised(true);
            getLogger().debug("LadonEdgeProxyBase::initialiseProxy(): Exit");
        }
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    public void setInitialised(boolean initialised) {
        isInitialised = initialised;
    }

    public LadonProcessingPlant getProcessingPlant() {
        return ladonPlant;
    }

    abstract protected Logger getLogger();
    abstract protected AccessorBase specifyVirtualDBAccessor();
    abstract protected VirtualDBMethodOutcome getResource(IdType id);
    abstract protected VirtualDBMethodOutcome getResource(Identifier identifier);
    abstract protected VirtualDBMethodOutcome createResource(Resource resource);
    abstract protected VirtualDBMethodOutcome updateResource(Resource resource);
    abstract protected VirtualDBMethodOutcome deleteResource(IdType id);

    protected AccessorBase getVirtualDBAccessor(){
        return(specifyVirtualDBAccessor());
    }

}
