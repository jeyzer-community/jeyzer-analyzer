
package org.jeyzer.monitor.engine.event;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */



import java.util.Comparator;
import java.util.Date;

/*-
 * #%L
 * Jeyzer Analyzer
 * %%
 * Copyright (C) 2020 Jeyzer
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Marker interface for applicative and publisher events
 */
public interface MonitorApplicativeEvent {
	
	public Date getApplicativeStartDate();
	
	public Date getApplicativeEndDate();

	
	public static class MonitorEventComparable implements Comparator<MonitorEvent>{
		 
	    @Override
	    public int compare(MonitorEvent e1, MonitorEvent e2) {
	    	Date d1 = e1 instanceof MonitorApplicativeEvent ? ((MonitorApplicativeEvent)e1).getApplicativeStartDate() : e1.getStartDate();
	    	Date d2 = e2 instanceof MonitorApplicativeEvent ? ((MonitorApplicativeEvent)e2).getApplicativeStartDate() : e2.getStartDate();
	    	return d1.compareTo(d2);
	    }
	}
}
