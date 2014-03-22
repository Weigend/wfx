//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: stagediverfx-maven-plugin
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 22.03.14 11:30
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin.resolver;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.artifact.versioning.VersionRange;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;

public class Artifact {

    private final String groupId;

    private final String artifactId;

    private final String type;

    private final String classifier;

    private final String scope;

    private volatile File file;

    private final VersionRange versionRange;

    public Artifact(String groupId, String artifactId, String version, File file) {
        this(groupId, artifactId, VersionRange.createFromVersion(version), "", "", "", file);
    }

    public Artifact(String groupId, String artifactId, String version, String classifier, File file) {
        this(groupId, artifactId, VersionRange.createFromVersion(version), "", "", classifier, file);
    }

    public Artifact(String groupId, String artifactId, VersionRange versionRange, String scope, String type, String classifier, File file) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionRange = versionRange;
        this.scope = scope;
        this.type = type;
        this.classifier = classifier;
        this.file = file;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getType() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getScope() {
        return scope;
    }

    public File getFile() {
        return file;
    }

    public VersionRange getVersionRange() {
        return versionRange;
    }

    public org.sonatype.aether.artifact.Artifact asAetherArtifact() {
        return new DefaultArtifact(
                getGroupId(),
                getArtifactId(),
                getClassifier(),
                "",
                getVersionRange().getRecommendedVersion().getQualifier(),
                null,
                getFile()
        );
    }

    public org.apache.maven.artifact.Artifact asMavenArtifact() {
        return new org.apache.maven.artifact.DefaultArtifact(
                getGroupId(),
                getArtifactId(),
                getVersionRange(),
                getScope(),
                getType(),
                getClassifier(),
                null
        );
    }

    public static Artifact fromArtifact(org.sonatype.aether.artifact.Artifact artifact) {
        return new Artifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                artifact.getClassifier(),
                artifact.getFile()
        );
    }

    public static Artifact fromArtifact(org.apache.maven.artifact.Artifact artifact) {
        return new Artifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                artifact.getClassifier(),
                artifact.getFile()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Artifact rhs = (Artifact) obj;
        return new EqualsBuilder()
                .append(this.groupId, rhs.groupId)
                .append(this.artifactId, rhs.artifactId)
                .append(this.type, rhs.type)
                .append(this.classifier, rhs.classifier)
                .append(this.scope, rhs.scope)
                .append(this.file, rhs.file)
                .append(this.versionRange, rhs.versionRange)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupId)
                .append(artifactId)
                .append(type)
                .append(classifier)
                .append(scope)
                .append(file)
                .append(versionRange)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("artifactId", artifactId)
                .append("type", type)
                .append("classifier", classifier)
                .append("scope", scope)
                .append("file", file)
                .append("versionRange", versionRange)
                .toString();
    }


}
