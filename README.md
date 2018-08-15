PureUtilities is a library of general Java utility classes that contains very 
few external dependencies.

While it is possible to use the standalone facilities of PureUtilities, like any
other java class, there are a number of compiler extensions built in, and in
order to avail of these functions, you must hook into them during the compile
stage.

# Class Discovery
The ClassDiscovery mechanism is an in depth and easy to use Class management
system. It allows for programs to follow meta programming to the extreme, and
request a list of all classes that have certain properties, particularly to
find all subclasses of a given superclass, or to find all classes, methods, or
fields that are tagged with a given annotation.

In combination with this, and custom compile time checks, it becomes nearly
trivial to scan a list of all compiling classes, to ensure that they meet
various project defined characteristics. There are a few annotations in the
Annotations package, which are defined by PureUtilities and managed
automatically, assuming the below installation steps are followed.

In general, Class discovery is a very resource intense process. Given that, a
decent amount of work has gone into caching. There are a few levels of caching,
though it is entirely possible to dynamically scan a jar, it is recommended
that where possible, jars are configured to use the below compile hooks. In
addition to enabling the ability to use the Annotations to ensure compliance
at compile time, it also adds a summary file to the jar directly, allowing a
much quicker startup. If there is no summary file in a jar, then a summary file
is created at first runtime and installed at the location specified at
ClassDiscovery#setClassDiscoveryCache(). If this was not set either, then the
jar is scanned at startup each time.

## Maven

There are 3 main steps to get this working with Maven. First, add the dependency
as normal in the dependencies section of your POM. Second, add the following
section to the build/plugins section:

    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.2.1</version>
        <executions>
            <execution>
                <id>cache-annotations</id>
                <goals>
                    <goal>java</goal>
                </goals>
                <phase>process-classes</phase>
            </execution>
        </executions>
        <configuration>
            <mainClass>com.path.to.your.class.CustomCompileChecks</mainClass>
            <arguments>
                <argument>${basedir}/target/classes</argument>
                <argument>${basedir}/target/classes</argument>
            </arguments>
        </configuration>
    </plugin>

Note that you need to change the mainClass value to a class you create in your
project.

Third, whatever that class you just created, add the following code to it:

    package com.path.to.your.class;
    import com.laytonsmith.PureUtilities.Common.Annotations.AnnotationChecks;
    public class CustomCompileChecks {
        public static void main(String[] args) throws Exception {
            AnnotationChecks.main(args);
        }
    }

Essentially, you must create a class *within* your project, which simply
forwards the main call into the PureUtilities AnnotationChecks main method.

If you are already using the exec-maven-plugin facility, you simply need to
call AnnotationChecks.main(args) from within your existing code.

Once that is complete, all of the custom compiler checks should be activated.

Enjoy!

## Gradle

TODO

## Multiple Jar system

By default, if you have a single jar project, (or you only care about scanning
classes within a single jar) then there is no extra 
configuration needed to make the ClassDiscovery system work other than what was
described above. However, if you
have multiple jars or an extension system, you'll need to manually add these
jars at runtime. Additionally, these other projects must manage their own
compilation using the above instructions to ensure that they also are able to
avail of the compile checks.

In order to add these jars, you should use 
ClassDiscovery#addDiscoveryLocation() or ClassDiscovery#addAllJarsInFolder(),
which will add the various jars to the scanning path.