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
package org.syphr.mythtv.monitor.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.mythtv.monitor.Monitor;
import org.syphr.mythtv.monitor.MonitorException;

/**
 * This class provides a controllable daemon for running the monitor as a
 * service that is compatible with commons-daemon/jsvc.
 * 
 * @author Gregory P. Moyer
 */
public class Daemon
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Daemon.class);

    /**
     * The application thread
     */
    private Thread thread;

    public void init(String[] args)
    {
        final Monitor monitor = Main.buildMonitor(args);

        thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    monitor.start();
                    wait();
                }
                catch (MonitorException e)
                {
                    LOGGER.error(e.getMessage(), e);

                    System.out.println("Monitor failed to start: " + e.getMessage());
                    System.exit(1);
                }
                catch (InterruptedException e)
                {
                    LOGGER.info("Monitor shutting down");
                    monitor.stop();
                }
            }
        });
    }

    public void start()
    {
        LOGGER.info("Starting monitor");
        thread.start();
    }

    public void stop()
    {
        LOGGER.info("Stopping monitor");
        thread.interrupt();
    }

    public void destroy()
    {
        LOGGER.info("Exiting");
        System.exit(0);
    }
}
