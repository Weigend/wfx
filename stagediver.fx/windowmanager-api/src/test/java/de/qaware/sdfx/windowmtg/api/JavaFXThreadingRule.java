//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-api
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 24.03.15 17:28
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import javafx.stage.Stage;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link Rule} for running tests on the JavaFX thread and performing
 * JavaFX initialisation. To include in your test case, add the following code:
 * <p>
 * <pre>
 * {@literal @}Rule
 * public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
 * </pre>
 * <p>
 * This rule is adopted from the following blog entry:
 * {@see http://andrewtill.blogspot.de/2012/10/junit-rule-for-javafx-controller-testing.html}
 *
 * @author Christian Fritz
 */
public class JavaFXThreadingRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Stage stage = GuiTestHelper.getStage();
                GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                    try {
                        if (!stage.isShowing()) {
                            stage.show();
                        }
                        statement.evaluate();
                    }
                    catch (Exception e) {
                        throw e;
                    }

                    catch (Throwable throwable) {
                        new Exception(throwable);
                    }
                });
            }
        };
    }
}
