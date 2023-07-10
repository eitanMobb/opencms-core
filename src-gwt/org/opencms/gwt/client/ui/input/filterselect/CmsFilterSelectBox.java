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

package org.opencms.gwt.client.ui.input.filterselect;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsFilterSelectCss;
import org.opencms.gwt.client.ui.input.A_CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsLabelSelectCell;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Select box that allows client-side filtering for its options.
 *
 * <p>Filtering is done by a case-insensitive substring test on the user-readable select option texts.
 */
public class CmsFilterSelectBox extends A_CmsSelectBox<CmsLabelSelectCell> implements I_CmsHasInit {

    /** The widget type identifier. */
    public static final String WIDGET_TYPE = "filterselect";

    /** The CSS bundle. */
    private static final I_CmsFilterSelectCss FILTERSELECT_CSS = I_CmsLayoutBundle.INSTANCE.filterSelectCss();

    /** The text box used for filtering. */
    private TextBox m_filterBox;

    /** The currently active timer that, when it fires, will update the filtering. */
    private Timer m_filterTimer;

    /**
     * Creates a new instance.
     */
    public CmsFilterSelectBox() {

        addStyleName(FILTERSELECT_CSS.filterSelect());
        sinkEvents(Event.ONMOUSEWHEEL);
    }

    /**
     * Creates a new instance.
     *
     * @param options the select options
     */
    public CmsFilterSelectBox(LinkedHashMap<String, String> options) {

        this();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            addOption(new CmsLabelSelectCell(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map, com.google.common.base.Optional)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams, Optional<String> defaultValue) {

                return new CmsFilterSelectBox(new LinkedHashMap<>(widgetParams));
            }
        });

    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#displayingAbove()
     */
    @Override
    public boolean displayingAbove() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();

    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        if (event.getTypeInt() == Event.ONMOUSEWHEEL) {
            event.preventDefault();
            event.stopPropagation();
        } else {
            super.onBrowserEvent(event);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do

    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        m_filterBox.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#close()
     */
    @Override
    protected void close() {

        super.close();

        // Use a timer for the case where the select box is currently opened, the text box has focus, and the user clicks on the text box again
        Timer blurTimer = new Timer() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                InputElement inputElem = m_filterBox.getElement().cast();
                inputElem.blur();
            }
        };
        blurTimer.schedule(0);
        m_filterBox.setValue(getOptionText(getFormValueAsString()));
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#createUnknownOption(java.lang.String)
     */
    @Override
    protected CmsLabelSelectCell createUnknownOption(String value) {

        return new CmsLabelSelectCell(value, value);
    }

    /**
     * Updates the visibility of select options based on the given filter string.
     *
     * <p>An option matches the filter if the display text contains the filter string as a substring, without regard
     * for case.
     *
     * @param filter the filter string
     */
    protected void filterCells(String filter) {

        String lowerCaseFilter = null;
        if (filter != null) {
            lowerCaseFilter = filter.toLowerCase();
        }
        for (Map.Entry<String, CmsLabelSelectCell> entry : m_selectCells.entrySet()) {
            boolean show = (filter == null) || entry.getValue().getText().toLowerCase().contains(lowerCaseFilter);
            entry.getValue().setVisible(show);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#initOpener()
     */
    @Override
    protected void initOpener() {

        m_filterBox = new TextBox();
        m_opener.setWidget(m_filterBox);
        // when the user types very fast, we don't want to update the filtering
        // after every keypress, so we 'debounce' the event handling using a timer
        m_filterBox.addKeyDownHandler(event -> {
            if (m_filterTimer != null) {
                m_filterTimer.cancel();
            }
            m_filterTimer = new Timer() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    m_filterTimer = null;
                    if (m_openClose.isDown()) {
                        filterCells(m_filterBox.getValue());
                    }

                }
            };
            m_filterTimer.schedule(150);
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#open()
     */
    @Override
    protected void open() {

        filterCells(null); // reset the filter before opening the select box
        super.open();
        m_filterBox.setFocus(true); // the focus panel somehow prevents the text box from gaining focus by clicking on it - so we focus it manually
        m_filterBox.selectAll(); // when we select everything, then text typed by the user replaces the content of the filter box and gets used as the filter string
        CmsLabelSelectCell cell = m_selectCells.get(getFormValueAsString());
        if (cell != null) {
            cell.getElement().scrollIntoView();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#truncateOpener(java.lang.String, int)
     */
    @Override
    protected void truncateOpener(String prefix, int width) {

        // not using truncation
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#updateOpener(java.lang.String)
     */
    @Override
    protected void updateOpener(String newValue) {

        String text = getOptionText(getFormValueAsString());
        m_filterBox.setValue(text);
        m_filterBox.setTitle(text);
    }

    /**
     * Gets the user-readable text for the option.
     *
     * @param key the key for the option
     * @return the user-readable text for the option
     */
    private String getOptionText(String key) {

        CmsLabelSelectCell cell = m_selectCells.get(key);
        if (cell != null) {
            return cell.getText();
        }
        return "";
    }

}
