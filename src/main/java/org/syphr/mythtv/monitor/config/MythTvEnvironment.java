/*
 * Copyright 2011 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.syphr.mythtv.monitor.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.syphr.mythtv.api.MythVersion;
import org.syphr.mythtv.monitor.xsd.config.x10.BackendType;
import org.syphr.mythtv.monitor.xsd.config.x10.BackendsType;
import org.syphr.mythtv.monitor.xsd.config.x10.MythTvType;

public class MythTvEnvironment
{
    private final MythVersion mythVersion;

    private BackendHost masterBackend;
    private final Map<String, BackendHost> slaveBackends;

    // TODO frontends

    public MythTvEnvironment(MythTvType config)
    {
        mythVersion = MythVersion.valueOf("_" + config.getVersion().replace('.', '_'));

        slaveBackends = new HashMap<String, BackendHost>();
        BackendsType backendsType = config.getBackends();
        if (backendsType != null)
        {
            masterBackend = new BackendHost(backendsType.getMaster(), mythVersion);

            for (BackendType slave : backendsType.getSlaveArray())
            {
                slaveBackends.put(slave.getHost(), new BackendHost(slave, mythVersion));
            }
        }
    }

    public MythVersion getMythVersion()
    {
        return mythVersion;
    }

    public BackendHost getMasterBackend()
    {
        return masterBackend;
    }

    public List<BackendHost> getAllBackends()
    {
        // TODO
        return Collections.singletonList(getMasterBackend());
    }

    public BackendHost getBackend(String host)
    {
        // TODO
        if (masterBackend.getHost().equals(host))
        {
            return masterBackend;
        }

        return null;
    }
}
