## ReplaceLine

Replace the _first_ occurrence of `foo` to `bar`

```xml
<replaceline change="foo" to="bar" file="a.txt"/>
```

Replace _all_ occurrences of `foo` to `bar`

```xml
<replaceline change="foo" to="bar" all="true" file="a.txt"/>
```

Find the line contains `foo`, replace `bar` to `baz`

```xml
<replaceline contain="foo" change="bar" to="baz" file="a.txt"/>
```

### When `change` is not set, the whole lines will be replaced

Find the line contains `foo`, change the whole line to `bar`

```xml
<replaceline contain="foo" to="bar" file="a.txt"/>
```

Fine the line contains `foo`, and the next `2` lines, change these lines to `bar`

```xml
<replaceline contain="foo" next="2" to="bar" file="a.txt"/>
```

Find the line contains `foo`, replace the whole line to `bar`. If `foo` is not found, add `bar` as a new line to the end of the file

```xml
<replaceline contain="foo" to="bar" append="true" file="a.txt"/>
```

## AppendLine

Add `foo` as a new line to the end of the file

```xml
<appendline add="foo" file="a.txt"/>
```

Find line contains `foo`, add `bar` as a new line after it

```xml
<appendline after="foo" add="bar" file="a.txt"/>
```

Find line contains `foo`, add `bar` as a new line before it

```xml
<appendline before="foo" add="bar" file="a.txt"/>
```

Find the line contains `foo` in `a.txt`, append the content of `b.txt` after it

```xml
<appendline file="a.txt" after="foo" concat="b.txt"/>
```

## DeleteLine

Delete lines which contain `foo`

```xml
<deleteline contain="foo" file="a.txt"/>
```

Delete lines from line contains `foo` until line contains `bar`

```xml
<deleteline from="foo" until="bar" file="a.txt"/>
```

From line contains `foo` until line contains `bar`, keep the first `1` line, delete the rest lines

```xml
<deleteline from="foo" until="bar" skip="1" file="a.txt"/>
```

Delete line contains `foo`, and the next `1` line

```xml
<deleteline contain="foo" next="1" file="a.txt"/>
```

Find line contains `foo`, delete the next `1` line

```xml
<deleteline contain="foo" next="1" skip="1" file="a.txt"/>
```

## TrimLine

Find line contains `foo`, if the first character is `#`, remove it

```xml
<trimline contain="foo" prefix="#" file="a.txt"/>
```

Find line contains `foo`, remove prefix `/ *` and suffix `* /`

```xml
<trimline contain="foo" prefix="/ *" suffix="* /" file="a.txt"/>
```

**Note that spaces will be trimmed too, so ` #foo` will become `foo`.**

**If you are modifying a Python script, this may be not what you want.**

## Block Mode

Attribute pairs like `contain` and `next`, `from` and `until` can be used to select a block of text

```xml
<replaceline contain="foo" next="2" change="bar" to="baz" file="a.txt"/>
<replaceline from="foo" until="foz" change="bar" to="baz" file="a.txt"/>

<deleteline contain="foo" next="2" file="a.txt"/>
<deleteline from="foo" until="bar" file="a.txt"/>

<trimline from="foo" until="bar" prefix="#" file="a.txt"/>
<trimline contain="x" next="1" prefix="#" file="a.txt"/>
```

Attribute `skip` can be used to skip the first `n` lines

```xml
<deleteline contain="foo" next="2" skip="1" file="a.txt"/>
<deleteline from="foo" until="bar" skip="1" file="a.txt"/>
```

## Regular Expression

Attributes which are used for pattern matching, like `contain` `change` `from` `until` `before` `after`, support regular expression.

```xml
<replaceline change="RE: foo?" to="bar" file="a.txt"/>
<replaceline contain="RE: foo?" change="RE: bar" to="baz" file="a.txt"/>

<appendline after="RE: foo?" add="bar" file="a.txt"/>
<appendline before="RE: foo?" add="bar" file="a.txt"/>

<deleteline contain="RE: foo?" file="a.txt"/>
<deleteline from="RE: foo?" until="RE: bar" file="a.txt"/>

<trimline contain="RE: foo?" prefix="#" file="a.txt"/>
<trimline contain="RE: foo?" prefix="/ *" suffix="* /" file="a.txt"/>
```

## Multiple Files

To modify multiple files, use `files` instead of `file`

```xml
<replaceline change="foo" to="bar" files="a.txt,b.txt"/>
```
