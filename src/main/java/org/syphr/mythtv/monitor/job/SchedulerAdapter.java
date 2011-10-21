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

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public abstract class SchedulerAdapter implements SchedulerListener
{
    @Override
    public void jobScheduled(Trigger trigger)
    {
        // NOOP
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey)
    {
        // NOOP
    }

    @Override
    public void triggerFinalized(Trigger trigger)
    {
        // NOOP
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey)
    {
        // NOOP
    }

    @Override
    public void triggersPaused(String triggerGroup)
    {
        // NOOP
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey)
    {
        // NOOP
    }

    @Override
    public void triggersResumed(String triggerGroup)
    {
        // NOOP
    }

    @Override
    public void jobAdded(JobDetail jobDetail)
    {
        // NOOP
    }

    @Override
    public void jobDeleted(JobKey jobKey)
    {
        // NOOP
    }

    @Override
    public void jobPaused(JobKey jobKey)
    {
        // NOOP
    }

    @Override
    public void jobsPaused(String jobGroup)
    {
        // NOOP
    }

    @Override
    public void jobResumed(JobKey jobKey)
    {
        // NOOP
    }

    @Override
    public void jobsResumed(String jobGroup)
    {
        // NOOP
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause)
    {
        // NOOP
    }

    @Override
    public void schedulerInStandbyMode()
    {
        // NOOP
    }

    @Override
    public void schedulerStarted()
    {
        // NOOP
    }

    @Override
    public void schedulerShutdown()
    {
        // NOOP
    }

    @Override
    public void schedulerShuttingdown()
    {
        // NOOP
    }

    @Override
    public void schedulingDataCleared()
    {
        // NOOP
    }

}
