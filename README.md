## What is Apache Ant?

Apache Ant is a build tool for Java, just like `make` for C/C++.

Although most Java projects use Maven and Gradle nowadays, there are still
projects, like Tomcat, use Ant as a build tool.

## What are Ant Tasks?

In Apache Ant, you can do many things with Tasks.

There are tasks for building, testing, and archiving, and
[many more](https://ant.apache.org/manual/tasksoverview.html).

## Why I create these tasks?

There are built-in tasks for modifying text files, but they are neither easy nor powerful to us. To workaround this, one solution is to call `sed` in a `exec` task as following

```xml
 <exec executable="sed">
   <arg line="-i s/foo/bar/ a.txt"/>
 </exec>
```

This works fine, but when there are a lot of this kind of `sed` commands, thing became a bit of ugly. What I would like to have is something looks like

```xml
<replaceline change="foo" to="bar" file="a.txt"/>
```

Looks much better, doesn't it? Not only this will improve the readibility, but also a `sed` is not required to be installed in your system. Yes, it works in Windows too.

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
<trimline contain="foo" suffix="#" file="a.txt"/>
```

Regular expressions are supported.

Read `usage.md` for more details about how to use these tasks.

## How to add it to your build script?

There is a already built `my-tasks.jar` exists in this repo.

Save this file and add the following lines at the begin of your build xml file

```xml
<taskdef resource="my/tasks/antlib.xml">
  <classpath>
    <pathelement location="../my-tasks.jar"/>
  </classpath>
</taskdef>
```

If you want to build from source files, you can run `ant`.
The `build.xml` in this repo will be executed, and a `my-tasks.jar` will be created.

**Note that at least JDK 8 and Ant 1.10.x are required**

## How to test it?

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

To show debug messages, run

```sh
./test.sh replace -d
```
