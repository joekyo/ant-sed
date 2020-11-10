#!/usr/bin/python

import sys

# escape <task attr=' < > & " '> to
# <task attr=" &lt; &gt; &amp; &quot; ">

begin="='"
end="'"

def escape_chars(a):
  return a.replace('&', '&amp;').replace('<', '&lt;').replace(
    '>', '&gt;').replace('"', '&quot;').replace("'", '"')

def escape(s):
  b = s
  i = s.find(begin)
  if i == -1:
    return s
  while i != -1:
    j = s.find(end, i+len(begin))
    if j == -1:
      raise Exception("missing ending `'` in: " + b)
    s = s[:i] + escape_chars(s[i:j+len(end)]) + s[j+len(end):]
    i = s.find("'")
  return s

if __name__ == '__main__':
  for line in sys.stdin:
    print(escape(line.rstrip()))
