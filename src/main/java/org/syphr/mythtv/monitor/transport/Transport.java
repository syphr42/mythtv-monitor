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
package org.syphr.mythtv.monitor.transport;

import org.syphr.mythtv.monitor.Report;
import org.syphr.mythtv.monitor.ReportException;
import org.syphr.mythtv.monitor.config.MonitorConfigException;
import org.syphr.mythtv.monitor.config.MythTvEnvironment;
import org.syphr.mythtv.monitor.xsd.config.x10.TransportType;

public interface Transport
{
    public void configure(MythTvEnvironment environment, TransportType config) throws MonitorConfigException;

    public void deliver(Report report) throws ReportException;
}
