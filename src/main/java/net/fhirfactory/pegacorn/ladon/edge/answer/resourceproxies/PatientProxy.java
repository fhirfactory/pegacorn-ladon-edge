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

import java.time.Instant;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import net.fhirfactory.pegacorn.datasets.fhir.r4.operationaloutcome.OperationOutcomeGenerator;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common.LadonEdgeSynchronousCRUDResourceBase;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.PatientAccessor;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.common.AccessorBase;

@ApplicationScoped
public class PatientProxy extends LadonEdgeSynchronousCRUDResourceBase implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PatientProxy.class);

    public PatientProxy() {
        super();
        this.setInitialised(false);
    }

    @Inject
    private PatientAccessor patientAccessor;

    @Inject
    private OperationOutcomeGenerator outcomeGenerator;

    @Override
    public Class<Patient> getResourceType() {
        return (Patient.class);
    }

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    @Override
    protected AccessorBase specifyVirtualDBAccessor() {
        return (patientAccessor);
    }

    /**
     * The "@Create" annotation indicates that this method implements "create=type",
     * which adds a new instance of a resource to the server.
     */
    @Create()
    public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
        LOG.debug(".createPatient(): Entry, thePatient (Patient) --> {}", thePatient);
        // validateResource(thePatient);
        VirtualDBMethodOutcome resourceActionOutcome = getVirtualDBAccessor().createResource(thePatient);
        return (resourceActionOutcome);
    }

    /**
     * This is the "read" operation. The "@Read" annotation indicates that this
     * method supports the read and/or get operation.
     * <p>
     * Read operations take a single parameter annotated with the {@link IdParam}
     * paramater, and should return a single resource instance.
     * </p>
     *
     * @param patientID The read operation takes one parameter, which must be of
     *                  type IdDt and must be annotated with the "@Read.IdParam"
     *                  annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public Patient readPatient(@IdParam IdType patientID) {
        LOG.debug(".readPatient(): Entry, patientID (IdType) --> {}", patientID);
        VirtualDBMethodOutcome outcome = getVirtualDBAccessor().getResource(patientID);
        Patient retrievedPatient = (Patient) outcome.getResource();
        LOG.debug(".readPatient(): Exit, retrieved Patient (Patient) --> {}", retrievedPatient);
        return (retrievedPatient);
    }

    /**
     * The "@Update" annotation indicates that this method implements "update=type",
     * which adds a new instance of a resource to the server.
     */
    @Update()
    public MethodOutcome updatePatient(@ResourceParam Patient thePatient) {
        LOG.debug(".createPatient(): Entry, thePatient (Patient) --> {}", thePatient);
        VirtualDBMethodOutcome resourceActionOutcome = getVirtualDBAccessor().updateResource(thePatient);
        return (resourceActionOutcome);
    }

    @Delete()
    public MethodOutcome deletePatient(@IdParam IdType resourceId) {
        LOG.debug(".deletePatient(): Entry, resourceId (IdType) --> {}", resourceId);
        throw (new UnsupportedOperationException("deletePatient() is not supported"));
    }

    @Search()
    public Bundle findByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) TokenParam identifierParam) {
        LOG.debug(".searchByIdentifier(): Entry, identifierParam (TokenParam) --> {}", identifierParam);

        Identifier identifierToSearchFor = new Identifier();
        identifierToSearchFor.setSystem(identifierParam.getSystem());
        identifierToSearchFor.setValue(identifierParam.getValue());

        VirtualDBMethodOutcome outcome = getVirtualDBAccessor().getResource(identifierToSearchFor);

        if (outcome.getStatusEnum().equals(VirtualDBActionStatusEnum.REVIEW_FINISH)
                || outcome.getStatusEnum().equals(VirtualDBActionStatusEnum.SEARCH_FINISHED)) {
            getLogger().info("findByIdentifier(): search is finished --> all good");
            Bundle searchOutcome = (Bundle) outcome.getResource();
            return (searchOutcome);
        } else {
            getLogger().info("findByIdentifier(): search is finished --> nothing to see here!");
            Bundle outputBundle = new Bundle();
            outputBundle.setType(Bundle.BundleType.SEARCHSET);
            outputBundle.setTimestamp(Date.from(Instant.now()));
            outputBundle.setTotal(0);
            return (outputBundle);
        }
    }
}
