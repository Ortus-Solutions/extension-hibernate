package ortus.extension.orm.osgi;

import java.util.HashMap;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ortus.extension.orm.ConfigurationParser;

public class ORMBundleActivator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        System.out.println("start extension bundle activator!");

        context.registerService(
            ConfigurationParser.class.getName(),
            new ConfigurationParser(),
            new Hashtable<>(new HashMap<>())
        );
    }

    public void stop(BundleContext context) throws Exception {
        //service automatically unregistered
    }
    
}