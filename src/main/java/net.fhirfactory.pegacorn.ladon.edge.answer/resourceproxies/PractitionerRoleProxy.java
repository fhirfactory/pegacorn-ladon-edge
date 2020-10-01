package net.fhirfactory.pegacorn.ladon.edge.answer.provider;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class PractitionerRoleProvider implements IResourceProvider {
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return null;
    }
}
