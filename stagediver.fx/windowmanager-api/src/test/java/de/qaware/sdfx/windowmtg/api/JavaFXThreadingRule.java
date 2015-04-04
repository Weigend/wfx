/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.qaware.sdfx.windowmtg.api;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javafx.stage.*;

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
