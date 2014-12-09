# stagediver.fx - A lightweight RCP for JavaFX
stagediver.fx is a lightweight Rich Client Platfrom for JavaFX. It is based on [OSGi](http://www.osgi.org) and uses [Apache Felix](http://felix.apache.org/).

## Introduction

The stagediver.fx platform provides the following features:

- Window Management with
 - Free layouting at runtime by the end user
 - Multiple window support
 - Editor area
 - Minimal impact to existing JavaFX-based components
- Module System
 - Container startup
 - OSGi-less startup  
 In this case the OSGi Service-Registry will be replaced by a
 [Google Guice](https://code.google.com/p/google-guice/) registry implementation.
- Packaging support
- Maven Archetype for rapid project starting

## Create a new Project with `mvn archetype:generate`

You can create a new stagediver.fx based project easy through the maven archetype. Because stagediver.fx is currently not integrated into common Maven repositories like Maven-Central you have first to clone and install it into the local repository. After that you can create a new project by calling:

	mvn archetype:generate                                    \
	  -DarchetypeGroupId=de.qaware.stagediver.fx              \
	  -DarchetypeArtifactId=stagediverfx-archetype-quickstart \
	  -DarchetypeVersion=0.1-SNAPSHOT  			   			  \
	  -DgroupId=YOUR_GROUPID                       		   	  \
	  -DartifactId=YOUR_ARTIFACTID			   			      \
	  -Dversion=1.0-SNAPSHOT				          		  \
	  -DinteractiveMode=false 

## First start of a project

After the creation of a new project through the archetype you can start your project the first time. To do this, you have to go to the project directory and compile the project with `mvn install`. Then you are able to start the platform and deploy your project.

The Platform provides a runner module which handles the full startup and deployment of your project bundles. The platforms main class is `de.qaware.sdfx.main.Main`.

## Maven Plugin

The stagediverfx-maven-plugin provides currently two goals:

- binary-css
- run

### Goal "binary-css"

This maven goal binds to the `process-resources` phase and compiles all cascading stylesheets (CSS) to binary style sheets (BSS). See <http://docs.oracle.com/javafx/2/deployment/javafx_ant_task_reference.htm#CIAEFCGA> for more information about the binary stylesheets.

### Goal "run"

The goal "run" is a wrapper to start a single module or the full project within the OSGi Container. Which part of the full application is started depends on the selected module from where you start the plugin. It always starts current selected module (current working directory) inclusive all dependencies and the dependencies of the stagediver.fx platform module ([de.qaware.stagediver.fx:platform-full](stagediver.fx/platform-full/pom.xml)).

**Note**: Please note that the run goal is currently not compatible with Maven 3.1.x.

## Contributing
You are highly welcome to contribute improvements, bug fixes or new features to this project.
See the stagediver.fx [Contributor Guide](https://github.com/qaware/stagediver.fx/wiki/Contributor-Guide) for more details.
