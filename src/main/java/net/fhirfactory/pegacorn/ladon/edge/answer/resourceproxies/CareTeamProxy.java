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
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import net.fhirfactory.pegacorn.datasets.fhir.r4.operationaloutcome.OperationOutcomeGenerator;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common.LadonEdgeSynchronousCRUDResourceBase;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.CareTeamAccessor;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.common.AccessorBase;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.OperationNotSupportedException;

@ApplicationScoped
public class CareTeamProxy extends LadonEdgeSynchronousCRUDResourceBase implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CareTeamProxy.class);

    public CareTeamProxy() {
        super();
        this.setInitialised(false);
    }

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    @Inject
    private CareTeamAccessor virtualDBAccessor;

    @Inject
    private OperationOutcomeGenerator outcomeGenerator;

    @Override
    protected AccessorBase specifyVirtualDBAccessor() {
        return (virtualDBAccessor);
    }

    @Override
    public Class<CareTeam> getResourceType() {
        return (CareTeam.class);
    }

    /**
     * The "@Create" annotation indicates that this method implements "create=type", which adds a
     * new instance of a resource to the server.
     */
    @Create()
    public MethodOutcome createCareTeam(@ResourceParam CareTeam theResource) {
        LOG.debug(".createPatient(): Entry, thePatient (Patient) --> {}", theResource);
        VirtualDBMethodOutcome outcome = getVirtualDBAccessor().createResource(theResource);
        return (outcome);
    }

    /**
     * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or
     * get operation.
     * <p>
     * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a
     * single resource instance.
     * </p>
     *
     * @param resourceId The read operation takes one parameter, which must be of type IdDt and must be annotated
     *                   with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public CareTeam reviewCareTeam(@IdParam IdType resourceId) {
        LOG.debug(".reviewCareTeam(): Entry, resourceId (IdType) --> {}", resourceId);
        VirtualDBMethodOutcome outcome = getResource(resourceId);
        CareTeam retrievedDocRef = (CareTeam) outcome.getResource();
        LOG.debug(".reviewCareTeam(): Exit, retrieved Document Reference (CareTeam) --> {}", retrievedDocRef);
        return (retrievedDocRef);
    }

    /**
     * This is the "update" operation. The "@Update" annotation indicates that this method supports the update operation.
     * <p>
     * Update operations take a single parameter annotated with the {@ResourceParam} paramater, and should return a
     * MethodOutcome as to the sucess (or otherwise) of the operation.
     * </p>
     *
     * @param resourceToUpdate The update operation takes one parameter, which must be the CareTeam resource to be updated.
     * @return Returns an outcome (MethodOutcome) detailing the success (or otherwise) of the action.
     */
    @Update()
    public MethodOutcome updateCareTeam(@ResourceParam CareTeam resourceToUpdate) {
        LOG.debug(".readCareTeam(): Entry, docRefToUpdate (CareTeam) --> {}", resourceToUpdate);
        VirtualDBMethodOutcome outcome = updateResource(resourceToUpdate);
        LOG.debug(".readCareTeam(): Exit, outcome (VirtualDBMethodOutcome) --> {}", outcome);
        return (outcome);
    }

    @Delete()
    public MethodOutcome deleteCareTeam(@IdParam IdType resourceId) throws OperationNotSupportedException {
        LOG.debug(".deleteCareTeam(): Entry, resourceId (IdType) --> {}", resourceId);
        throw (new OperationNotSupportedException("deletion of a CareTeam is not supported"));
    }

    //
    //
    // Support Searches
    //
    //

    @Search()
    public Bundle findByIdentifier(@RequiredParam(name = CareTeam.SP_IDENTIFIER) TokenParam identifierParam) {
        getLogger().debug(".findByIdentifier(): Entry, identifierParam --> {}", identifierParam);
        Identifier identifierToSearchFor = tokenParam2Identifier(identifierParam);
        Bundle outcome = findResourceViaIdentifier(identifierToSearchFor);
        return(outcome);
    }
}
