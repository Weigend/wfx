#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\' )
package ${package}.application;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.platform.api.events.ProgressEvent;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ${rootArtifactId}Module implements Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(${rootArtifactId}Module.class);

    @Override
    public String getName() {
        return "${groupId}:${artifactId}";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void preload() {
        try {
            EventBus eventBus = Lookup.lookup(EventBus.class);
            eventBus.publish(new ProgressEvent("Startup Views", 0, this));

            FXMLView view = new FXMLView("${package}.editors:1", "Example Editor", Position.CENTER,
                    "${packageInPathFormat}/editors/editor.fxml", getClass().getClassLoader());
            eventBus.publish(new ProgressEvent("Finished loading view 1", 0.25, this));

            FXMLView view1 = new FXMLView("${package}.editors:2", "Example Editor", Position.CENTER,
                    "${packageInPathFormat}/editors/editor.fxml", getClass().getClassLoader());
            eventBus.publish(new ProgressEvent("Finished loading view 2", 0.5, this));

            FXMLView view2 = new FXMLView("${package}.explorer:1", "Example explorer", Position.LEFT,
                    "${packageInPathFormat}/explorer/explorer.fxml", 0.25, getClass().getClassLoader());
            eventBus.publish(new ProgressEvent("Finished loading view 3", 0.75, this));

            WindowManager windowManager = Lookup.lookup(WindowManager.class);
            windowManager.register(view);
            windowManager.register(view1);
            windowManager.register(view2);
            eventBus.publish(new ProgressEvent("Finished loading all views", 1, this));
        }
        catch (IOException e) {
            LOGGER.error("Can not start module", e);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
