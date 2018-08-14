PureUtilities is a library of general Java utility classes that contains very 
few external dependencies

While it is possible to use the standalone facilities of PureUtilities, like any
other java class, there are a number of compiler extensions built in, and in
order to avail of these functions, you must hook into them during the compile
stage.

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