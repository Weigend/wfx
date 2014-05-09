// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 03.04.14 16:44
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub for test of {@link AbstractBundleResolverMojo}
 */
class BundleResolverTestMojo extends AbstractBundleResolverMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }

    @Override
    public Map getPluginContext() {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
        HashMap<String, Object> context = new HashMap<>();
        context.put("pluginDescriptor", pluginDescriptor);
        pluginDescriptor.setVersion("1.0.0");
        return context;
    }
}
