## What is Apache Ant?

[Apache Ant](http://ant.apache.org/) is a build tool for Java, just like `make` for C/C++.

Although most Java projects use Maven and Gradle nowadays, there are still
projects, like Tomcat, use Ant as their build tool.

## What are Ant Tasks?

In Apache Ant, you can do many things with Tasks.

There are tasks for building, testing, and archiving, and
[many more](https://ant.apache.org/manual/tasksoverview.html).

Usually one task is designed for doing one specific job.

## Why I create these tasks?

There are built-in tasks for modifying text files, but none of them are easy nor powerful to use. To workaround this, one have to call `sed` in a `exec` task as following

```xml
 <exec executable="sed">
   <arg line="-i /foo/s/bar/baz/ a.txt b.txt"/>
 </exec>
```

This works fine, except it looks a bit ugly; and people don't know `sed` cannot understand this code. What I wish to have is something looks like this

```xml
<replaceline contain="foo" change="bar" to="baz" files="a.txt,b.txt"/>
```

Looks much better, doesn't it? Not only this will improve the readability, but also an `sed` is not required to be installed in your system.

## What are these tasks?

There are mainly 3 tasks, for modifying lines,

```xml
<replaceline change="foo" to="bar" file="a.txt"/>
```

for adding lines,

```xml
<appendline after="foo" add="baz" file="a.txt"/>
```

and for deleting lines.

```xml
<deleteline contain="foo" file="a.txt"/>
```

Plus another task for trimming prefix and suffix.

```xml
<trimline contain="foo" prefix="#" file="a.txt"/>
```

Regular expressions are supported.

Read `doc.md` for more details about how to use these tasks.

## How to add them to my Ant build script?

There is a already built `my-tasks.jar` exists in this repo.

Save this file and add the following lines at the begin of your ant build script

```xml
<taskdef resource="my/tasks/antlib.xml">
  <classpath>
    <pathelement location="./my-tasks.jar"/>
  </classpath>
</taskdef>
```

If you want to build it from source code, clone this repo and run `ant`.
The `build.xml` in this repo will be executed, and a new `my-tasks.jar` will be created.

**Note that at least JDK 8 and Ant 1.10.x are required**

## What about testing?

I have created tests to make sure that these tasks work as I expected.

There are test cases in the `tests` directory in this repo.

To run all tests, use

```sh
./test.sh
```

To run a specific test, for example, to test the `ReplaceLine` task

```sh
./test.sh replace
```

To show debug messages

```sh
./test.sh replace -d
```
