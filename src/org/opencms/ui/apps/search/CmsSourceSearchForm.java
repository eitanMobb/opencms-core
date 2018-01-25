/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.CmsVaadinUtils.PropertyId;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The source search form.<p>
 */
public class CmsSourceSearchForm extends VerticalLayout {

    /** The available search types. */
    public static enum SearchType {
        /** XML content values only. */
        contentValues(false, true, false),
        /** Full text search. */
        fullText(false, false, false),
        /** Filter using a solr index, before searching for matches. */
        solr(true, false, false),
        /** Filter using a solr index, before searching for matches, XML content values only. */
        solrContentValues(true, true, false),

        /** Property search. */
        properties(false, false, true),
        /** */
        resourcetype(false, false, false);

        /** The content values only flag. */
        private boolean m_contentValuesOnly;

        /** The is solr search flag. */
        private boolean m_solrSearch;

        /** The property flag.*/
        private boolean m_property;

        /**
         * Constructor.<p>
         *
         * @param solrSearch the is solr search flag
         * @param contentValuesOnly the content values only flag
         * @param property the property flag
         */
        private SearchType(boolean solrSearch, boolean contentValuesOnly, boolean property) {

            m_solrSearch = solrSearch;
            m_contentValuesOnly = contentValuesOnly;
            m_property = property;
        }

        /**
         * Returns whether this is a content values only search type.<p>
         *
         * @return <code>true</code> if this is a content values only search type
         */
        public boolean isContentValuesOnly() {

            return m_contentValuesOnly;
        }

        /**
         * Returns whether this is a property search type.<p>
         *
         * @return true if this is property search
         *  */
        public boolean isPropertySearch() {

            return m_property;
        }

        /**
         * Returns whether this is a SOLR search type.<p>
         *
         * @return <code>true</code> if this is a SOLR search type
         */
        public boolean isSolrSearch() {

            return m_solrSearch;
        }
    }

    /**Regex expression for finding all. */
    public static final String REGEX_ALL = ".*";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSourceSearchForm.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1023130318064811880L;

    /** The source search app instance. */
    private CmsSourceSearchApp m_app;

    /** The search locale select. */
    private ComboBox m_locale;

    /** The replace check box. */
    private CheckBox m_replace;

    /** The replace pattern field. */
    private TextField m_replacePattern;

    /** The resource type select. */
    private ComboBox m_resourceType;

    /** The search button. */
    private Button m_search;

    /** Check box to ignore subsites. */
    private CheckBox m_ignoreSubSites;

    /** The site select. */
    private ComboBox m_siteSelect;

    /** The search index select. */
    private ComboBox m_searchIndex;

    /** The search pattern field. */
    private TextField m_searchPattern;

    /** The search root path select. */
    private CmsPathSelectField m_searchRoot;

    /** The search root path select. */
    private CmsPathSelectField m_replaceResource;

    /** The search root path select. */
    private CmsPathSelectField m_resourceSearch;

    /** The search type select. */
    private ComboBox m_searchType;

    /** The property select.*/
    private ComboBox m_property;

    /** The SOLR query field. */
    private TextField m_solrQuery;

    /** The replace project. */
    private ComboBox m_workProject;

    /** The XPath field. */
    private TextField m_xPath;

    /**
     * Constructor.<p>
     *
     * @param app the source search app instance
     */
    public CmsSourceSearchForm(CmsSourceSearchApp app) {

        m_app = app;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        initFields();
        m_replace.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                updateReplace();
            }
        });
        m_searchType.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                changedSearchType();
            }
        });
        m_search.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                search();
            }
        });
        updateReplace();
        changedSearchType();
    }

    /**
     * Initializes the form with the given settings.<p>
     *
     * @param settings the settings
     */
    public void initFormValues(CmsSearchReplaceSettings settings) {

        m_siteSelect.setValue(settings.getSiteRoot());
        m_ignoreSubSites.setValue(new Boolean(settings.ignoreSubSites()));
        m_searchType.setValue(settings.getType());
        if (!settings.getPaths().isEmpty()) {
            m_searchRoot.setValue(settings.getPaths().get(0));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(settings.getTypes())) {
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(settings.getTypes());
                m_resourceType.setValue(type);
            } catch (CmsLoaderException e) {
                // nothing to do, skip setting the type
            }
        }
        m_searchPattern.setValue(settings.getSearchpattern());
        m_ignoreSubSites.setValue(new Boolean(settings.ignoreSubSites()));
        if (settings.getType().isContentValuesOnly()) {
            if (settings.getLocale() != null) {
                m_locale.setValue(settings.getLocale());
            }
            m_xPath.setValue(settings.getXpath());
        }
        if (settings.getType().isSolrSearch()) {
            m_solrQuery.setValue(settings.getQuery());
            m_searchIndex.setValue(settings.getSource());
        }

        if (settings.getType().isPropertySearch()) {
            m_property.select(settings.getProperty());
        }
        if (settings.getType().equals(SearchType.resourcetype)) {
            try {
                CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                cms.getRequestContext().setSiteRoot("");
                m_resourceSearch.setValue(
                    cms.readResource(
                        new CmsUUID(
                            settings.getSearchpattern().substring(
                                settings.getSearchpattern().indexOf("<uuid>") + 6,
                                settings.getSearchpattern().indexOf("</uuid>")))).getRootPath());
            } catch (CmsException e) {
                LOG.error("Unable to read resource", e);
            }

        }
    }

    /**
     * Handles search type changes.<p>
     */
    void changedSearchType() {

        SearchType type = (SearchType)m_searchType.getValue();

        m_property.setVisible(type.isPropertySearch());
        m_searchPattern.setVisible(!type.equals(SearchType.resourcetype));
        m_resourceSearch.setVisible(type.equals(SearchType.resourcetype));
        if (!type.equals(SearchType.resourcetype)) {
            m_ignoreSubSites.setValue(Boolean.FALSE);
            m_ignoreSubSites.setVisible(false);
        } else {
            m_ignoreSubSites.setVisible(true);
        }

        m_searchIndex.setVisible(type.isSolrSearch());
        m_solrQuery.setVisible(type.isSolrSearch());
        updateReplace();
        m_xPath.setVisible(type.isContentValuesOnly());
        m_locale.setVisible(type.isContentValuesOnly());

        m_resourceType.setVisible(!type.isPropertySearch() & !type.equals(SearchType.resourcetype));

        IndexedContainer types = (IndexedContainer)m_resourceType.getContainerDataSource();
        types.removeAllContainerFilters();
        types.addContainerFilter(
            type.isContentValuesOnly() ? CmsVaadinUtils.FILTER_XML_CONTENTS : CmsVaadinUtils.FILTER_NO_FOLDERS);
    }

    /**
     * Calls the search for the given parameters.<p>
     */
    void search() {

        CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
        settings.setSiteRoot((String)m_siteSelect.getValue());
        settings.setType((SearchType)m_searchType.getValue());
        settings.setPaths(Collections.singletonList(m_searchRoot.getValue()));
        settings.setIgnoreSubSites(m_ignoreSubSites.getValue().booleanValue());
        I_CmsResourceType type = (I_CmsResourceType)m_resourceType.getValue();
        if (type != null) {
            settings.setTypes(type.getTypeName());
        }
        if (SearchType.resourcetype.equals(m_searchType.getValue())) {
            settings.setTypes(CmsResourceTypeXmlContainerPage.getStaticTypeName());
        }

        if (m_replace.getValue().booleanValue()) {
            try {
                CmsProject workProject = A_CmsUI.getCmsObject().readProject((CmsUUID)m_workProject.getValue());
                settings.setProject(workProject.getName());
            } catch (CmsException e) {
                // ignore
            }
            if (SearchType.resourcetype.equals(m_searchType.getValue())) {
                try {
                    CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    cms.getRequestContext().setSiteRoot("");
                    CmsResource resource = cms.readResource(m_replaceResource.getValue());
                    settings.setReplacepattern(
                        "<target><![CDATA["
                            + resource.getRootPath()
                            + "]]></target>//t<uuid>"
                            + resource.getStructureId().getStringValue()
                            + "</uuid>");
                } catch (CmsException e) {
                    LOG.error("Unable to read resource.", e);
                }
            } else {

                settings.setReplacepattern(m_replacePattern.getValue());
            }

        }

        if (SearchType.resourcetype.equals(m_searchType.getValue())) {
            try {
                CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                cms.getRequestContext().setSiteRoot("");
                CmsResource resource = cms.readResource(m_resourceSearch.getValue());
                settings.setSearchpattern(
                    "<target>.{0,9}"
                        + resource.getRootPath()
                        + ".{0,3}</target>\\s*<uuid>"
                        + resource.getStructureId().getStringValue()
                        + "</uuid>");
            } catch (CmsException e) {
                LOG.error("Unable to read resource.", e);
            }
        } else {

            settings.setSearchpattern(m_searchPattern.getValue());
        }
        if (settings.getType().isContentValuesOnly()) {
            if (m_locale.getValue() != null) {
                settings.setLocale(m_locale.getValue().toString());
            }
            settings.setXpath(m_xPath.getValue());
        }
        if (settings.getType().isSolrSearch()) {
            settings.setQuery(m_solrQuery.getValue());
            settings.setSource((String)m_searchIndex.getValue());
        }

        if (settings.getType().isPropertySearch()) {
            settings.setProperty((CmsPropertyDefinition)m_property.getValue());
            settings.setForceReplace(m_replace.getValue().booleanValue());
        }

        m_app.search(settings, true);
    }

    /**
     * Toggles the replace option.<p>
     */
    void updateReplace() {

        boolean replace = m_replace.getValue().booleanValue();

        m_replaceResource.setVisible(replace ? SearchType.resourcetype.equals(m_searchType.getValue()) : replace);
        m_replacePattern.setVisible(replace ? !SearchType.resourcetype.equals(m_searchType.getValue()) : replace);

        m_workProject.setVisible(replace);

        m_search.setCaption(
            replace
            ? CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_REPLACE_0)
            : CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SEARCH_0));

    }

    /**
     * Initializes the form fields.<p>
     */
    private void initFields() {

        CmsObject cms = A_CmsUI.getCmsObject();
        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();

        if (m_searchPattern.getValue().isEmpty()) {
            m_searchPattern.setValue(REGEX_ALL);
        }
        m_resourceSearch.setUseRootPaths(true);
        m_replaceResource.setUseRootPaths(true);

        m_siteSelect.setContainerDataSource(
            CmsVaadinUtils.getAvailableSitesContainer(cms, CmsVaadinUtils.PROPERTY_LABEL));
        m_siteSelect.setItemCaptionPropertyId(CmsVaadinUtils.PROPERTY_LABEL);
        m_siteSelect.setTextInputAllowed(true);
        m_siteSelect.setNullSelectionAllowed(false);
        m_siteSelect.setFilteringMode(FilteringMode.CONTAINS);
        m_siteSelect.setValue(cms.getRequestContext().getSiteRoot());
        try {
            for (CmsPropertyDefinition prop : A_CmsUI.getCmsObject().readAllPropertyDefinitions()) {
                m_property.addItem(prop);
                m_property.setItemCaption(prop, prop.getName());
            }
        } catch (CmsException e) {
            //
        }
        m_property.setNullSelectionAllowed(false);
        m_property.select(m_property.getItemIds().iterator().next());
        m_property.setFilteringMode(FilteringMode.CONTAINS);
        m_searchType.setFilteringMode(FilteringMode.OFF);
        m_searchType.setNullSelectionAllowed(false);
        m_searchType.addItem(SearchType.fullText);
        m_searchType.setItemCaption(
            SearchType.fullText,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_FULLTEXT_0));
        m_searchType.addItem(SearchType.contentValues);
        m_searchType.setItemCaption(
            SearchType.contentValues,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_XMLCONTENT_0));
        m_searchType.addItem(SearchType.properties);
        m_searchType.setItemCaption(
            SearchType.properties,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_PROPERTY_SEARCH_0));
        m_searchType.addItem(SearchType.resourcetype);
        m_searchType.setItemCaption(
            SearchType.resourcetype,
            CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_RESOURCE_SEARCH_0));
        if (OpenCms.getSearchManager().getSolrServerConfiguration().isEnabled()) {

            m_searchIndex.setFilteringMode(FilteringMode.OFF);
            m_searchIndex.setNullSelectionAllowed(false);
            String selectIndex = null;
            for (CmsSearchIndex index : OpenCms.getSearchManager().getAllSolrIndexes()) {
                boolean offlineMode = CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode());
                // in case the current project is offline, show offline indexes, otherwise show online indexes
                if ((!online && offlineMode) || (online && !offlineMode)) {
                    m_searchIndex.addItem(index.getName());
                    if (selectIndex == null) {
                        selectIndex = index.getName();
                    }
                }
            }
            if (selectIndex != null) {
                m_searchIndex.setValue(selectIndex);

                // only add the solr search types if there is an index available
                m_searchType.addItem(SearchType.solr);
                m_searchType.setItemCaption(
                    SearchType.solr,
                    CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_SOLR_0));
                m_searchType.addItem(SearchType.solrContentValues);
                m_searchType.setItemCaption(
                    SearchType.solrContentValues,
                    CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_SERACH_TYPE_SOLR_CONTENT_VALUES_0));

            }
        }
        m_searchType.setValue(SearchType.fullText);

        m_searchRoot.setValue("/");

        m_locale.setFilteringMode(FilteringMode.OFF);
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            m_locale.addItem(locale);
        }

        m_resourceType.setNullSelectionAllowed(true);
        IndexedContainer resTypes = CmsVaadinUtils.getResourceTypesContainer();
        resTypes.addContainerFilter(CmsVaadinUtils.FILTER_NO_FOLDERS);

        m_resourceType.setContainerDataSource(resTypes);
        m_resourceType.setItemCaptionPropertyId(PropertyId.caption);
        m_resourceType.setItemIconPropertyId(PropertyId.icon);
        m_resourceType.setFilteringMode(FilteringMode.CONTAINS);

        m_workProject.setNullSelectionAllowed(false);
        IndexedContainer projects = CmsVaadinUtils.getProjectsContainer(A_CmsUI.getCmsObject(), "caption");
        projects.removeItem(CmsProject.ONLINE_PROJECT_ID);
        m_workProject.setContainerDataSource(projects);
        m_workProject.setItemCaptionPropertyId("caption");

        if (online) {
            m_replace.setEnabled(false);
        } else {
            m_workProject.setValue(cms.getRequestContext().getCurrentProject().getUuid());
        }
    }
}
