/*
 * Copyright (c) 2020 Mark A. Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.DocumentReferenceAccessor;
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.PatientAccessor;
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.common.OperationOutcomeGenerator;
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.common.OperationOutcomeSeverityEnum;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common.LadonResourceProxy;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DocumentReferenceProxy extends LadonResourceProxy implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentReferenceProxy.class);

    public DocumentReferenceProxy(){
        super();
        this.setInitialised(false);
    }

    @Inject
    private DocumentReferenceAccessor docRefAccessor;

    @Inject
    private OperationOutcomeGenerator outcomeGenerator;

    @PostConstruct
    private void initialisePatientProxy(){
        if(!this.isInitialised()) {
            LOG.debug("DocumentReferenceProxy::initialisePatientProxy(): Entry, Initialising Services");
            getLadonPlant().initialisePlant();
            getDocumentReferenceAccessor().initialiseServices();
            this.setInitialised(true);
            LOG.debug("DocumentReferenceProxy::initialisePatientProxy(): Exit");
        }
    }

    public void initialiseServices(){
        initialisePatientProxy();
    }

    public DocumentReferenceAccessor getDocumentReferenceAccessor() {
        return docRefAccessor;
    }

    @Override
    public Class<DocumentReference> getResourceType() {
        return (DocumentReference.class);
    }

    /**
     * The "@Create" annotation indicates that this method implements "create=type", which adds a
     * new instance of a resource to the server.
     */
    @Create()
    public MethodOutcome createDocumentReference(@ResourceParam DocumentReference theResource) {
        LOG.debug(".createPatient(): Entry, thePatient (Patient) --> {}", theResource);
        //validateResource(thePatient);
        IdType resourceId = docRefAccessor.addResource(theResource);
        MethodOutcome outcome = new MethodOutcome();
        if(resourceId != null){
            outcome.setId(resourceId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceAddedOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_INFORMATION));
            LOG.debug(".createDocumentReference(): Exit, all good. Outcome (MethodOutcome) -->", outcome);
        } else {
            IdType deletedDocRefId = docRefAccessor.removeResource(theResource);
            outcome.setId(deletedDocRefId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceDeletedOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_ERROR));
            LOG.debug(".createDocumentReference(): Exit, There was a problem adding the resource. Outcome (MethodOutcome) -->", outcome);
        }



        return new MethodOutcome(resourceId);
    }

    /**
     * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or
     * read operation.
     * <p>
     * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a
     * single resource instance.
     * </p>
     *
     * @param resourceId The read operation takes one parameter, which must be of type IdDt and must be annotated
     *                  with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public DocumentReference readDocumentReference(@IdParam IdType resourceId){
        LOG.debug(".readDocumentReference(): Entry, resourceId (IdType) --> {}", resourceId);
        DocumentReference retrievedDocRef = docRefAccessor.getResourceById(resourceId);
        LOG.debug(".readDocumentReference(): Exit, retrieved Document Reference (DocumentReference) --> {}", retrievedDocRef);
        return(retrievedDocRef);
    }


    @Delete()
    public MethodOutcome deleteDocumentReference(@IdParam IdType resourceId){
        LOG.debug(".deleteDocumentReference(): Entry, resourceId (IdType) --> {}", resourceId);
        DocumentReference retrievedDocRef = docRefAccessor.getResourceById(resourceId);
        MethodOutcome outcome = new MethodOutcome();
        if(retrievedDocRef == null){
            outcome.setId(resourceId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceNotFoundOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_ERROR));
            LOG.debug(".deleteDocumentReference(): Exit, Operation failed, resource did not exist. Outcome (MethodOutcome) -->", outcome);
        } else {
            IdType deletedPatientId = docRefAccessor.removeResource(retrievedDocRef);
            outcome.setId(deletedPatientId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceDeletedOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_INFORMATION));
            LOG.debug(".deleteDocumentReference(): Exit, Operation succeeded, resource did not exist. Outcome (MethodOutcome) -->", outcome);
        }
        return(outcome);
    }
}
