# <img alt="Hartshorn" src="https://github.com/GuusLieben/Hartshorn/blob/develop/hartshorn-assembly/images/logo.png" height="100" /> Hartshorn Dynamic Proxies
Hartshorn Dynamic Proxies (HDP) is a Java dynamic object proxy library, extracted as standalone component from the Hartshorn framework. [Hartshorn](https://hartshorn.dockbox.org/) is a modern JVM-based full stack Java framework, which can be found at [GuusLieben/Hartshorn](https://github.com/GuusLieben/Hartshorn).

### Maven configuration 
To get started, add the Maven dependency:
```xml
<dependency>
  <groupId>org.dockbox.hartshorn</groupId>
  <artifactId>Hartshorn-Dynamic-Proxies</artifactId>
  <version>${version}</version>
</dependency>
```
Or if you are using Gradle:
```groovy
implementation "org.dockbox.hartshorn:Hartshorn-Dynamic-Proxies:$version"
```
  
## Usage
To support the creation of proxies, the `ProxyFactory` exposes a set of methods that
can be used to modify the proxy object before it is created. This includes the delegation and
interception of methods. The type of the `ProxyFactory` determines how the proxy is created, and 
how proxy objects are treated throughout the application lifecycle. You can either create a
`ProxyFactory` directly, or use an implementation of `ApplicationProxier` which can provide it
behind a safe abstraction.

```java
// Directly create the factory
final ProxyFactory factory = new JavassistProxyFactory(TheTypeToProxy.class);

// Through an application proxier
final ApplicationProxier proxier = new SimpleApplicationProxier();
final ProxyFactory factory = proxier.factory(TheTypeToProxy.class);
```

### Interception  
Interception indicates the method is replaced by whichever implementation is chosen. Interception
can be done in two ways; full replacement, and wrapping.

#### Full replacement interception  
A full replacement is done using a custom
`MethodInterceptor`, which accepts a `MethodInterceptorContext` to execute given functionality.
Within an interceptor it is possible to access all required information about the intercepted method,
as can be seen in the `MethodInterceptorContext` class.

Method interceptors are executed in series, allowing each step to re-use and/or modify the result of
another interceptor. To do so, the previous `MethodInterceptorContext#result()` is provided. If
the interceptor is the first one to execute, the result will be the default value of the return type.
The series are executed in no specific order.

```java
factory.intercept(greetingMethod, interceptorContext -> "Hello world!");
final User user = factory.proxy().get();
final String greeting = user.greeting(); // Returns 'Hello world!'
```

#### Wrapping interception  
Wrapping interception is similar to the pre-existing method phasing
approach. It allows for specific callbacks to be executed before a method is performed, after it is finished,
and when an exception is thrown during the execution of the method. Wrappers will always be executed, even
if the method is intercepted or delegated. This allows for specific states to be prepared and closed around
a method's execution. For example, an annotation like `@Transactional` the wrapper can be used to:
<ul>
    <li>Open a transaction before the method is performed</li>
    <li>Commit the transaction after the method is finished</li>
    <li>Rollback the transaction if an exception is thrown</li>
</ul>

```java
public class UserMethodExecutionLogger implements MethodWrapper<User> {
    @Override
    public void acceptBefore(final MethodContext<?, User> method, final User instance, final Object[] args) {
        System.out.println("Before method!");
    }

    @Override
    public void acceptAfter(final MethodContext<?, User> method, final User instance, final Object[] args) {
        System.out.println("After method!");
    }

    @Override
    public void acceptError(final MethodContext<?, User> method, final User instance, final Object[] args, final Throwable error) {
        System.out.println("Method caused an exception: " + error.getMessage());
    }
}
```
```java
factory.intercept(greetingMethod, new UserMethodExecutionLogger());
final User user = factory.proxy().get();
user.speakGreeting();
```

The above would then result in the following output:  

```
Before method!
User says: Hello world!
After method!
```

### Delegation  
Like interception, delegation replaces the implementation of a proxy object. However, it does not carry the proxy's
context down to the implementation. Instead, it redirects the method call to another object. Delegation knows two different
delegate types; original instance, and backing implementations.

#### Original instance delegation  
Original instance delegation indicates that the delegate is of the exact same type as the proxy type, or a sub-type of that
type. This allows all functionality to be delegated to this instance.

```java
public interface User {
    String greeting();
}
public class UserImpl implements User {
    @Override
    public String greeting() {
        return "Hello implementation!";
    }
}
```
```java
final StateAwareProxyFactory<User, ?> factory = applicationManager.factory(User.class);
factory.delegate(new UserImpl());
final User user = factory.proxy().get();
user.greeting(); // Returns 'Hello implementation!'
```

#### Backing implementation delegation  
Backing implementations follow the opposite rule of original instance delegation. Instead of requiring the exact type or a subtype to
be implemented, backing implementations delegate the behavior of a given parent of the type. This allows types like `JpaRepository`
implementations to specifically delegate to e.g. `HibernateJpaRepository`.

```java
public interface User extends ContextCarrier {
    String greeting();
}
public class ContextCarrierImpl implements ContextCarrier {
    @Override
    public ApplicationContext applicationContext() {
        return ...;
    }
}
```
```java
final StateAwareProxyFactory<User, ?> factory = applicationManager.factory(User.class);
factory.delegate(ContextCarrier.class, new ContextCarrierImpl());
final User user = factory.proxy().get();
user.applicationContext(); // Returns a valid application context
user.greeting(); // Yields an exception as no implementation is assigned and the method is abstract
```

However, it is not unlikely a delegate returns itself in chained method calls. To avoid leaking the delegate, method handles always check if
the returned object is the delegate, and will replace it with the proxy instance if it is so.

```java
public interface Returner {
    Returner self();
}
public interface User extends Returner {
    String greeting();
}
public class ReturnerImpl implements Returner {
    @Override
    public Returner self() {
        return this;
    }
}
```
```java
final StateAwareProxyFactory<User, ?> factory = applicationManager.factory(User.class);
factory.delegate(Returner.class, new ReturnerImpl());
final User user = factory.proxy().get();
user.self(); // Returns the user proxy object instead of the ReturnerImpl instance
```

## Building HDP
If you wish to build the project yourself, either to get access to pre-release versions, or to add customizations, the guide below explains how to set up your Gradle environment.  All platforms require a Java installation, with JDK 8 or more recent version.

Set the JAVA\_HOME environment variable. For example:

| Platform | Command |
| :---: | --- |
|  Unix    | ``export JAVA_HOME=/usr/lib/jvm/openjdk-8-jdk``            |
|  OSX     | ``export JAVA_HOME=`/usr/libexec/java_home -v 1.8` ``  |
|  Windows | ``set JAVA_HOME="C:\Program Files\Java\jdk-1.8.0_302"`` |
 
Depending on your IDE the Gradle wrapper may be automatically used. If you encounter any issues, use `./gradlew` for Unix systems or Git Bash and `gradlew.bat` for Windows systems in place of any 'gradle' command.  

Within the directory containing the unpacked source code, run the gradle build:
```bash
./gradlew build
```

## Contributing
Interested in contributing to Hartshorn, want to report a bug, or have a question? Before you do, please read the [contribution guidelines](https://hartshorn.dockbox.org/contributing/)
