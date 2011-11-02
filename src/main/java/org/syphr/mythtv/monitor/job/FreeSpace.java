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
import java.text.NumberFormat;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.syphr.mythtv.api.backend.Backend;
import org.syphr.mythtv.data.DriveInfo;
import org.syphr.mythtv.monitor.Report;
import org.syphr.mythtv.monitor.config.BackendHost;
import org.syphr.mythtv.monitor.config.MonitorConfigException;
import org.syphr.mythtv.monitor.config.MythTvEnvironment;
import org.syphr.mythtv.protocol.ConnectionType;

public class FreeSpace implements MythJob
{
    private static final String NEWLINE = "\n";

    private List<BackendHost> hosts;

    private double warningThreshold = 0.90;
    private double criticalThreshold = 0.95;

    public double getWarningThreshold()
    {
        return warningThreshold;
    }

    public void setWarningThreshold(double warningThreshold)
    {
        this.warningThreshold = warningThreshold;
    }

    public double getCriticalThreshold()
    {
        return criticalThreshold;
    }

    public void setCriticalThreshold(double criticalThreshold)
    {
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        context.setResult(perform());
    }

    @Override
    public void configure(MythTvEnvironment environment) throws MonitorConfigException
    {
        hosts.addAll(environment.getAllBackends());
    }

    public Report perform() throws JobExecutionException
    {
        final StringBuilder reportBuilder = new StringBuilder();

        for (BackendHost host : hosts)
        {
            try
            {
                Backend backend = host.getBackend(ConnectionType.MONITOR);

                try
                {
                    List<DriveInfo> drives = backend.getInfo().getDrives();
                    for (DriveInfo drive : drives)
                    {
                        DriveAnalyzer analyzer = new DriveAnalyzer(drive);

                        if (analyzer.isWarning() || analyzer.isCritical())
                        {
                            reportBuilder.append(analyzer);
                            reportBuilder.append(NEWLINE);
                        }

                    }
                }
                finally
                {
                    backend.destroy();
                }
            }
            catch (IOException e)
            {
                throw new JobExecutionException(e);
            }
        }

        if (reportBuilder.length() == 0)
        {
            return null;
        }

        return new Report()
        {
            @Override
            public String getTitle()
            {
                return "MythTV Disk Space Alert";
            }

            @Override
            public String getBody()
            {
                return reportBuilder.toString();
            }
        };
    }

    private class DriveAnalyzer
    {
        private final double SCALE = Math.pow(1024, 2); //KB to GB

        private final String location;
        private final String usedSpace;
        private final String totalSpace;
        private final String percentage;
        private final String notice;

        private final double percentValue;

        public DriveAnalyzer(DriveInfo drive)
        {
            location = drive.getHostname() + ":" + drive.getDriveRoot();

            double used = drive.getUsedSpace() / SCALE;
            double total = drive.getTotalSpace() / SCALE;
            NumberFormat sizeFormat = NumberFormat.getNumberInstance();
            sizeFormat.setMaximumFractionDigits(2);
            sizeFormat.setGroupingUsed(false);
            usedSpace = sizeFormat.format(used);
            totalSpace = sizeFormat.format(total);

            percentValue = used / total;
            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            percentage = percentFormat.format(percentValue);

            if (isCritical())
            {
                notice = "CRITICAL";
            }
            else if (isWarning())
            {
                notice = "WARNING";
            }
            else
            {
                notice = "";
            }
        }

        public String getLocation()
        {
            return location;
        }

        public String getUsedSpace()
        {
            return usedSpace;
        }

        public String getTotalSpace()
        {
            return totalSpace;
        }

        public String getPercentage()
        {
            return percentage;
        }

        public String getNotice()
        {
            return notice;
        }

        public boolean isWarning()
        {
            return percentValue > warningThreshold && !isCritical();
        }

        public boolean isCritical()
        {
            return percentValue > criticalThreshold;
        }

        @Override
        public String toString()
        {
            return String.format("%-60s  (%-8s / %8s)GB  %-6s  %-8s",
                                 getLocation(),
                                 getUsedSpace(),
                                 getTotalSpace(),
                                 getPercentage(),
                                 getNotice());
        }
    }
}
