/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementXml.java,v $
* Date   : $Date: 2002/06/21 15:35:20 $
* Version: $Revision: 1.20 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.template.cache;

import java.util.*;
import com.opencms.boot.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;

/**
 * An instance of CmsElementXML represents an requestable Element in the OpenCms
 * element cache area. It contains all informations to generate the content of this
 * element. It also stores the variants of once generated content to speed up
 * performance.
 *
 * It points to other depending elements. Theses elements are called to generate
 * their content on generation-time.
 *
 * @author Alexander Lucas
 */
public class CmsElementXml extends A_CmsElement implements com.opencms.boot.I_CmsLogChannels {

    /**
     * Constructor for an element with the given class and template name.
     */
    public CmsElementXml(String className, String templateName, String readAccessGroup, CmsCacheDirectives cd, int variantCachesize) {
        init(className, templateName, readAccessGroup, cd, variantCachesize);
    }

    /**
     * A construcor which creates an element with the given element
     * definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param readAccessGroup The group that may read the element.
     * @param cd Cache directives for this element
     * @param defs CmsElementDefinitionCollection for this element.
     */
    public CmsElementXml(String className, String templateName, String readAccessGroup, CmsCacheDirectives cd, CmsElementDefinitionCollection defs, int variantCachesize) {
        init(className, templateName, readAccessGroup, cd, defs, variantCachesize);
    }

    /**
     * Get the content of this element.
     * @param elementCache Entry point for the element cache
     * @param cms CmsObject for accessing system resources
     * @param elDefs Definitions of this element's subelements
     * @param parameters All parameters of this request
     * @param methodParameter not used here, only methodelemets need it.
     * @return Byte array with the processed content of this element.
     * @exception CmsException
     */
    public byte[] getContent(CmsElementCache elementCache, CmsObject cms, CmsElementDefinitionCollection elDefs, String elementName, Hashtable parameters, String methodParameter) throws CmsException  {
        byte[] result = null;

        // Merge own element definitions with our parent's definitions
        CmsElementDefinitionCollection mergedElDefs = new CmsElementDefinitionCollection(elDefs, m_elementDefinitions);

        // Get out own cache directives
        A_CmsCacheDirectives cd = getCacheDirectives();

        boolean streamable = cms.getRequestContext().isStreaming();

        CmsElementVariant variant = null;

        // Now check, if there is a variant of this element in the cache.
        if(cd.isInternalCacheable()) {

            checkReadAccess(cms);
            // check if this Element has a date of expiry
            if (cd.isTimeCritical() && (m_timestamp < cd.getTimeout().getLastChange())){
                // this element is too old, delete the cache
                if(this.hasDependenciesVariants()){
                    // remove all the variants from the extern dep table
                    cms.getOnlineElementCache().getElementLocator().removeElementFromDependencies(
                                mergedElDefs.get(elementName).getDescriptor(), this);
                }
                clearVariantCache();
            }else{
                variant = getVariant(cd.getCacheKey(cms, parameters));
                if((variant != null) && variant.isTimeCritical()
                                     && variant.getNextTimeout() < System.currentTimeMillis()){
                    // the variant is not longer valid, remove it from the extern dependencies
                    cms.getOnlineElementCache().getElementLocator().removeVariantFromDependencies(
                                        m_className +"|" + m_templateName +"|" + cd.getCacheKey(cms, parameters), variant);
                    variant = null;
                }
            }
            if(variant != null) {
                result = resolveVariant(cms, variant, elementCache, mergedElDefs, elementName, parameters);
            }
        }
        if(variant == null) {
            // This element was not found in the variant cache.
            // We have to generate it by calling the "classic" getContent() method on the template
            // class.

            // Get template class.
            // In classic mode, this is donw by the launcher.
            I_CmsTemplate templateClass = null;
            try {
                templateClass = getTemplateClass(cms, m_className);
            } catch(Throwable e) {
                if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Could not load my template class \"" + m_className + "\". ");
                    A_OpenCms.log(C_OPENCMS_CRITICAL, e.toString());
                    return e.toString().getBytes();
                }
            }

            try {
                /*if(cd.isInternalCacheable()) {
                    System.err.println(toString() + " ### Variant not in cache. Must be generated.");
                } else {
                    System.err.println(toString() + " ### Element not cacheable. Generating variant temporarily.");
                }*/
                // startProcessing() later will be responsible for generating our new variant.
                // since the method resolveVariant (THIS method) will be called recursively
                // by startProcessing(), we have to pass the current element definitions.
                // Unfortunately, there is no other way than putting them into our parameter
                // hashtable. For compatibility reasons we are not allowed to change
                // the interface of getContent() or startProcessing()
                parameters.put("_ELDEFS_", mergedElDefs);
                String templateSelector = null;
                try{
                    templateSelector = mergedElDefs.get(elementName).getTemplateSelector();
                }catch(Exception e){
                }
                try {
                    String theTemplate = m_templateName;
                    if(theTemplate == null){
                        try{
                            theTemplate = mergedElDefs.get("body").getTemplateName();
                        }catch(Exception exc){
                            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " could not find the body element to get the default templatefile for " + this.toString() );
                            }
                        }
                    }
                    result = templateClass.getContent(cms, theTemplate, elementName, parameters, templateSelector);
                } catch(Exception e) {
                    if(e instanceof CmsException) {
                        CmsException ce = (CmsException)e;
                        if(ce.getType() == ce.C_ACCESS_DENIED) {
                            // This was an access denied exception.
                            // This is not very critical at the moment.
                            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(C_OPENCMS_DEBUG, toString() + " Access denied in getContent for template class " + m_className);
                            }
                        } else {
                            // Any other CmsException.
                            // This could be more critical.
                            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(C_OPENCMS_INFO, toString() + " Error in getContent for template class " + m_className);
                            }
                        }
                        throw ce;
                    } else {
                        // No CmsException. This is really, really bad!
                        if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Non OpenCms error occured in getContent for template class " + m_className);
                        }
                        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                }
            }
            catch(CmsException e) {
                // Clear cache and do logging here
                throw e;
            }
        }
        if(streamable) {
            try {
                cms.getRequestContext().getResponse().getOutputStream().write(result);
            } catch(Exception e) {
                if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, this.toString() + " Error while streaming!");
                }
            }
            result = null;
        }
        return result;
    }
}