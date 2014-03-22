//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: stagediverfx-maven-plugin
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 22.03.14 14:18
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin.resolver;

import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.Test;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the artifact transfer object.
 *
 * @author christian.fritz
 */
public class ArtifactTest {

    private Artifact sdfxArtifact = new Artifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), "", "jar", "jar", null);
    private org.sonatype.aether.artifact.Artifact aetherArtifact = new DefaultArtifact("group:artifact::jar:1.0.0");
    private org.apache.maven.artifact.Artifact mavenArtifact = new org.apache.maven.artifact.DefaultArtifact("group", "artifact", VersionRange.createFromVersion("1.0.0"), "", "jar", "jar", null);

    @Test
    public void testAsAetherArtifact() throws Exception {
        assertThat(sdfxArtifact.asAetherArtifact(), is(equalTo(aetherArtifact)));
    }

    @Test
    public void testAsMavenArtifact() throws Exception {
        assertThat(sdfxArtifact.asMavenArtifact(), is(equalTo(mavenArtifact)));
    }

    @Test
    public void testFromMavenArtifact() throws Exception {
        assertThat(Artifact.fromArtifact(mavenArtifact), is(equalTo(sdfxArtifact)));
    }

    @Test
    public void testFromAetherArtifact() throws Exception {
        assertThat(Artifact.fromArtifact(aetherArtifact), is(equalTo(sdfxArtifact)));
    }
}
