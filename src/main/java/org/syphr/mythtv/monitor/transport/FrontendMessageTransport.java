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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.syphr.mythtv.api.frontend.Frontend;
import org.syphr.mythtv.monitor.Report;
import org.syphr.mythtv.monitor.ReportException;
import org.syphr.mythtv.monitor.config.MonitorConfigException;
import org.syphr.mythtv.monitor.config.MythTvEnvironment;
import org.syphr.mythtv.monitor.xsd.config.x10.TransportType;
import org.syphr.mythtv.monitor.xsd.femessage.x10.OptionsDocument;
import org.syphr.mythtv.monitor.xsd.femessage.x10.OptionsType;

public class FrontendMessageTransport implements Transport
{
    private List<Frontend> frontends;

    @Override
    public void configure(MythTvEnvironment environment, TransportType config) throws MonitorConfigException
    {
        frontends = new ArrayList<Frontend>();

        try
        {
            XmlCursor cursor = config.getConfiguration().newCursor();
            cursor.toFirstChild();

            OptionsDocument doc = OptionsDocument.Factory.parse(cursor.getDomNode());
            OptionsType options = doc.getOptions();

            String[] frontendHosts = options.getFrontendArray();
            if (frontendHosts != null)
            {
                for (String frontendHost : frontendHosts)
                {
                    frontends.add(environment.getFrontend(frontendHost));
                }
            }
        }
        catch (XmlException e)
        {
            throw new MonitorConfigException(e);
        }

        while (frontends.remove(null))
        {
            /*
             * Clear any null values.
             */
        }

        /*
         * If no frontends were specified, add use all available frontends.
         */
        if (frontends.isEmpty())
        {
            frontends.addAll(environment.getAllFrontends());
        }
    }

    @Override
    public void deliver(Report report) throws ReportException
    {
        for (Frontend frontend : frontends)
        {
            try
            {
                frontend.sendMessage(report.getBody());
            }
            catch (IOException e)
            {
                throw new ReportException("Failed to send report to frontend at "
                        + frontend.getHost(), e);
            }
        }
    }
}
