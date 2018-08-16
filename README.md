PureUtilities is a library of general Java utility classes that contains very 
few external dependencies.

While it is possible to use the standalone facilities of PureUtilities, like any
other java class, there are a number of compiler extensions built in, and in
order to avail of these functions, you must hook into them during the compile
stage.

# Class Discovery and Installation
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
jar is scanned at startup each time. Additionally, the first search query of a
given signature is cached, so future calls at runtime will be faster. Thus, it
may be useful to preheat various queries if you know they will be used later in
runtime.

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

# Functionality

## ClassDiscovery

Once properly installed, the ClassDiscovery system allows you to get information
about the classes in the specified jars quickly and efficiently. Even if you
have not or cannot add the caching as described in the installation above, you
can still make use of the rest of the ClassDiscovery system, simply by
installing the jars you wish to search through at runtime.

Note that even if a jar has been compiled with the cached ClassDiscovery 
information, at runtime, you still need to register the jar(s) that you wish
to scan. `ClassDiscovery.addDiscoveryLocation()` or 
`ClassDiscovery.addAllJarsInFolder()` will add the various jars to the scanning 
path. For convenience, there is also a `ClassDiscovery.GetClassContainer()`
method, which accepts a class and gets the root container URL. Note that
ClassDiscovery supports both jars or folders, and the url to the "container"
can be difficult to determine for files, so it is generally best to simply use
this method, which determines it for you. However, if you have a folder of jars,
for instance in an extension based system, then it may be easier to manually
add the jars.

In general however, once installed, the facilities available are relatively
straightforward: there are methods to get classes, methods, and fields that have
various properties. In the case of classes, you may find all classes that
extend/implement a certain superclass/interface, and/or find all classes that
have an annotation on the class. Note that annotations must have 
@Retention(RetentionPolicy.RUNTIME) in order to be supported by this mechanism.

You can also find fields and methods with a given annotation.

There is currently no support for parameter annotations, though they can be
retrieved from the returned Method object.

There is a notable difference between the get* and load* class of methods in
ClassDiscovery. Using a get* method returns a Class/Field/MethodMirror type, 
rather than a Class/Field/Method. This is to prevent 2 things. In older versions
of Java, loading a class puts it in permgen, which might be undesirable,
particularly if you find yourself scanning a large number of classes. Secondly,
loading a class causes its static initializers to run, which also might be
undesirable. All of the load* methods have 2 versions, one that uses the default
ClassLoader, and a second which accepts a ClassLoader, which is then used to
load the class. It is also possible to get the real Class from the Mirror as
well. Note that getting the real Method or Field will also cause the real Class
object to be loaded and initialized as well. Often this is desirable, but it
simply depends on your use case as to which is most appropriate.

Additionally, there are other utility methods which allow various processing,
details of which can be found directly in the javadocs.

## Utilities

There are a number of other utilities in the package as well. Each class is
generally self explanatory, or has javadoc which you can read about.