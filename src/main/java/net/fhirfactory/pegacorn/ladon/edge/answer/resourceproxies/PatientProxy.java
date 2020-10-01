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
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.PatientAccessor;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common.LadonResourceProxy;
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.common.OperationOutcomeGenerator;
import net.fhirfactory.pegacorn.ladon.dtcache.accessors.common.OperationOutcomeSeverityEnum;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PatientProxy extends LadonResourceProxy implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PatientProxy.class);

    public PatientProxy(){
        super();
        this.setInitialised(false);
    }

    @Inject
    private PatientAccessor patientAccessor;

    @Inject
    private OperationOutcomeGenerator outcomeGenerator;

    @PostConstruct
    private void initialisePatientProxy(){
        if(!this.isInitialised()) {
            LOG.debug("PatientProxy::initialisePatientProxy(): Entry, Initialising Services");
            getLadonPlant().initialisePlant();
            getPatientAccessor().initialiseServices();
            this.setInitialised(true);
            LOG.debug("PatientProxy::initialisePatientProxy(): Exit");
        }
    }

    public void initialiseServices(){
        initialisePatientProxy();
    }

    public PatientAccessor getPatientAccessor() {
        return patientAccessor;
    }

    @Override
    public Class<Patient> getResourceType() {
        return (Patient.class);
    }

    /**
     * The "@Create" annotation indicates that this method implements "create=type", which adds a
     * new instance of a resource to the server.
     */
    @Create()
    public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
        LOG.debug(".createPatient(): Entry, thePatient (Patient) --> {}", thePatient);
        //validateResource(thePatient);
        IdType resourceId = patientAccessor.addResource(thePatient);
        MethodOutcome outcome = new MethodOutcome();
        if(resourceId != null){
            outcome.setId(resourceId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceNotFoundOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_ERROR));
            LOG.debug(".deletePatient(): Exit, Operation failed, resource did not exist. Outcome (MethodOutcome) -->", outcome);
        } else {
            IdType deletedPatientId = patientAccessor.removeResource(thePatient);
            outcome.setId(deletedPatientId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceDeletedOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_INFORMATION));
            LOG.debug(".deletePatient(): Exit, Operation succeeded, resource did not exist. Outcome (MethodOutcome) -->", outcome);
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
     * @param patientID The read operation takes one parameter, which must be of type IdDt and must be annotated
     *                  with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public Patient readPatient(@IdParam IdType patientID){
        LOG.debug(".readPatient(): Entry, patientID (IdType) --> {}", patientID);
        Patient retrievedPatient = patientAccessor.getResourceById(patientID);
        LOG.debug(".readPatient(): Exit, retrieved Patient (Patient) --> {}", retrievedPatient);
        return(retrievedPatient);
    }


    @Delete()
    public MethodOutcome deletePatient(@IdParam IdType resourceId){
        LOG.debug(".deletePatient(): Entry, resourceId (IdType) --> {}", resourceId);
        Patient retrievedPatient = patientAccessor.getResourceById(resourceId);
        MethodOutcome outcome = new MethodOutcome();
        if(retrievedPatient == null){
            outcome.setId(resourceId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceNotFoundOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_ERROR));
            LOG.debug(".deletePatient(): Exit, Operation failed, resource did not exist. Outcome (MethodOutcome) -->", outcome);
        } else {
            IdType deletedPatientId = patientAccessor.removeResource(retrievedPatient);
            outcome.setId(deletedPatientId);
            outcome.setOperationOutcome(outcomeGenerator.generateResourceDeletedOutcome(resourceId, OperationOutcomeSeverityEnum.SEVERITY_INFORMATION));
            LOG.debug(".deletePatient(): Exit, Operation succeeded, resource did not exist. Outcome (MethodOutcome) -->", outcome);
        }
        return(outcome);
    }
}
