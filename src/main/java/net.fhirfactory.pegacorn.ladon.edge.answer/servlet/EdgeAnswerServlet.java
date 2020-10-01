package net.fhirfactory.pegacorn.platform.edge.answer.servlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import net.fhirfactory.pegacorn.platform.edge.answer.provider.DocumentReferenceProvider;
import net.fhirfactory.pegacorn.platform.edge.answer.provider.PractitionerProvider;

import java.util.ArrayList;
import java.util.List;

public class EdgeAnswerServlet extends RestfulServer {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public EdgeAnswerServlet() {
        super(FhirContext.forR4()); // This is an STU3 server
    }

    /**
     * This method is called automatically when the
     * servlet is initializing.
     */
    @Override
    public void initialize() {
        /*
         * Two resource providers are defined. Each one handles a specific
         * type of resource.
         */
        List<IResourceProvider> providers = new ArrayList<IResourceProvider>();
        providers.add(new DocumentReferenceProvider());
        providers.add(new PractitionerProvider());
        setResourceProviders(providers);

        /*
         * Use a narrative generator. This is a completely optional step,
         * but can be useful as it causes HAPI to generate narratives for
         * resources which don't otherwise have one.
         */
        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);

        /*
         * Use nice coloured HTML when a browser is used to request the content
         */
        registerInterceptor(new ResponseHighlighterInterceptor());

    }

}
