# NetCom2

NetCom2 is a framework, modular desigend to function as a universal Client-Server-Communication-Interface.

NOTE: The documentation is still in Work!

----

## For whom this is

If you are searching for a easy to use framework, that gives you the option to fastly create a Server-Client-Communication, this is for you

But do not mistake this as an simple framework! If you want, you can create your own en-/decryption mechanism. You can create customized Serialization. This framework also comes with a pre-build cache and registration-mechanism, that makes it easyer to send Object to specific clients, that want those object. This framework is completly modular, which means, you can change nearly everything, from serialization to the socket, that is being used.

----

## Getting started

Getting startet is easy. You should have basic knowledge about how a Client-Server-Architecture works. For that excample, let's assume you have 3 Project: Client, Server and Shared, while Client and Server depend on Shared.

### Creating a Server

To create a server, you simply say:

```java
ServerStart serverStart = ServerStart.of(/* your port number here */88888);
```

With that done, you have to tell the ServerStart-Object to listen to Clients

```java
serverStart.launch();    
try {    
  serverStart.acceptAllNextClients();    
} catch (ClientConnectionFailedException e) {   
  e.printStackTrace();    
  System.exit(1);    
}
```

Launch creates internal dependencies and  acceptAllNextClients(); waits for the next clients to connect.

### Creating a Client

You create a Client similiiar to a Server. You just say:

```java
ClientStart clientStart = ClientStart.of(/* address of Server */"localhost", /* port of Server*/88888);
```

Now, to connect, simply say:

```java 
clientStart.launch();
```

### Sending Stuff

Let's assume, you have an Object called Test in the Shared project, which looks like this:
```java
public class Test implements Serializable {
  private String s;
  
  public void setString(String newS) {
    this.s = newS;
  }
  
  public String getString() {
    return this.s;
  }
}
```

Now we want to send this from the Client to the Server. We realize this by saying:
```java
ClientStart clientStart = ClientStart.of(/* address of Server */"localhost", /* port of Server*/88888);
clientStart.launch();
clientStart.send().objectToServer(new Test());
```

on the ServerSide we have to say, how to handle this Object. We realize this by saying:
```java
ServerStart serverStart = ServerStart.of(88888);
serverStart.launch();

serverStart.getCommunicationRegistration().register(Test.class).addFirst((user, o) -> {
  System.out.println("received " + o.getString() + " from " + user);
  o.setString("received");
  user.send(o);
});

try {
  serverStart.acceptAllNextClients();
} catch (ClientConnectionFailedException e) {
  e.printStackTrace();
  System.exit(1);
}
```

Now, you have a simple Server, that prints out what he received to the console and sends a "received" message back.

### Where to go from here

Check out the Wiki for more informations about creating a Server and a Client, with more depth.

----

## Version:
Current Version: 0.1 (Beta)

