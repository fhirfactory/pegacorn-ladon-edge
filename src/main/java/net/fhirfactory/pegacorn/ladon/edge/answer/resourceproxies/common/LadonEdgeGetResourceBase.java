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
package net.fhirfactory.pegacorn.ladon.edge.answer.resourceproxies.common;

import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import org.hl7.fhir.r4.model.*;

public abstract class LadonEdgeGetResourceBase extends LadonEdgeProxyBase{
    private static String parameterSepeator = "|";

    /**
     * This is the default findByIdentifier() search function. It is called by the subclass (@Search annotated) public
     * Resource specific findByIdentifier() classes after those classes have resolved the Identifier.
     *
     * @param identifier the (partially populated) Identifier to search for
     * @return A FHIR::Bundle containing one or more resources matching the Identifier or an empty FHIR::Bundle.
     */
    protected Bundle findByIdentifier(Identifier identifier) {
        getLogger().debug(".findByIdentifier(): Entry, identifier --> {}", identifier);

        VirtualDBMethodOutcome outcome = getVirtualDBAccessor().findResourceByIdentifier(identifier);

        if (outcome.getStatusEnum().equals(VirtualDBActionStatusEnum.REVIEW_FINISH) || outcome.getStatusEnum().equals(VirtualDBActionStatusEnum.SEARCH_FINISHED)) {
            getLogger().trace("findByIdentifier(): search is finished, extracting result bundle");
            Bundle searchOutcome = (Bundle) outcome.getResource();
            getLogger().debug("findByIdentifier(): Exit, resulting Bundle contains {} Resources", searchOutcome.getTotal());
            return (searchOutcome);
        } else {
            Bundle emptyOutcomeBundle = searchProcessHasFailed(".findByIdentifier()");
            getLogger().debug("findByIdentifier(): Exit, search failed (not just not finding any matching resources, but the search process itself faield)!");
            return (emptyOutcomeBundle);
        }
    }


    @Override
    protected VirtualDBMethodOutcome getResource(IdType id) {
        return null;
    }

    /**
     * This method converts a received TokenParam (representing a FHIR::Identifier) and converts it to
     * an actual FHIR::Identifier object.
     *
     * Contrary to the understood structure of the TokenParam attribute within the HAPI FHIR documentation,
     * Pegacorn maps the System & Code to be associated to the FHIR::Identifier.type.system &  FHIR::Identifier.type.code
     * rather than the System attribute of the TokenParam mapping to the FHIR::Identifier.system.
     *
     * @param identifierParam The TokenParam attribute representing an FHIR::Identifier
     * @return A (minimally populated) FHIR::Identifier element.
     */
    protected Identifier tokenParam2Identifier(TokenParam identifierParam){
        getLogger().debug(".tokenParam2Identifier(): Entry, identifierParam (TokenParam) --> {}", identifierParam);
        if(identifierParam == null){
            getLogger().warn(".tokenParam2Identifier(): Parameter identifierParam (TokenParam) is null");
            return(null);
        }
        String identifierTypeSystemValue = identifierParam.getSystem();
        String identifierTypeCodeValue = null;
        String identifierValue = null;
        if(identifierParam.getModifier().getValue().equals(TokenParamModifier.OF_TYPE.getValue())){
            String paramValue = identifierParam.getValue();
            String values[] = paramValue.split("|");
            identifierTypeCodeValue = values[0];
            identifierValue = values[1];
        } else {
            identifierValue = identifierParam.getValue();
            identifierTypeCodeValue = "";
        }
        CodeableConcept identifierType = new CodeableConcept();
        Coding identifierTypeCode = new Coding();
        identifierTypeCode.setCode(identifierTypeCodeValue);
        identifierTypeCode.setSystem(identifierTypeSystemValue);
        identifierType.addCoding(identifierTypeCode);
        identifierType.setText(identifierTypeSystemValue + ":" + identifierTypeCodeValue);
        Identifier generatedIdentifier = new Identifier();
        generatedIdentifier.setType(identifierType);
        generatedIdentifier.setValue(identifierValue);
        getLogger().debug(".tokenParam2Identifier(): Exit, created Identifier --> {}", generatedIdentifier);
        return(generatedIdentifier);
    }
}
