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

import org.syphr.mythtv.api.MythVersion;
import org.syphr.mythtv.api.backend.Backend;
import org.syphr.mythtv.monitor.xsd.config.x10.BackendType;
import org.syphr.mythtv.protocol.ConnectionType;

public class BackendHost
{
    private final BackendType config;
    private final MythVersion version;

    public BackendHost(BackendType config, MythVersion version)
    {
        this.config = config;
        this.version = version;
    }

    public Backend getBackend(ConnectionType connectionType)
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

        Backend backend = new Backend(version);
        backend.setBackendConnectionParameters(localHost,
                                               getHost(),
                                               getProtocolPort(),
                                               connectionType,
                                               getHttpPort());

        return backend;
    }

    public String getHost()
    {
        return config.getHost();
    }

    public int getHttpPort()
    {
        return config.getHttpPort();
    }

    public int getProtocolPort()
    {
        return config.getProtocolPort();
    }

    public long getTimeout()
    {
        return config.getTimeout();
    }
}
