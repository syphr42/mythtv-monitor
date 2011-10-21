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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.syphr.mythtv.monitor.job.MythJob;
import org.syphr.mythtv.monitor.transport.Transport;
import org.syphr.mythtv.monitor.xsd.config.x10.ConfigDocument;
import org.syphr.mythtv.monitor.xsd.config.x10.ConfigType;
import org.syphr.mythtv.monitor.xsd.config.x10.TransportType;

public class MonitorConfig
{
    private static final File DEFAULT_CONFIG_DIR = new File(System.getProperty("user.home"),
                                                            ".mythtv-monitor");

    private static final String DEFAULT_CONFIG_FILE_NAME = "config.xml";

    private static final String DEFAULT_JOBS_FILE_NAME = "jobs.xml";

    private static final String DEFAULT_PLUGINS_DIR_NAME = "plugins";

    private MythTvEnvironment environment;
    private List<Transport> transports;
    private File jobsFile;

    public void load() throws MonitorConfigException, IOException
    {
        load(DEFAULT_CONFIG_DIR);
    }

    public void load(File configDir) throws MonitorConfigException, IOException
    {
        load(new File(configDir, DEFAULT_CONFIG_FILE_NAME),
             new File(configDir, DEFAULT_JOBS_FILE_NAME),
             new File(configDir, DEFAULT_PLUGINS_DIR_NAME));
    }

    public void load(File configFile, File jobsFile, File pluginsDir) throws MonitorConfigException,
                                                                     IOException
    {
        this.jobsFile = jobsFile;

        ClassLoader loader = new URLClassLoader(getUrls(pluginsDir), getClass().getClassLoader());

        try
        {
            ConfigDocument doc = ConfigDocument.Factory.parse(configFile);
            processConfig(doc.getConfig(), loader);
        }
        catch (XmlException e)
        {
            throw new IOException("Unable to parse configuration file at \""
                    + configFile.getAbsolutePath()
                    + "\"", e);
        }
    }

    private URL[] getUrls(File directory) throws MalformedURLException
    {
        List<URL> urls = new ArrayList<URL>();

        if (directory.isDirectory())
        {
            for (File file : directory.listFiles())
            {
                urls.add(file.toURI().toURL());
            }
        }

        return urls.toArray(new URL[urls.size()]);
    }

    private void processConfig(ConfigType config, ClassLoader loader) throws MonitorConfigException
    {
        environment = new MythTvEnvironment(config.getMythtv());

        transports = new ArrayList<Transport>();
        for (TransportType transportConfig : config.getTransports().getTransportArray())
        {
            Transport transport = loadClass(Transport.class, transportConfig.getType(), loader);
            transport.configure(environment, transportConfig);

            transports.add(transport);
        }
    }

    public MythJob loadJob(String name) throws MonitorConfigException
    {
        // TODO
        return loadClass(MythJob.class, name, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T loadClass(Class<T> type, String name, ClassLoader loader) throws MonitorConfigException
    {
        try
        {
            Class<?> loaded = loader.loadClass(name);

            if (!type.isAssignableFrom(loaded))
            {
                throw new MonitorConfigException("Incorrect type specified: expected sub-type of "
                        + type.getName()
                        + "; found "
                        + name);
            }

            Class<T> namedClass = (Class<T>)loaded;
            return namedClass.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new MonitorConfigException("Unable to find class " + name, e);
        }
        catch (InstantiationException e)
        {
            throw new MonitorConfigException("Unable to construct a no-arg instance of " + name, e);
        }
        catch (IllegalAccessException e)
        {
            throw new MonitorConfigException("Access not allowed for " + name, e);
        }
    }

    public MythTvEnvironment getEnvironment()
    {
        return environment;
    }

    public List<Transport> getTransports()
    {
        return Collections.unmodifiableList(transports);
    }

    public File getJobsFile()
    {
        return jobsFile;
    }
}
