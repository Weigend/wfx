# wfx CDI Support
This package provides some useful additional features when using wfx with CDI.
## ```FXMLLoaderProducer```
The ```FXMLLoaderProducer``` allows to use dependency injection in controller classes of javafx
created by ```fx:controller="CLASSNAME"``` or by using the class name as tag within an fxml.

To use the ```FXMLLoaderProducer``` just include the ```cdi-contexts``` jar into the classpath.
## Implementing an own scope based on ```JfxContext```
Implementing an own scope based on ```JfxContext``` quite easy. But it needs some boilerplate code.

There are five parts to implement an own scope.

 1. The scope annotation. Example: [ViewScoped](./src/main/java/de/qaware/sdfx/extensions/cdi/contexts/api/ViewScoped.java)
 2. The context interface. Example: [ViewContext](./src/main/java/de/qaware/sdfx/extensions/cdi/contexts/api/ViewContext.java)
 3. The context implementation. Example: [ViewContextImpl](./src/main/java/de/qaware/sdfx/extensions/cdi/contexts/view/ViewContextImpl.java)
 4. Register the context within the bean manager. Example: [ViewContextExtension](./src/main/java/de/qaware/sdfx/extensions/cdi/contexts/view/ViewContextExtension.java)
 5. Register your context extension. Example: [Registration](./src/main/resources/META-INF/services/jakarta.enterprise.inject.spi.Extension)

