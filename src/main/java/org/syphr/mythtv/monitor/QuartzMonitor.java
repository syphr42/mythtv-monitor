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
package org.syphr.mythtv.monitor;

import java.util.Properties;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.plugins.xml.XMLSchedulingDataProcessorPlugin;
import org.quartz.simpl.RAMJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.mythtv.monitor.config.MonitorConfig;
import org.syphr.mythtv.monitor.job.MythJobFactory;
import org.syphr.mythtv.monitor.transport.Transport;

public class QuartzMonitor implements Monitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzMonitor.class);

    private static final Properties PROPERTIES = new Properties();
    static
    {
        PROPERTIES.setProperty("org.quartz.plugin.jobInitializer.class",
                               XMLSchedulingDataProcessorPlugin.class.getName());
        PROPERTIES.setProperty("org.quartz.plugin.jobInitializer.failOnFileNotFound",
                               Boolean.TRUE.toString());
        PROPERTIES.setProperty("org.quartz.plugin.jobInitializer.scanInterval", "600");
        PROPERTIES.setProperty("org.quartz.plugin.jobInitializer.wrapInUserTransaction",
                               Boolean.FALSE.toString());
        PROPERTIES.setProperty("org.quartz.scheduler.instanceName", "MythTVMonitorScheduler");
        PROPERTIES.setProperty("org.quartz.threadPool.threadCount", "3");
        PROPERTIES.setProperty("org.quartz.jobStore.class", RAMJobStore.class.getName());
    }

    private final MonitorConfig config;

    private final Properties properties;

    private Scheduler scheduler;

    public QuartzMonitor(MonitorConfig config)
    {
        this.config = config;

        properties = new Properties();
        properties.putAll(PROPERTIES);
        properties.put("org.quartz.plugin.jobInitializer.fileNames",
                       config.getJobsFile().getAbsolutePath());
    }

    @Override
    public void start() throws MonitorException
    {
        stop();

        try
        {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(properties);

            scheduler = factory.getScheduler();
            addListeners(scheduler.getListenerManager());
            scheduler.setJobFactory(new MythJobFactory(config));

            scheduler.start();
        }
        catch (SchedulerException e)
        {
            scheduler = null;
            throw new MonitorException("Unable to create scheduler", e);
        }

    }

    @SuppressWarnings("unchecked")
    private void addListeners(ListenerManager listenerManager)
    {
        listenerManager.addJobListener(new JobListener()
        {
            @Override
            public void jobWasExecuted(JobExecutionContext context,
                                       JobExecutionException jobException)
            {
                String jobName = context.getJobDetail().getKey().getName();

                if (jobException != null)
                {
                    LOGGER.error("Error while executing job " + jobName, jobException);
                    return;
                }

                Object result = context.getResult();
                if (result instanceof Report)
                {
                    Report report = (Report)result;
                    for (Transport transport : config.getTransports())
                    {
                        try
                        {
                            transport.deliver(report);
                        }
                        catch (ReportException e)
                        {
                            LOGGER.error("Failed to deliver report via "
                                    + transport.getClass().getSimpleName()
                                    + " for job "
                                    + jobName, e);
                        }
                    }
                }
            }

            @Override
            public void jobToBeExecuted(JobExecutionContext context)
            {
                // NOOP
            }

            @Override
            public void jobExecutionVetoed(JobExecutionContext context)
            {
                // NOOP
            }

            @Override
            public String getName()
            {
                return "Report Listener";
            }
        });
    }

    @Override
    public void stop()
    {
        if (scheduler == null)
        {
            return;
        }

        try
        {
            scheduler.shutdown();
        }
        catch (SchedulerException e)
        {
            LOGGER.error("Error occurred while shutting down scheduler", e);
        }
        finally
        {
            scheduler = null;
        }
    }

    @Override
    public void pause() throws MonitorException
    {
        if (scheduler == null)
        {
            return;
        }

        try
        {
            scheduler.pauseAll();
        }
        catch (SchedulerException e)
        {
            throw new MonitorException("Failed to pause scheduler", e);
        }
    }

    @Override
    public void resume() throws MonitorException
    {
        if (scheduler == null)
        {
            return;
        }

        try
        {
            scheduler.resumeAll();
        }
        catch (SchedulerException e)
        {
            throw new MonitorException("Failed to resume scheduler", e);
        }
    }
}
