[![Build Status](https://travis-ci.com/eeverman/andhow.svg?branch=master)](https://travis-ci.com/github/eeverman/andhow)
[![codecov](https://codecov.io/gh/eeverman/andhow/branch/main/graph/badge.svg?token=hs7nr7V0Ed)](https://codecov.io/gh/eeverman/andhow)
[![Javadocs](https://www.javadoc.io/badge/org.yarnandtail/andhow.svg)](https://www.javadoc.io/doc/org.yarnandtail/andhow)


## New Release:  0.4.1, June 2, 2021 ([notes](https://github.com/eeverman/andhow/releases/tag/andhow-0.4.1)).
<img src="https://github.com/eeverman/andhow/raw/master/logo/AndHow-empty-circle-combination.png" width="55" height="72" alt="AndHow's new logo"  align="left">

This larger update fixes several issues due to newer JVMs and the IntelliJ IDE.
Special thanks to first time contributor [Vicky Ronnen](https://github.com/VickyRonnen) for fixing Issue [497](https://github.com/eeverman/andhow/issues/497) -
This bug made AndHow unusable for anyone using newer versions of IntelliJ.


![Andhow Visual](andhow.gif)
## AndHow!  strong.valid.simple.AppConfiguration
AndHow is an easy to use configuration framework with strong typing and detailed
validation for web apps, command line or any application environment.

_**Learn more at the [AndHow main site](https://sites.google.com/view/andhow)**_

## Key Features
* **Strong Typing**
* **Detailed validation**
* **Simple to use**
* **Use Java _public_ & _private_ to control configuration visibility**
* **Validates _all_ property values at startup to _[Fail Fast](http://www.practical-programming.org/ppl/docs/articles/fail_fast_principle/fail_fast_principle.html)_**
* **Loads values from multiple sources (JNDI, env vars, prop files, etc)**
* **Generates configuration sample file based on  application properties**

## Questions / Discussion / Contact
[Join the discussion](https://sites.google.com/view/andhow/join-discussion)
on the [user forum](https://groups.google.com/d/forum/andhowuser)
or the *Slack* group (See details on the
[Join](https://sites.google.com/view/andhow/join-discussion) page).

## Use it via Maven (available on Maven Central)
```xml
<dependency>
    <groupId>org.yarnandtail</groupId>
    <artifactId>andhow</artifactId>
    <version>0.4.1</version>
</dependency>
```
**AndHow can be used in projects with Java 8 - Java 15.  Work to support Java 16 is underway and will be the next major release.  There are [some considerations](https://sites.google.com/view/andhow/user-guide/java9-and-above) for Java 9 and above if your project uses Jigsaw Modules.**

## Complete Usage Example
_**More usage examples and documentation
are available at the [AndHow main site](https://sites.google.com/view/andhow)**_
```java
package org.simple;

import org.yarnandtail.andhow.AndHow;
import org.yarnandtail.andhow.property.*;

public class GettingStarted {
	
	//1
	public final static IntProp COUNT_DOWN_START = IntProp.builder().mustBeNonNull()
			.mustBeGreaterThanOrEqualTo(1).defaultValue(3).build();
	
	private final static StrProp LAUNCH_CMD = StrProp.builder().mustBeNonNull()
			.desc("What to say when its time to launch")
			.mustMatchRegex(".*Go.*").defaultValue("Go-Go-Go!").build();
	
	public String launch() {
		String launch = "";
		
		//2
		for (int i = COUNT_DOWN_START.getValue(); i >= 1; i--) {
			launch = launch += i + "...";
		}
		
		return launch + LAUNCH_CMD.getValue();
	}
	
	public static void main(String[] args) {
		AndHow.findConfig().setCmdLineArgs(args);	//3 Optional
		
		System.out.println( new GettingStarted().launch() );
		System.out.println( LAUNCH_CMD.getValue().replace("Go", "Gone") );
	}
}
```
### Section //1 : Declaring AndHow Properties
Properties must be `final static`, but may be `private` or any other scope.
`builder` methods simplify adding validation, description, defaults and
other metadata.
Properties are strongly typed, so default values and validation are type specific, e.g.,
`StrProp` has Regex validation while the `IntProp` has GreaterThan / LessThan rules available.

### Section //2 : Using AndHow Properties
AndHow Properties are used just like static final constants with an added
`.getValue()` tacked on. Strong typing means that calling `COUNT_DOWN_START.getValue()`
returns an `Integer` while calling `LAUNCH_CMD.getValue()` returns a `String`.

An AndHow Property (and its value) can be accessed anywhere it is visible.
`COUNT_DOWN_START` is public in a public class, so it could be used anywhere, while
`LAUNCH_CMD` is private.
AndHow Properties are always `static`, so they can be accessed in both static
and instance methods, just like this example shows.

### Section //3 : Accepting Command Line Arguments
If an application needs command line arguments (CLAs), just pass them to AndHow
at startup as this example shows.   Properties are referred to using 'dot notation', e.g.:
```
java -jar GettingStarted.jar org.simple.GettingStarted.LAUNCH_CMD=GoManGo
```
If you don't need to accept CLA's, you can leave line `//3` out -
AndHow will initialize and startup without any explicit _init_ method when
the first Property is accessed.

### How do I actually configure some values?
We're getting there.
The example has defaults for each property so with no other configuration available,
the main method uses the defaults and prints:
```
3...2...1...Go-Go-Go!
Gone-Gone-Gone!
```
Things are more interesting if the default values are removed from the code above:
```java
public final static IntProp COUNT_DOWN_START = IntProp.builder().mustBeNonNull()
		.mustBeGreaterThanOrEqualTo(1).build();  //default removed
	
private final static StrProp LAUNCH_CMD = StrProp.builder().mustBeNonNull()
		.mustMatchRegex(".*Go.*").build();  //default removed
```
Both properties must be non-null, so removing the defaults causes the validation
rules to be violated at startup.  Here is an excerpt from the console when that happens:
```
========================================================================
Drat! There were AndHow startup errors.
Sample configuration files will be written to: '/some_local_tmp_directory/andhow-samples/'
========================================================================
Property org.simple.GettingStarted.COUNT_DOWN_START: This Property must be non-null
Property org.simple.GettingStarted.LAUNCH_CMD: This Property must be non-null
========================================================================
```

**AndHow does validation at startup for all properties in the entire application.**
Properties, _even those defined in 3rd party jars_, are discovered and values for
them are loaded and validated.  
If validation fails (as it did above), AndHow throws a RuntimeException to stop
application startup and uses property metadata to generate specific error
messages and (helpfully) sample configuration files.
Here is an excerpt of the Java Properties file created when the code above failed validation:
```
# ######################################################################
# Property Group org.simple.GettingStarted

# COUNT_DOWN_START (Integer) NON-NULL - Start the countdown from this number
# The property value must be greater than or equal to 1
org.simple.GettingStarted.COUNT_DOWN_START = [Integer]

# LAUNCH_CMD (String) NON-NULL - What to say when its time to launch
# The property value must match the regex expression '.*Go.*'
org.simple.GettingStarted.LAUNCH_CMD = [String]
```
AndHow uses all of the provided metadata to create a detailed and well commented
configuration file for your project.  
Insert some real values into that file and place it on your classpath at
`/andhow.properties` and it will automatically be discovered and loaded at startup.
By default, AndHow discovers and loads configuration from seven common sources.  
The default list of configuration loading, in order, is:
1. Fixed values (explicitly set in code for AndHow to use)
2. String[] arguments from the static void main() method
3. System Properties _(Like the one auto-generated above)_
4. Environmental Variables
5. JNDI
6. Java properties file on the filesystem (path must be specified)
7. Java properties file on the classpath (defaults to /andhow.properties)

Property values are set on a first-win basis, so if a property is set as fixed value,
that will take precedence over values passed in to the main method.  
Values passed to the main method take precedence over system properties as so on.

_**For more examples and documentation, visit the [AndHow main site](https://sites.google.com/view/andhow)**_

_**&?!**_
