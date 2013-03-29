/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.ConfigSourceFactory;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.IElementDef;
import org.webharvest.exception.PluginException;
import org.webharvest.gui.Ide;
import org.webharvest.ioc.DebugFileLogger;
import org.webharvest.ioc.HttpModule;
import org.webharvest.ioc.ScraperModule;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.database.DefaultDriverManager;
import org.webharvest.runtime.database.DriverManager;
import org.webharvest.runtime.web.HttpClientManager.ProxySettings;
import org.webharvest.utils.CommonUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Startup class  for Web-Harvest.
 */
public class CommandLine {

    private static DefinitionResolver definitionResolver =
        DefinitionResolver.INSTANCE;

    private static Map<String, String> getArgValue(String[] args, boolean caseSensitive) {
        Map<String, String> params = new HashMap<String, String>();
        for (String curr : args) {
            String argName = caseSensitive ? curr : curr.toLowerCase();
            String argValue = "";

            int eqIndex = curr.indexOf('=');
            if (eqIndex >= 0) {
                argName = curr.substring(0, eqIndex).trim();
                argValue = curr.substring(eqIndex + 1).trim();
            }

            params.put(caseSensitive ? argName : argName.toLowerCase(), argValue);
        }

        return params;
    }

    private static Map<String, String> getArgValue(String[] args) {
        return getArgValue(args, false);
    }

    public static void main(final String[] args) throws IOException {
        final Map<String, String> params = getArgValue(args);

        if (params.size() == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new Ide().createAndShowGUI();
                }
            });
        } else if (params.containsKey("-h") || params.containsKey("/h")) {
            printHelp();
            System.exit(0);
        } else {
            String configFilePath = params.get("config");
            if (configFilePath == null || "".equals(configFilePath)) {
                System.err.println("You must specify configuration file path using config=<path> argument!");
                printHelp();
                System.exit(1);
            }

            String workingDir = params.get("workdir");
            if (workingDir == null || "".equals(workingDir)) {
                workingDir = ".";
            }

            parseLoggingSettings(params);

            final ProxySettings proxySettings = parseProxySettings(params);

            final Injector injector = Guice.createInjector(
                    new ScraperModule(workingDir),
                    new HttpModule(proxySettings));

            parseDebugModeSettings(params, workingDir);

            // register plugins if specified
            String pluginsString = params.get("plugins");
            if (!CommonUtil.isEmpty(pluginsString)) {
                for (String pluginAndUri : CommonUtil.tokenize(pluginsString, ",")) {
                    try {
                        final String pluginClass = StringUtils.substringBefore(pluginAndUri, ":");
                        final String pluginUri = StringUtils.substringAfter(pluginAndUri, ":");
                        definitionResolver.registerPlugin(pluginClass, pluginUri);
                    } catch (PluginException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            parseDatabaseDrivers(params);

            final String configLowercase = configFilePath.toLowerCase();

            final HarvestLoadCallback callback = new HarvestLoadCallback() {
                @Override
                public void onSuccess(final List<IElementDef> elements) {
                    // TODO Auto-generated method stub

                }
            };

            final ConfigSourceFactory configSourceFactory =
                    injector.getInstance(ConfigSourceFactory.class);
            final Harvest harvest = injector.getInstance(Harvest.class);
            final ConfigSource configSource = (configLowercase.startsWith("http://") || configLowercase.startsWith("https://"))
                    ? configSourceFactory.create(new URL(configFilePath))
                    : configSourceFactory.create(new File(configFilePath));
            // FIXME rbala although temporary solution it is duplicated (ConfigPanel)
            final Harvester harvester = harvest.getHarvester(configSource, callback);

            harvester.execute(new Harvester.ContextInitCallback() {
                @Override
                public void onSuccess(DynamicScopeContext context) {
                    // adds initial variables to the scraper's content, if any
                    final Map<String, String> vars =
                        getArgValue(getVariables(params), true);
                    for (Map.Entry<String, String> var : vars.entrySet()) {
                        final String varName = var.getKey();
                        if (varName.length() > 0) {
                            context.setLocalVar(varName, var.getValue());
                        }
                    }
                }

            });

        }
    }

    private static void parseDebugModeSettings(final Map<String, String> params,
            final String workingDir) throws IOException {
        if (CommonUtil.isBooleanTrue(params.get("debug"))) {
            final Logger logger = LogManager.getLogger(DebugFileLogger.NAME);
            logger.setLevel(Level.TRACE);
            logger.addAppender(new FileAppender(
                    new PatternLayout(DebugFileLogger.LAYOUT),
                    new File(workingDir, "_DEBUG").getPath(),
                    false));
        }
    }

    private static void parseLoggingSettings(final Map<String, String> params) {
        final String logPropsFile = params.get("logpropsfile");
        final String logLevel = params.get("loglevel");
        if (logPropsFile != null && !"".equals(logPropsFile)) {
            PropertyConfigurator.configure(logPropsFile);
        } else if (logLevel != null && !"".equals(logLevel)) {
            LogManager.getRootLogger().setLevel(Level.toLevel(logLevel));
        }
    }

    private static ProxySettings parseProxySettings(
            final Map<String, String> params) {
         final String proxyHost = params.get("proxyhost");
         if (proxyHost == null || "".equals(proxyHost)) {
             return ProxySettings.NO_PROXY_SET;
         }

         final ProxySettings.Builder proxySettingsBuilder =
             new ProxySettings.Builder(proxyHost);

         String proxyPort = params.get("proxyport");
         if (proxyPort != null && !"".equals(proxyPort)) {
             proxySettingsBuilder.setProxyPort(
                     Integer.parseInt(proxyPort));
         }

         String proxyUser = params.get("proxyuser");
         if (proxyUser != null && !"".equals(proxyUser)) {

             proxySettingsBuilder.setProxyCredentialsUsername(proxyUser);
             proxySettingsBuilder.setProxyCredentialsPassword(
                     params.get("proxypassword"));
             proxySettingsBuilder.setProxyCredentialsNTHost(
                     params.get("proxynthost"));
             proxySettingsBuilder.setProxyCredentialsNTDomain(
                     params.get("proxyntdomain"));
         }

         return proxySettingsBuilder.build();
    }

    private static void parseDatabaseDrivers(final Map<String, String> params) {
        final String drivers = params.get("dbdrivers");
        final DriverManager driverManager = DefaultDriverManager.INSTANCE;

        if (!CommonUtil.isEmpty(drivers)) {
            for (String driverLocation : CommonUtil.tokenize(drivers, ",")) {
                driverManager.addDriverResource(
                        new File(driverLocation).toURI());
            }
        }
    }

    /**
     * Returns {@link String} array with value of 'vars' parameter if it exists,
     * otherwise it returns empty array. The value of 'vars' parameter is split
     * using ';' delimiter.
     *
     * @param params
     *            parameters specified in the command line
     * @return array of configuration's variables
     */
    private static String[] getVariables(final Map<String, String> params) {
        final String vars = params.get("vars");
        if (vars != null) {
            return vars.split(";");
        }
        return new String[]{};
    }

    private static void printHelp() {
        System.out.println("");
        System.out.println("To open Web-Harvest GUI:");
        System.out.println("   java -jar webharvestXX.jar");
        System.out.println("or just double-click webharvestXX.jar from the file manager.");
        System.out.println("");
        System.out.println("Command line use:");
        System.out.println("   java -jar webharvestXX.jar [-h] config=<path> [workdir=<path>] [debug=yes|no]");
        System.out.println("             [proxyhost=<proxy server> [proxyport=<proxy server port>]]");
        System.out.println("             [proxyuser=<proxy username> [proxypassword=<proxy password>]]");
        System.out.println("             [proxynthost=<NT host name>]");
        System.out.println("             [proxyntdomain=<NT domain name>]");
        System.out.println("             [loglevel=<level>]");
        System.out.println("             [logpropsfile=<path>]");
        System.out.println("             [plugins=<plugin-class1>[:<uri1>][,<plugin-class2>[:<uri2>]]...]");
        System.out.println("             [dbdrivers=<jar-uri1>[,<jar-uri2>]...]");
        System.out.println("             [vars=<name1>=<value1>[;<name2>=<value2>...]]");
        System.out.println("");
        System.out.println("   -h            - shows this help.");
        System.out.println("   config        - path or URL of configuration (URL must begin with \"http://\" or \"https://\").");
        System.out.println("   workdir       - path of the working directory (default is current directory).");
        System.out.println("   debug         - specify if Web-Harvest generates debugging output (default is no).");
        System.out.println("   proxyhost     - specify proxy server.");
        System.out.println("   proxyport     - specify port for proxy server.");
        System.out.println("   proxyuser     - specify proxy server username.");
        System.out.println("   proxypassword - specify proxy server password.");
        System.out.println("   proxynthost   - NTLM authentication scheme - the host the request is originating from.");
        System.out.println("   proxyntdomain - NTLM authentication scheme - the domain to authenticate within.");
        System.out.println("   loglevel      - specify level of logging for Log4J (trace,info,debug,warn,error,fatal).");
        System.out.println("   logpropsfile  - file path to custom Log4J properties. If specified, loglevel is ignored.");
        System.out.println("   plugins       - comma-separated list of pairs <plugin-class>[:<uri>], where <plugin-class> is full plugin class name," +
                "and URI is an XML namespace in which this plugin is declared. If URI is not specified the default WebHarvest schema is assumed.");
        System.out.println("   dbdrivers     - comma-separated list of JAR locations containing database drivers");
        System.out.println("   vars	         - semicolon-separated list of pairs <name>=<value>, where <name> is a name of initial variable " +
                "of the Web-Harvest context and <value> is its value");
    }

}