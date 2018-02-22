/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.organisation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.organisation.Organisation;
import org.olat.modules.organisation.OrganisationManagedFlag;
import org.olat.modules.organisation.OrganisationService;
import org.olat.modules.organisation.ui.OrganisationTreeDataModel.OrganisationCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTreeAdminController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private OrganisationTreeDataModel model;
	private FormLink createOrganisationButton;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private EditOrganisationController newOrganisationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private int counter = 0;
	
	@Autowired
	private OrganisationService organisationService;
	
	public OrganisationTreeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "organisation_list");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createOrganisationButton = uifactory.addFormLink("create.organisation", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrganisationCols.key, "select"));

		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer();
		treeNodeRenderer.setFlatBySearchAndFilter(true);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationCols.identifier));
		DefaultFlexiColumnModel selectColumn = new DefaultFlexiColumnModel("select", translate("select"), "select");
		selectColumn.setExportable(false);
		selectColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(selectColumn);
		DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(OrganisationCols.tools);
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);

		model = new OrganisationTreeDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		//tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_organisations_listing");
		tableEl.setEmtpyTableMessageKey("table.organisation.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(24);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<Organisation> organisations = organisationService.getOrganisations();
		List<OrganisationRow> rows = new ArrayList<>(organisations.size());
		Map<Long,OrganisationRow> organisationKeyToRows = new HashMap<>();
		for(Organisation organisation:organisations) {
			OrganisationRow row = forgeRow(organisation);
			rows.add(row);
			organisationKeyToRows.put(organisation.getKey(), row);
		}
		
		//parent line
		for(OrganisationRow row:rows) {
			if(row.getParentOrganisationKey() != null) {
				row.setParent(organisationKeyToRows.get(row.getParentOrganisationKey()));
			}
		}
		
		Collections.sort(rows, new FlexiTreeNodeComparator());
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private OrganisationRow forgeRow(Organisation organisation) {
		//tools
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		OrganisationRow row = new OrganisationRow(organisation, toolsLink);
		toolsLink.setUserObject(row);
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newOrganisationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newOrganisationCtrl);
		removeAsListenerAndDispose(cmc);
		newOrganisationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(createOrganisationButton == source) {
			doCreateOrganisation(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				OrganisationRow row = (OrganisationRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doCreateOrganisation(UserRequest ureq) {
		if(newOrganisationCtrl != null) return;

		newOrganisationCtrl = new EditOrganisationController(ureq, getWindowControl(), null);
		listenTo(newOrganisationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newOrganisationCtrl.getInitialComponent(), true, translate("create.organisation"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateOrganisation(UserRequest ureq, Organisation parentOrganisation) {
		if(newOrganisationCtrl != null) return;

		newOrganisationCtrl = new EditOrganisationController(ureq, getWindowControl(), null, parentOrganisation);
		listenTo(newOrganisationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newOrganisationCtrl.getInitialComponent(), true, translate("create.organisation"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, OrganisationRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		Organisation organisation = organisationService.getOrganisation(row);
		if(organisation == null) {
			tableEl.reloadData();
			showWarning("warning.organisation.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, organisation);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doMove(UserRequest ureq, Organisation organisation) {
		//TODO
	}
	
	private void doSelectOrganisation(UserRequest ureq, Organisation organisation) {
		//TODO
	}
	
	private void doConfirmDelete(UserRequest ureq, OrganisationRow row) {
		//TODO 
	}
	

	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link editLink;
		private Link moveLink;
		private Link newLink;
		private Link deleteLink;
		
		private OrganisationRow row;
		private Organisation organisation;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, OrganisationRow row, Organisation organisation) {
			super(ureq, wControl);
			this.row = row;
			this.organisation = organisation;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(6);
			
			//edit
			editLink = addLink("edit", "o_icon_edit", links);
			if(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.move)) {
				moveLink = addLink("move.organisation.level", "o_icon_move", links);
			}
			newLink = addLink("add.organisation.under", "o_icon_levels", links);
			if(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}
			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doSelectOrganisation(ureq, organisation);
			} else if(moveLink == source) {
				close();
				doMove(ureq, organisation);
			} else if(newLink == source) {
				close();
				doCreateOrganisation(ureq, organisation);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}