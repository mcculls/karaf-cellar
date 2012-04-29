/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cellar.obr.shell;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.cellar.core.Configurations;
import org.apache.karaf.cellar.core.Group;
import org.apache.karaf.cellar.core.control.SwitchStatus;
import org.apache.karaf.cellar.core.event.EventProducer;
import org.apache.karaf.cellar.core.event.EventType;
import org.apache.karaf.cellar.core.shell.CellarCommandSupport;
import org.apache.karaf.cellar.obr.Constants;
import org.apache.karaf.cellar.obr.ObrBundleEvent;

import java.util.Set;

@Command(scope = "cluster", name = "obr-deploy", description = "Deploy a bundle from the OBR assigned to a cluster group.")
public class ObrDeployCommand extends CellarCommandSupport {

    @Argument(index = 0, name = "group", description = "The cluster group name.", required = true, multiValued = false)
    String groupName;

    @Argument(index = 1, name="bundleId", description = "The bundle ID (symbolicname,version in the OBR) to deploy.", required = true, multiValued = false)
    String bundleId;

    @Option(name = "-s", aliases = { "--start" }, description = "Start the deployed bundles.", required = false, multiValued = false)
    boolean start = false;

    private EventProducer eventProducer;

    @Override
    protected Object doExecute() throws Exception {
        // check if the group exists
        Group group = groupManager.findGroupByName(groupName);
        if (group == null) {
            System.err.println("Cluster group " + groupName + " doesn't exist");
            return null;
        }

        // check if the producer is ON
        if (eventProducer.getSwitch().getStatus().equals(SwitchStatus.OFF)) {
            System.err.println("Cluster event producer is OFF for this node");
            return null;
        }

        // create an event and produce it
        int type = 0;
        if (start) type = Constants.BUNDLE_START_EVENT_TYPE;
        ObrBundleEvent event = new ObrBundleEvent(bundleId, type);
        event.setForce(true);
        event.setSourceGroup(group);
        eventProducer.produce(event);

        return null;
    }

}
