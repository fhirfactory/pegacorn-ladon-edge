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
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common.LadonResourceProxy;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class DocumentReferenceProxy extends LadonResourceProxy implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentReferenceProxy.class);

    @Override
    public Class<DocumentReference> getResourceType() {
        return (DocumentReference.class);
    }

    @Inject
    private DocumentReferenceAccessor docRefAccessor;

    @PostConstruct
    private void initialiseDocumentReferenceProxy(){
        if(!this.isInitialised()) {
            LOG.debug("PatientProxy::initialisePatientProxy(): Entry, Initialising Services");
            getLadonPlant().initialisePlant();
            getDocRefAccessor().initialiseServices();
            this.setInitialised(true);
            LOG.debug("PatientProxy::initialisePatientProxy(): Exit");
        }
    }

    public void initialiseServices(){
        initialiseDocumentReferenceProxy();
    }

    public DocumentReferenceAccessor getDocRefAccessor() {
        return docRefAccessor;
    }

    /**
     * The "@Create" annotation indicates that this method implements "create=type", which adds a
     * new instance of a resource to the server.
     */
    @Create()
    public MethodOutcome createDocumentReference(@ResourceParam DocumentReference theDocRef) {

        LOG.debug(".createDocumentReference(): Entry, theDocRef (DocumentReference) --> {}", theDocRef);
        //validateResource(thePatient);

        IdType docrefId = getDocRefAccessor().addResource(theDocRef);

        return new MethodOutcome(docrefId);
    }

    /**
     * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or
     * read operation.
     * <p>
     * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a
     * single resource instance.
     * </p>
     *
     * @param docrefID The read operation takes one parameter, which must be of type IdDt and must be annotated
     *                  with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public DocumentReference readDocumentReference(@IdParam IdType docrefID){
        LOG.debug(".readDocumentReference(): Entry, docrefID (IdType) --> {}",docrefID );
        DocumentReference retrievedDocRef = docRefAccessor.getResourceById(docrefID);
        if( retrievedDocRef == null){
            return(null);
        }
        LOG.debug(".readDocumentReference(): Exit, retrieved DocumentReference --> {}", retrievedDocRef);
        return(retrievedDocRef);
    }

    @Delete()
    public MethodOutcome deletePatient(@IdParam IdType thePatientId){

        MethodOutcome outcome = new MethodOutcome();
        return(outcome);
    }
}
