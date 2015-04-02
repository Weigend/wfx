# stagediver.fx - A lightweight RCP for JavaFX
stagediver.fx is a lightweight Rich Client Platfrom for JavaFX.

## Introduction

The stagediver.fx platform provides the following features:

- Window Management with
  - Free layouting at runtime by the end user
  - Multiple window support
  - Editor area
  - Minimal impact to existing JavaFX-based components
- Module System
  - Container startup
  - Packaging support
  - Maven Archetype for rapid project starting
  - Different Lookup Strategies (Currently Java Service Loader and Guice)
  
The module system did not longer depend on OSGi. OSGi brings a lot of problems while starting the JavaFX platform and makes the full platform much more complex.

## Create a new Project with `mvn archetype:generate`

You can create a new stagediver.fx based project easy through the maven archetype. Because stagediver.fx is currently not integrated into common Maven repositories like Maven-Central you have first to clone and install it into the local repository. After that you can create a new project by calling:

	mvn archetype:generate                                    \
	  -DarchetypeGroupId=de.qaware.stagediver.fx              \
	  -DarchetypeArtifactId=stagediverfx-archetype-quickstart \
	  -DarchetypeVersion=0.1        			   			  \
	  -DgroupId=YOUR_GROUPID                       		   	  \
	  -DartifactId=YOUR_ARTIFACTID			   			      \
	  -Dversion=1.0-SNAPSHOT				          		  \
	  -DinteractiveMode=false 

## First start of a project

After the creation of a new project through the archetype you can start your project the first time. To do this, you have to go to the project directory and compile the project with `mvn install`. Then you are able to start the platform and deploy your project.

The Platform provides a runner module which handles the full startup and deployment of your project bundles. The platforms main class is `de.qaware.sdfx.main.Main`.

## Maven Plugin

The stagediverfx-maven-plugin provides currently one goals:

- binary-css

### Goal "binary-css"

This maven goal binds to the `process-resources` phase and compiles all cascading stylesheets (CSS) to binary style sheets (BSS). See <http://docs.oracle.com/javafx/2/deployment/javafx_ant_task_reference.htm#CIAEFCGA> for more information about the binary stylesheets.


## Contributing
You are highly welcome to contribute improvements, bug fixes or new features to this project.
See the stagediver.fx [Contributor Guide](https://github.com/qaware/stagediver.fx/wiki/Contributor-Guide) for more details.
