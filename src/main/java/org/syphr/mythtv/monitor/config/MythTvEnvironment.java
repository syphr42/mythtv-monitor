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

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.syphr.mythtv.api.MythVersion;
import org.syphr.mythtv.api.backend.Backend;
import org.syphr.mythtv.api.frontend.Frontend;
import org.syphr.mythtv.monitor.xsd.config.x10.BackendType;
import org.syphr.mythtv.monitor.xsd.config.x10.BackendsType;
import org.syphr.mythtv.monitor.xsd.config.x10.FrontendType;
import org.syphr.mythtv.monitor.xsd.config.x10.FrontendsType;
import org.syphr.mythtv.monitor.xsd.config.x10.MythTvType;
import org.syphr.mythtv.protocol.ConnectionType;

public class MythTvEnvironment
{
    private final MythVersion mythVersion;

    private Backend masterBackend;
    private final Map<String, Backend> slaveBackends;

    private final Map<String, Frontend> frontends;

    public MythTvEnvironment(MythTvType config)
    {
        mythVersion = MythVersion.valueOf("_" + config.getVersion().replace('.', '_'));

        slaveBackends = new HashMap<String, Backend>();
        BackendsType backendsType = config.getBackends();
        if (backendsType != null)
        {
            masterBackend = buildBackend(backendsType.getMaster());

            for (BackendType slave : backendsType.getSlaveArray())
            {
                slaveBackends.put(slave.getHost(), buildBackend(slave));
            }
        }

        frontends = new HashMap<String, Frontend>();
        FrontendsType frontendsType = config.getFrontends();
        if (frontendsType != null)
        {
            for (FrontendType frontendType : frontendsType.getFrontendArray())
            {
                Frontend frontend = new Frontend(mythVersion);
                frontend.setFrontendConnectionParameters(frontendType.getHost(),
                                                         frontendType.getControlPort(),
                                                         frontendType.getHttpPort());
                frontend.setConnectionTimeout(frontendType.getControlTimeout(),
                                              TimeUnit.MILLISECONDS);

                frontends.put(frontendType.getHost(), frontend);
            }
        }
    }

    private Backend buildBackend(BackendType backendType)
    {
        String localHost;
        try
        {
            localHost = InetAddress.getLocalHost().getHostName();
        }
        catch (IOException e)
        {
            localHost = "localHost";
        }

        Backend backend = new Backend(mythVersion);
        backend.setBackendConnectionParameters(localHost,
                                               backendType.getHost(),
                                               backendType.getProtocolPort(),
                                               ConnectionType.MONITOR, // TODO
                                               backendType.getHttpPort());

        return backend;
    }

    public MythVersion getMythVersion()
    {
        return mythVersion;
    }

    public Backend getMasterBackend()
    {
        return masterBackend;
    }

    public List<Backend> getSlaveBackends()
    {
        return new ArrayList<Backend>(slaveBackends.values());
    }

    public List<Backend> getAllBackends()
    {
        List<Backend> backends = new ArrayList<Backend>();
        backends.add(masterBackend);
        backends.addAll(slaveBackends.values());

        return backends;
    }

    public Backend getBackend(String host)
    {
        // TODO
        return null;
    }

    public List<Frontend> getAllFrontends()
    {
        return new ArrayList<Frontend>(frontends.values());
    }

    public Frontend getFrontend(String host)
    {
        return frontends.get(host);
    }
}
