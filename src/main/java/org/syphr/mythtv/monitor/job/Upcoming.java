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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.syphr.mythtv.api.Backend;
import org.syphr.mythtv.data.Program;
import org.syphr.mythtv.data.UpcomingRecordings;
import org.syphr.mythtv.db.DatabaseException;
import org.syphr.mythtv.monitor.Report;
import org.syphr.mythtv.monitor.config.BackendHost;
import org.syphr.mythtv.monitor.config.MonitorConfigException;
import org.syphr.mythtv.monitor.config.MythTvEnvironment;
import org.syphr.mythtv.protocol.ConnectionType;
import org.syphr.mythtv.protocol.EventLevel;
import org.syphr.mythtv.util.exception.CommandException;

public class Upcoming implements MythJob
{
    private static final Comparator<Program> PROGRAM_COMPARATOR = new Comparator<Program>()
    {
        @Override
        public int compare(Program p1, Program p2)
        {
            int startCompare = p1.getRecStartTs().compareTo(p2.getRecStartTs());
            return startCompare != 0 ? startCompare : p1.getRecEndTs().compareTo(p2.getRecEndTs());
        }
    };

    private BackendHost master;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        context.setResult(perform());
    }

    @Override
    public void configure(MythTvEnvironment environment) throws MonitorConfigException
    {
        master = environment.getMasterBackend();
    }

    public Report perform() throws JobExecutionException
    {
        try
        {
            Backend backend = master.connect(ConnectionType.MONITOR, EventLevel.NONE);

            try
            {
                UpcomingRecordings upcoming = backend.getUpcomingRecordings();

                List<Program> today = new ArrayList<Program>();
                List<Program> tomorrow = new ArrayList<Program>();

                for (Program program : upcoming.getPrograms())
                {
                    if (!isImportant(program))
                    {
                        continue;
                    }

                    if (isToday(program))
                    {
                        today.add(program);
                    }

                    if (isTomorrow(program))
                    {
                        tomorrow.add(program);
                    }
                }

                Collections.sort(today, PROGRAM_COMPARATOR);
                Collections.sort(tomorrow, PROGRAM_COMPARATOR);

                final StringBuilder builder = new StringBuilder();

                builder.append("Conflicts? ").append(upcoming.isConflicted()).append("\n\n");

                builder.append("Today:\n");
                for (Program program : today)
                {
                    builder.append('\t').append(format(program)).append("\n");
                }
                builder.append('\n');

                builder.append("Tomorrow:\n");
                for (Program program : tomorrow)
                {
                    builder.append('\t').append(format(program)).append("\n");
                }
                builder.append('\n');

                return new Report()
                {
                    @Override
                    public String getTitle()
                    {
                        return "MythTV - Upcoming Recordings";
                    }

                    @Override
                    public String getBody()
                    {
                        return builder.toString();
                    }
                };
            }
            finally
            {
                backend.disconnect();
            }
        }
        catch (IOException e)
        {
            throw new JobExecutionException(e);
        }
        catch (CommandException e)
        {
            throw new JobExecutionException(e);
        }
        catch (DatabaseException e)
        {
            throw new JobExecutionException(e);
        }
    }

    private boolean isImportant(Program scheduledRecording)
    {
        switch (scheduledRecording.getRecStatus())
        {
            case UNKNOWN:
            case PREVIOUS_RECORDING:
            case CURRENT_RECORDING:
            case EARLIER_SHOWING:
            case REPEAT:
            case NEVER_RECORD:
                return false;

            default:
                return true;
        }
    }

    private boolean isToday(Program scheduledRecording)
    {
        return isDay(scheduledRecording, new Date());
    }

    private boolean isTomorrow(Program scheduledRecording)
    {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        return isDay(scheduledRecording, tomorrow.getTime());
    }

    private boolean isDay(Program scheduledRecording, Date day)
    {
        Calendar test = Calendar.getInstance();
        test.setTime(day);
        int testDay = test.get(Calendar.DATE);

        Calendar recordingStart = Calendar.getInstance();
        recordingStart.setTime(scheduledRecording.getRecStartTs());
        int recordingStartDay = recordingStart.get(Calendar.DATE);

        return testDay == recordingStartDay;
    }

    private String format(Program scheduledRecording)
    {
        return String.format("%-60s%-30s%-12Tr to %-12Tr",
                             scheduledRecording.getTitle(),
                             scheduledRecording.getChannel().getName(),
                             scheduledRecording.getRecStartTs(),
                             scheduledRecording.getRecEndTs());
    }
}
