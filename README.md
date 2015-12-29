# Sandbox Experiment

This is an implementation of sandboxed code using the Java SecurityManager, written in Scala.

It takes inspiration from Jens Nordahl's [Sandboxing plugins in Java](http://www.jayway.com/2014/06/13/sandboxing-plugins-in-java/), although it took some poking to see what the prarameters are.

It consists of a Main class that starts up a sandbox, then starts a script from within the sandbox.

## Running

Install SBT ("brew install sbt" or similar package manager).  Then type `sbt run`.  If all goes well, the script should execute.

You may need to change `testscript.sh` to be executable.

## How it works

The Java SecurityManager / architecture can prevent sensitive operations (like file execution) to sandboxed code, even if it's a server side application.

The SandboxPolicy class is where all the magic happens.  Specifically, this line:

```
permissions.add(new java.io.FilePermission(scriptName, "execute"))
```

If you comment out that line and recompile (or just run `sbt run` again), then you'll see errors.

## DoPrivilegedAction

Because Scala has closures and implicits, we can do fun things like: 

```
implicit val context = AccessController.getContext

private def createSandboxClassLoader: SandboxClassLoader = {
  DoPrivilegedAction(new RuntimePermission("createClassLoader")) {
    new SandboxClassLoader(this.getClass.getClassLoader)
  }
} // context is passed in automatically
```

There's much more that can be done in that area: for example, having typed permissions instead having everything be RuntimePermission.

## Logging

There's also a complete set of logs using Logback, containing all the security information in the policy, in JSON format, using the SLF4J marker API.

You don't need all the logging information, but it does show how you'd log information (especially if someone is trying an exploit).

## This Looks Complicated

It's not as bad as it looks, but it is not well laid out for application developers.  This system was originally designed for applets, so it makes sense that it assumes discrete packaging.  The tough bit is adding all the permissions to the sandbox.

If you have an existing application and you just want to blacklist some operations, you can use [Pro-Grade](http://pro-grade.sourceforge.net/) as an [easy way to secure applications])(https://tersesystems.com/2015/12/22/an-easy-way-to-secure-java-applications/).

## Why do it this way?

Because this is the defined way to do it.  Per [Evaluating the Flexibility of the Java Sandbox](https://www.cs.cmu.edu/~clegoues/docs/coker15acsac.pdf):

> Java provides an “orthodox” mechanism to achieve this goal while aligning with intended sandbox usage: a custom class loader that loads untrusted classes into a constrained protection domain. This approach is more clearly correct and enables a self-protecting sandbox.

## Limitations and Warnings

Note that the security policy can only distinguish by code location (packaging) and by class loader (which means URLClassLoader, which also means packaging).  This means that you probably need to package sandbox code in a different jar -- I have been completely unable to effectively sandbox code that was in the same package. 

There are notes about the ProtectionDomain in the Java Security book that suggest that packaging sandboxed code together with the Main, "AllPermissions" class is a Bad Idea and will compromise the system, so this is probably for the best.
  
## Restricting Classes

Because the policy only looks at location and classloader, if you want to forbid a specific class, like java.io.ObjectInputStream, then you have to do that from inside the classloader.

```
class SandboxClassLoader(parent:ClassLoader) extends URLClassLoader(sandboxCodeLocation, parent) {

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    if ("java.io.ObjectInputStream".equals(name)) {
      throw new IllegalArgumentException("This functionality is disabled")
    }
    super.loadClass(name, resolve)
  }

}
```

