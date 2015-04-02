// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 24.07.14 17:18
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.archetype.quickstart;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test for the quickstart archetype
 *
 * @author christian.fritz
 */
public class GenerateArchetypeTest {
    public static final File ROOT = new File("target/test-classes/");
    private static final String ARCHETYPE_GROUP_ID = "de.qaware.stagediver.fx";
    private static final String ARCHETYPE_ARTEFACT_ID = "stagediverfx-archetype-quickstart";
    private static final String TEST_GROUP_ID = "de.qaware.sdfx";
    private static final String TEST_ARTIFACT_ID = "archetypeTest";
    private static final String TEST_VERSION = getVersion();
    public static final String ARCHETYPE_CATALOG = "local";

    @Before
    public void setUp() throws VerificationException, IOException {
        Verifier verifier;
        /*
         * We must first make sure that any artifact created
         * by this test has been removed from the local
         * repository. Failing to do this could cause
         * unstable test results. Fortunately, the verifier
         * makes it easy to do this.
         */
        verifier = new Verifier(ROOT.getAbsolutePath());
        // Deleting a former created artefact from the archetype to be tested
        verifier.deleteArtifact(TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION, null);

        // Delete the created maven project
        verifier.deleteDirectory(TEST_ARTIFACT_ID);
    }

    @Test
    @Ignore
    public void testGenerateAndBuildArchetypeArtefact() throws VerificationException {
        Verifier verifier = new Verifier(ROOT.getAbsolutePath());
        verifier.setSystemProperties(getSystemProperties());
        verifier.setAutoclean(false);
        verifier.executeGoal("archetype:generate");
        verifier.verifyErrorFreeLog();

        verifier = new Verifier(ROOT.getAbsolutePath() + "/" + TEST_ARTIFACT_ID);
        verifier.setAutoclean(true);
        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("Building zip");
    }

    private static Properties getSystemProperties() {
        Properties props = new Properties(System.getProperties());
        props.put("archetypeGroupId", ARCHETYPE_GROUP_ID);
        props.put("archetypeArtifactId", ARCHETYPE_ARTEFACT_ID);
        props.put("archetypeVersion", getVersion());
        props.put("groupId", TEST_GROUP_ID);
        props.put("artifactId", TEST_ARTIFACT_ID);
        props.put("version", TEST_VERSION);
        props.put("archetypeCatalog", ARCHETYPE_CATALOG);
        props.put("interactiveMode", "false");

        return props;
    }

    private static String getVersion() {
        String path = "/version.properties";
        InputStream stream = GenerateArchetypeTest.class.getResourceAsStream(path);
        if (stream == null) {
            return "UNKNOWN";
        }
        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        }
        catch (IOException e) {
            return "UNKNOWN";
        }
    }
}
