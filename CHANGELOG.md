V 0.4.0.0

## ClientConnectedHandler

### Methods

* The getIdentifier Method has been removed, so that its creation is only depending on the DefaultClientConnectedHandler

## ClientSendBride

### Methods

* The Method: ClientSendBridge#send(Object) will now await the clients primed value

## Session

### Other

* Added missing Java-Doc

## SessionImpl

### Attributes

* The Attribute: synchronize was changed to final

### Methods

* The Method: SessionImpl#newPrimation() will now await the synchronization of the current element.
* The Method: SessionImpl#addHeatBeat(HeartBeat<Session>) will now start the HeartBeat asynchronous (using .parallel())

## Client

### Methods

* Added the Method getAnyConnection
    * Returns a random Connection
* Added the Method getFormattedAddress
    * Returns getAnyConnection().getFormattedAddress
* Added the Method addFallBackSerializationAdapter
    * To be used instead of setFallBackSerializationAdapter
* Added the Method addFallBackDeSerializationAdapter
    * To be used instead of setFallBackDeSerializationAdapter
* DEPRECATED setFallBackSerializationAdapter = unsuited Name
* DEPRECATED setFallBackDeSerializationAdapter = unsuited Name

### Other

* Arrangement of the Methods has been changed
* Added missing Java-Doc

## AbstractConnection

### Methods

### Other

* Rearranged Methods

## CallerTraceSystemDefaultStyleLogging

* This class has been added
* It describes how the CallerReflectionLogging should look

## CallerReflectionTrace

### Methods

* The Constructor now uses the new CallerTraceSystemDefaultStyleLogging for printing.

## SystemDefaultStyleLogging

### Attributes

* The Lock has been removed, because it was not needed

### Methods

* All lock calls have been removed, because the print is already synchronized at the println method.