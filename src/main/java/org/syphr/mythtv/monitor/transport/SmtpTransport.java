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

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.syphr.mythtv.monitor.Report;
import org.syphr.mythtv.monitor.ReportException;
import org.syphr.mythtv.monitor.config.MonitorConfigException;
import org.syphr.mythtv.monitor.config.MythTvEnvironment;
import org.syphr.mythtv.monitor.xsd.config.x10.TransportType;
import org.syphr.mythtv.monitor.xsd.smtp.x10.OptionsDocument;
import org.syphr.mythtv.monitor.xsd.smtp.x10.OptionsType;

public class SmtpTransport implements Transport
{
    private String to;
    private String from;

    private String host;
    private String user;
    private String password;

    @Override
    public void configure(MythTvEnvironment environment, TransportType config) throws MonitorConfigException
    {
        try
        {
            XmlCursor cursor = config.getConfiguration().newCursor();
            cursor.toFirstChild();

            OptionsDocument doc = OptionsDocument.Factory.parse(cursor.getDomNode());
            OptionsType options = doc.getOptions();
            to = options.getTo();
            from = options.getFrom();
            host = options.getHost();
            user = options.getUser();
            password = options.getPassword();
        }
        catch (XmlException e)
        {
            throw new MonitorConfigException(e);
        }
    }

    @Override
    public void deliver(Report report) throws ReportException
    {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(mailProps, new ConfigAuthenticator());
        MimeMessage message = new MimeMessage(session);

        try
        {
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message.setSubject(report.getTitle());
            message.setText(report.getBody());

            javax.mail.Transport.send(message);
        }
        catch (AddressException e)
        {
            throw new ReportException("Invalid address detected while attempt to send report via SMTP",
                                      e);
        }
        catch (MessagingException e)
        {
            throw new ReportException("Failed to send report via SMTP", e);
        }
    }

    private class ConfigAuthenticator extends Authenticator
    {
        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(user, password);
        }
    }

}
