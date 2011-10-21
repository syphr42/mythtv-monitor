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
package org.syphr.mythtv.monitor.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.JobExecutionContext;
import org.syphr.mythtv.api.Backend;
import org.syphr.mythtv.db.DatabaseException;
import org.syphr.mythtv.monitor.Report;
import org.syphr.mythtv.monitor.config.BackendHost;
import org.syphr.mythtv.monitor.config.MonitorConfigException;
import org.syphr.mythtv.monitor.config.MythTvEnvironment;
import org.syphr.mythtv.protocol.ConnectionType;
import org.syphr.mythtv.protocol.EventLevel;
import org.syphr.mythtv.util.exception.CommandException;

public class Alive implements MythJob
{
    private MythTvEnvironment env;

    @Override
    public void execute(JobExecutionContext context)
    {
        context.setResult(perform());
    }

    @Override
    public void configure(MythTvEnvironment environment) throws MonitorConfigException
    {
        env = environment;
    }

    public Report perform()
    {
        Map<String, String> downBackends = new HashMap<String, String>();
        Map<String, String> downFrontends = new HashMap<String, String>();

        for (BackendHost host : env.getAllBackends())
        {
            try
            {
                Backend backend = host.connect(ConnectionType.MONITOR, EventLevel.NONE);
                backend.disconnect();
            }
            catch (IOException e)
            {
                downBackends.put(host.getHost(), e.getMessage());
            }
            catch (CommandException e)
            {
                downBackends.put(host.getHost(), e.getMessage());
            }
            catch (DatabaseException e)
            {
                downBackends.put(host.getHost(), e.getMessage());
            }
        }

        // TODO frontends

        if (downBackends.isEmpty() && downFrontends.isEmpty())
        {
            return null;
        }

        final StringBuilder builder = new StringBuilder();

        if (!downBackends.isEmpty())
        {
            builder.append("Backends:\n");
            for (Entry<String, String> entry : downBackends.entrySet())
            {
                builder.append('\t').append(entry.getKey()).append(" => ").append(entry.getValue()).append('\n');
            }
            builder.append("\n\n");
        }

        if (!downFrontends.isEmpty())
        {
            builder.append("Frontends:\n");
            for (Entry<String, String> entry : downFrontends.entrySet())
            {
                builder.append('\t').append(entry.getKey()).append(" => ").append(entry.getValue()).append('\n');
            }
        }

        return new Report()
        {
            @Override
            public String getTitle()
            {
                return "MythTV Host Errors";
            }

            @Override
            public String getBody()
            {
                return builder.toString();
            }
        };
    }
}
