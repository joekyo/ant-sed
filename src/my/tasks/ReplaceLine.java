package my.tasks;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;

/*
    usage:
        <replaceline change="x" to="y" files="a, b"/>  <= replace first 'x' to 'y'
        <replaceline change="x" to="y" all="true" files="a, b"/>  <= replace all 'x' to 'y'

        <replaceline contain="z" change="x" to="y" files="a, b"/>
        <replaceline contain="z" next="1" change="x" to="y" files="a, b"/>
        <replaceline contain="z" next="2" skip="1" change="x" to="y" files="a, b"/>

    regex:
        <replaceline change="RE: x" to="y" files="a, b"/>
        <replaceline contain="RE: z" change="RE: x" to="y" files="a, b"/>

    when 'change' is not set, multiple lines can be changed to a single line:
        <replaceline contain="z" to="y" files="a, b"/>   <= replace the whole line to 'y'
        <replaceline contain="z" next="2" to="y" files="a, b"/>   <= replace 3 lines to 'y'
        <replaceline contain="z" next="2" skip="1" to="y" files="a, b"/>   <= replace 2 lines to 'y'

    when no lines were changed, and 'append' is true, append 'to' to file
        <replaceline contain="x" to="y" append="true" files="a, b"/>

*/

public class ReplaceLine extends EditLine {

    private String change, to;

    private Pattern changeRE;

    private int changedLines;

    private boolean replaceAll, appendIfNotChanged;

    public void setChange(String s) {
        if (s.startsWith("RE: ")) {
            changeRE = Pattern.compile(s.substring(4));
        }
        change = s;
    }

    public void setTo(String s) {
        to = s;
    }

    public void setAll(Boolean b) {
        replaceAll = b;
    }

    public void setAppend(Boolean b) {
        appendIfNotChanged = b;
    }

    private static String replaceFirst(String a, String s, String t) {
        int idx = a.indexOf(s);
        if (idx != -1) {
            return a.substring(0, idx) + t + a.substring(idx + s.length());
        } else {
            return a;
        }
    }

    private String maybeReplaceLine(String line) {
        if (changeRE != null) {
            Matcher changeMatcher = changeRE.matcher(line);
            if (changeMatcher.find()) {
                changedLines++;
                String replace;
                if (replaceAll) {
                    replace = changeMatcher.replaceAll(to);
                } else {
                    replace = changeMatcher.replaceFirst(to);
                }
                return replace;
            }
        } else if (change != null) {
            int idx = line.indexOf(change);
            if (idx != -1) {
                changedLines++;
                String replace;
                if (replaceAll) {
                    replace = line.replace(change, to);
                } else {
                    replace = replaceFirst(line, change, to);
                }
                return replace;
            }
        }
        return line;
    }

    private String buildLogMessage() {
        StringBuilder sb = new StringBuilder();
        // line to line mode
        if (change != null) {
            if (to.isEmpty()) {
                sb.append(String.format("Remove %s'%s'", replaceAll ? "all " : "", change));
            } else {
                sb.append(String.format("Change %s'%s' to '%s'", replaceAll ? "all " : "", change, to));
            }
            if (contain != null) {
                sb.append(String.format(" in line contains '%s'", contain));
                addNextAndSkipLinesMessage(sb, next, skip);
            } else if (from != null && until != null) {
                sb.append(String.format(" in lines from '%s' until '%s'", from, until));
            }

        // multiple lines to a single line mode
        } else {
            if (contain != null) {
                sb.append(String.format("Replace line which contains '%s'", contain));
                addNextAndSkipLinesMessage(sb, next, skip);
            } else if (from != null && until != null) {
                sb.append(String.format("Replace lines from '%s' until '%s'", from, until));
            }
            sb.append(String.format(" to '%s'", to));
        }

        if (appendIfNotChanged) {
            sb.append(String.format(" or append line '%s'", to));
        }

        return sb.toString();
    }

    protected void editFile(String file) throws BuildException {
        changedLines = 0;
        int nextLines = 0;
        int skipLines = 0;
        boolean foundContain = false;
        boolean lineAppended = false;

        if (change != null && change.equals(to)) {
            if (verbose) {
                log(String.format("Skip modifying %s as attributes 'change' and 'to' are both '%s'\n", file, change));
            }
            return;
        }

        List<String> lines = IOUtils.readFile(file);

        if (verbose) {
            log(buildLogMessage());
        }

        StringBuilder sb = new StringBuilder();

        // simply replace all 'change' to 'to'
        if (contain == null && from == null && until == null) {
            for (String line : lines) {
                appendline(sb, maybeReplaceLine(line));
            }

        } else if (contain != null) {
            for (String line : lines) {
                if (nextLines > 0) {
                    nextLines--;

                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                        continue;
                    }

                    // both 'contain' and 'next' are set, if 'change' is set too, replace
                    // 'change' to 'to' in the 'contain' line and the next 'next' lines
                    if (change != null) {
                        appendline(sb, maybeReplaceLine(line));

                    // when 'contain' and 'next' are set, but 'change' is not set,
                    // the next 'next' lines are changed to 'to'
                    } else {
                        // lines are deleted here
                        changedLines++;

                        // add a single line
                        if (nextLines == 0) {
                            appendline(sb, to);
                        }
                    }

                // 'contain' is set and the line is matched, try replacing 'change' to 'to'
                } else if (containRE != null && containRE.matcher(line).find() ||
                    containRE == null && contain != null && line.contains(contain)) {

                    foundContain = true;
                    nextLines = next;
                    skipLines = skip;

                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                        continue;
                    }

                    if (change == null) {
                        // multiple lines are changed to a single line
                        // include this matched line
                        if (nextLines > 0) {
                            changedLines++;
                            continue;

                        // 'contain' is matched, and 'change' is not set
                        //  if line != 'to', change the whole line to 'to'
                        } else {
                            if (line.equals(to)) {
                                appendline(sb, line);
                            } else {
                                changedLines++;
                                appendline(sb, to);
                            }
                            continue;
                        }
                    }

                    appendline(sb, maybeReplaceLine(line));
                } else {
                    appendline(sb, line);
                }
            }

        } else {
            boolean inBlock = false;
            for (String line : lines) {
                // enter 'from' ~ 'until' block
                if (!inBlock) {
                    if (fromRE != null && fromRE.matcher(line).find() ||
                    fromRE == null && from != null && line.contains(from)) {
                        skipLines = skip;
                        inBlock = true;

                        if (skipLines > 0) {
                            skipLines--;
                            appendline(sb, line);

                        // 'change' is set; change 'change' to 'to' in the block
                        } else if (change != null) {
                            appendline(sb, maybeReplaceLine(line));

                        // 'change' is not set; change the next 'next' lines to 'to'
                        } else {
                            // lines are deleted here
                            changedLines++;
                        }
                    continue;
                    }
                }

                // exit block
                if (inBlock) {
                    if (untilRE != null && untilRE.matcher(line).find() ||
                        untilRE == null && until != null && line.contains(until)) {

                        skipLines = 0;
                        inBlock = false;

                        if (change != null) {
                            appendline(sb, maybeReplaceLine(line));

                        // add the single line
                        } else {
                            appendline(sb, to);
                        }
                    continue;
                    }
                }

                if (inBlock) {
                    // 'change' is set; change 'change' to 'to' in the block
                    if (change != null) {
                        if (skipLines > 0) {
                            skipLines--;
                            appendline(sb, line);
                        } else {
                            appendline(sb, maybeReplaceLine(line));
                        }

                    // 'change' is not set; change the next 'next' lines are changed to 'to'
                    } else {
                        // lines are deleted here
                        changedLines++;
                    }
                } else {
                    appendline(sb, line);
                }
            }
        }

        if (changedLines == 0 && !foundContain && appendIfNotChanged) {
            appendline(sb, to);
            lineAppended = true;
            changedLines++;
        }

        if (changedLines > 0) {
            IOUtils.writeFile(file, sb.toString());
        }

        if (verbose) {
            if (changedLines > 1) {
                log(String.format("%d lines were changed in %s\n", changedLines, file));
            } else if (lineAppended) {
                log(String.format("1 line was appended in %s\n", file));
            } else if (changedLines == 0 && foundContain) {
                log(String.format("No line was changed since '%s' already exists in %s\n", to, file));
            } else { // 0 or 1
                log(String.format("%d line was changed in %s\n", changedLines, file));
            }
        }
    }

    protected void checkAttributes() throws BuildException {
        super.checkAttributes();

        if (to == null) {
            throw new BuildException("Attribute 'to' is required");
        }
        if (change == null && contain == null && (from == null || until == null)) {
            throw new BuildException("Attribute 'change' or 'contain', or 'from' and 'until' are required");
        }
        if (replaceAll && change == null) {
            throw new BuildException("Attribute 'all' requires 'change' is set");
        }
        if (appendIfNotChanged && change != null) {
            throw new BuildException("Attribute 'append' can not be used with 'change'");
        }
    }

}
