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
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import net.fhirfactory.pegacorn.fhir.operations.OperationOutcomeGenerator;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common.LadonEdgeSynchronousCRUDResourceBase;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.DocumentReferenceAccessor;
import net.fhirfactory.pegacorn.ladon.virtualdb.accessors.common.AccessorBase;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class DocumentReferenceProxy extends LadonEdgeSynchronousCRUDResourceBase implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentReferenceProxy.class);

    public DocumentReferenceProxy() {
        super();
        this.setInitialised(false);
    }

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    @Inject
    private DocumentReferenceAccessor virtualDBAccessor;

    @Inject
    private OperationOutcomeGenerator outcomeGenerator;

    @Override
    protected AccessorBase specifyVirtualDBAccessor() {
        return (virtualDBAccessor);
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
        VirtualDBMethodOutcome outcome = getVirtualDBAccessor().createResource(theResource);
        return (outcome);
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
     *                   with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public DocumentReference readDocumentReference(@IdParam IdType resourceId) {
        LOG.debug(".readDocumentReference(): Entry, resourceId (IdType) --> {}", resourceId);
        VirtualDBMethodOutcome outcome = getResource(resourceId);
        DocumentReference retrievedDocRef = (DocumentReference) outcome.getResource();
        LOG.debug(".readDocumentReference(): Exit, retrieved Document Reference (DocumentReference) --> {}", retrievedDocRef);
        return (retrievedDocRef);
    }


    @Delete()
    public MethodOutcome deleteDocumentReference(@IdParam IdType resourceId) throws OperationNotSupportedException {
        LOG.debug(".deleteDocumentReference(): Entry, resourceId (IdType) --> {}", resourceId);
        throw (new OperationNotSupportedException("deletion of a DocumentReference is not supported"));
    }

    @Search()
    public Bundle searchByDateAndType(@RequiredParam(name = DocumentReference.SP_DATE) DateRangeParam theRange, @RequiredParam(name = DocumentReference.SP_TYPE) TokenParam docRefType) {
        LOG.debug(".searchByDateAndType(): Entry, DateTimeRange --> {}, Type --> {}", theRange, docRefType);

        HashMap<Property, Element> argumentList = new HashMap<>();

        // First Parameter, the DocumentReference.type
        CodeableConcept docType = new CodeableConcept();
        Coding docCoding = new Coding();
        docCoding.setCode(docRefType.getValue());
        docCoding.setSystem((docRefType.getSystem()));
        docType.addCoding(docCoding);
        docType.setText(docRefType.getValue());
        Property docRefTypeProperty = new Property(
                "type",
                "CodeableConcept",
                "Specifies the particular kind of document referenced (e.g. History and Physical, Discharge Summary, Progress Note). This usually equates to the purpose of making the document referenced.",
                0,
                1,
                (List<? extends Base>) null);
        argumentList.put(docRefTypeProperty, docType);
        // Second Parameter, the DocumentReference.date (expressed as a Period, where the date is to be in-between)
        Period searchPeriod = new Period();
        searchPeriod.setStart(theRange.getLowerBoundAsInstant());
        searchPeriod.setEnd(theRange.getUpperBoundAsInstant());
        Property docRefDateProperty = new Property(
                "date",
                "instant",
                "When the document reference was created.",
                0,
                1,
                (List<? extends Base>) null);
        argumentList.put(docRefDateProperty, searchPeriod);

        VirtualDBMethodOutcome outcome = getVirtualDBAccessor().getResourcesViaSearchCriteria(ResourceType.DocumentReference, argumentList);

        if (outcome.getStatusEnum() == VirtualDBActionStatusEnum.SEARCH_FINISHED) {
            Bundle searchOutcome = (Bundle) outcome.getResource();
            return (searchOutcome);
        } else {
            Bundle outputBundle = new Bundle();
            outputBundle.setType(Bundle.BundleType.SEARCHSET);
            outputBundle.setTimestamp(Date.from(Instant.now()));
            outputBundle.setTotal(0);
            return (outputBundle);
        }
    }
}
