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
package org.olat.modules.taxonomy.ui.events;

import org.olat.core.gui.control.Event;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewTaxonomyLevelEvent extends Event {

	private static final long serialVersionUID = -1856658753352539989L;
	public static final String NEW_TAXONOMY_LEVEL_EVENT = "new-taxonomy-level-event";
	
	private final TaxonomyLevel taxonomyLevel;
	
	public NewTaxonomyLevelEvent(TaxonomyLevel taxonomyLevel) {
		super(NEW_TAXONOMY_LEVEL_EVENT);
		this.taxonomyLevel = taxonomyLevel;
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
}
