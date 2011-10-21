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

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.PropertySettingJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.syphr.mythtv.monitor.config.MonitorConfig;
import org.syphr.mythtv.monitor.config.MonitorConfigException;

public class MythJobFactory extends PropertySettingJobFactory
{
    private final MonitorConfig config;

    public MythJobFactory(MonitorConfig config)
    {
        this.config = config;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException
    {
        Job job = super.newJob(bundle, scheduler);

        if (job instanceof MythJob)
        {
            try
            {
                ((MythJob)job).configure(config.getEnvironment());
            }
            catch (MonitorConfigException e)
            {
                throw new SchedulerException("Failed to configure MythTV job", e);
            }
        }

        return job;
    }
}
