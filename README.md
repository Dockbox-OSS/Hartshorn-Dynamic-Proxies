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
