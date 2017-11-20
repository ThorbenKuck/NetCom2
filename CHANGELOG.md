# V 0.4.0.0

## Changes

### ClientSendBride

#### Methods

* The Method: ClientSendBridge#send(Object) will now await the clients primed value

### Session

#### Other

* Added missing Java-Doc

### SessionImpl

#### Attributes

* The Attribute: synchronize was changed to final

#### Methods

* The Method: SessionImpl#newPrimation() will now await the synchronization of the current element.
* The Method: SessionImpl#addHeatBeat(HeartBeat<Session>) will now start the HeartBeat asynchronous (using .parallel())

### AbstractConnection

#### Other

* Rearranged Methods

### CallerReflectionTrace

#### Methods

* The Constructor now uses the new CallerTraceSystemDefaultStyleLogging for printing.

### SystemDefaultStyleLogging

#### Attributes

* The Lock has been removed, because it was not needed

#### Methods

* All lock calls have been removed, because the print is already synchronized at the println method.

### PipelineReceiverImpl

#### Other

* Renamed to PipelineReceiver, since it does not implement an interface
* Changed all Objects.requireNotNull(Object) calls to Validate.parameterNotNull(Object)
  * Since those variables are not used, but just stored, an IllegalArgumentException should be thrown instead of an NullPointerException
* Added Java-Doc
------

## Interfaces

### ClientConnectedHandler

#### Methods

* The getIdentifier Method has been removed, so that its creation is only depending on the DefaultClientConnectedHandler

### Client

#### Methods

* Added the Method getAnyConnection
    * Returns a random Connection
* Added the Method getFormattedAddress
    * Returns getAnyConnection().getFormattedAddress
* Added the Method addFallBackSerializationAdapter
    * To be used instead of setFallBackSerializationAdapter
* Added the Method addFallBackDeSerializationAdapter
    * To be used instead of setFallBackDeSerializationAdapter
* Added the Method routeConnection(Class, Class)
    * Route a connection, identified by a key to another given key.
* Added the Method routeConnection(Connection, Class)
    * Route a connection to another given key.
* DEPRECATED setFallBackSerializationAdapter = unsuited Name
* DEPRECATED setFallBackDeSerializationAdapter = unsuited Name

#### Other

* Arrangement of the Methods has been changed
* Added missing Java-Doc

### Adapter

#### Other

* Added Java-Doc
* Added the @FunctionalInterface annotation, to signal, that the interface should contain exactly 1 non-default method

### Factory

#### Other

* Added Java-Doc

### MultipleConnections

#### Other

* Added Java-Doc

### ReceivePipeline

#### Other

* Added Java-Doc

### SendBridge

#### Other

* Added Java-Doc

### SimpleFactory

#### Other

* Added Java-Doc
* Added the @FunctionalInterface annotation

### SocketFactory

#### Other

* Added Java-Doc

### SoftStoppable

#### Other

* Added Java-Doc

### TriConsumer

#### Methods

* Added the andThen(TriConsumer) Method, which is like the Consumer#andThen(Consumer)

#### Other

* Added Java-Doc

### TriPredicate

#### Methods

* Added the and Method, which is like the Predicate#and(Predicate) Method
* Added the negate Method, which is like the Predicate#negate() Method
* Added the or Method, which is like the Predicate#or(Predicate) Method

#### Other

* Added Java-Doc

------

## Annotations

### Asynchronous-Annotation

#### Other

* Added Java-Doc

### Exposed-Annotation

#### Other

* Added Java-Doc

### Experimental-Annotation

#### Other

* This annotation is now used at Methods, to notify that a method is currently Experimental

### Tested-Annotation

#### Methods

* added the responsibleTest method, to show of, what test is testing this annotated Class
  * This Method takes a String, since the Test-Sources are not available at the source set
* added the uniteTest method, which describes whether or not the responsible Test is a Unit Test

#### Other

* Added Java-Doc

### ReceiveHandler-Annotation

#### Other

* Changed the Java-Doc

## Additions

### ExposedAnnotationProcessor

* This class has been added

* It is an AnnotationProcessor, which might be used to process the Exposed Annotation

### CallerTraceSystemDefaultStyleLogging

* This class has been added
* It describes how the CallerReflectionLogging should look

### Validate

* This class has been added
* It provides methods, to throw IllegalArgumentExceptions, if any specific parameter is null