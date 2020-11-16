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
package net.fhirfactory.pegacorn.ladon.edge.answer.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;

import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.DocumentReferenceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.DocumentReferenceProxy;
import net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.PatientProxy;
import net.fhirfactory.pegacorn.platform.edge.ask.PegacornHapiFhirProxy;
import net.fhirfactory.pegacorn.platform.edge.receive.common.ApiKeyValidatorInterceptor;
import net.fhirfactory.pegacorn.util.FhirUtil;


public abstract class EdgeAnswerServlet extends RestfulServer {
    abstract protected Logger getLogger();

    private static final long serialVersionUID = 1L;

    @Inject
    protected PatientProxy patientProxy;

    @Inject
    protected DocumentReferenceProxy docRefProxy;

    /**
     * Constructor
     */

    public EdgeAnswerServlet() {
        super(FhirUtil.getInstance().getFhirContext()); // This is an R4 server
    }

    /**
     * This method is called automatically when the
     * servlet is initializing.
     */
    @Override
    public void initialize() {
        getLogger().debug(".initialise(): entry");
        /*
         * Two resource providers are defined. Each one handles a specific
         * type of resource.
         */
        List<IResourceProvider> providers = new ArrayList<IResourceProvider>();
//        providers.add(new CareTeamProxy());
            providers.add(docRefProxy);
//        providers.add(new GroupProxy());
//        providers.add(new HealthCareServiceProxy());
//        providers.add(new LocationProxy());
// providers.add(new OrganizationProxy()); */
//        providers.add(patientProxy);
//        providers.add(new PractitionerProxy());
//        providers.add(new PractitionerRoleProxy());
//        providers.add(new ValueSetProxy()); */
        setResourceProviders(providers);

        /*
         * Use a narrative generator. This is a completely optional step,
         * but can be useful as it causes HAPI to generate narratives for
         * resources which don't otherwise have one.
         */
        INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
        getFhirContext().setNarrativeGenerator(narrativeGen);

        ApiKeyValidatorInterceptor apiKeyValidatorInterceptor = new ApiKeyValidatorInterceptor(PegacornHapiFhirProxy.API_KEY_HEADER_NAME, PegacornHapiFhirProxy.DEFAULT_API_KEY_PROPERTY_NAME);
        registerInterceptor(apiKeyValidatorInterceptor);
        
        /*
         * Use nice coloured HTML when a browser is used to request the content
         */
        registerInterceptor(new ResponseHighlighterInterceptor());        
        getLogger().debug(".initialize(): Exit");
    }

}
