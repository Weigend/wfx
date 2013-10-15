package de.qaware.sdfx.maven.plugin;

import de.qaware.sdfx.main.AutoProcessor;
import de.qaware.sdfx.main.Main;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


@Mojo(
        name = "run",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class RunMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.artifactId}-application/target/bundles")
    protected String bundleDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            System.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, bundleDirectory);

            System.setProperty("sdfx.logger.error", String.valueOf(getLog().isErrorEnabled()));
            System.setProperty("sdfx.logger.warn", String.valueOf(getLog().isWarnEnabled()));
            System.setProperty("sdfx.logger.info", String.valueOf(getLog().isInfoEnabled()));
            System.setProperty("sdfx.logger.debug", String.valueOf(getLog().isDebugEnabled()));

            getLog().info("Set bundle directory to: " + bundleDirectory);
            System.setProperty("binary.css", "false");
            Main.main(new String[]{});
        } catch (Exception e) {
            throw new MojoExecutionException("Startup failed", e);
        }
    }
}
