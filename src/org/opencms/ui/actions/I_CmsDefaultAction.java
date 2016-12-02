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

package org.opencms.ui.actions;

import org.opencms.ui.I_CmsDialogContext;

/**
 * Marks workplace actions as an default action to be executed on item click within the file table.<p>
 */
public interface I_CmsDefaultAction extends I_CmsWorkplaceAction {

    /**
     * Executes the action.<p>
     *
     * @param context the current dialog context.<p>
     * @param hasModifier <code>true</code> in case the action was triggered with an active modifier
     */
    void executeAction(I_CmsDialogContext context, boolean hasModifier);

    /**
     * Returns the action rank, the highest ranked default action will be used.<p>
     *
     * @param context the dialog context
     *
     * @return the action rank
     */
    int getDefaultActionRank(I_CmsDialogContext context);

}
