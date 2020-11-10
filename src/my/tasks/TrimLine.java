package my.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

/*
    usage:
        <trimline contain="x" prefix="#" files="a, b"/>
        <trimline contain="x" prefix="/ *" suffix="* /" files="a, b"/>

        <trimline from="x" until="y" prefix="#" files="a, b"/>
        <trimline from="x" until="y" skip="1" prefix="#" files="a, b"/>

        <trimline contain="x" next="1" prefix="#" files="a, b"/>
        <trimline contain="x" next="1" skip="1" prefix="#" files="a, b"/>

    regex:
        <trimline contain="RE: x" prefix="#" files="a, b"/>
        <trimline contain="RE: x" prefix="/ *" suffix="* /" files="a, b"/>

    Note that spaces will be trimmed too, ' #x ' will become 'x'.

*/

public class TrimLine extends EditLine {

    private String prefix, suffix;

    private int trimmedLines = 0;

    public void setPrefix(String s) {
        prefix = s;
    }

    public void setSuffix(String s) {
        suffix = s;
    }

    private String buildLogMessage() {
        StringBuilder sb = new StringBuilder();

        if (prefix != null && suffix != null) {
            sb.append(String.format("Remove prefix '%s' and suffix '%s'", prefix, suffix));
        } else if (prefix != null) {
            sb.append(String.format("Remove prefix '%s'", prefix));
        } else {
            sb.append(String.format("Remove suffix '%s'", suffix));
        }

        if (contain != null) {
            sb.append(String.format(" in line contains '%s'", contain));
        } else {
            sb.append(String.format(" in lines from '%s' until '%s'", from, until));
        }

        addNextAndSkipLinesMessage(sb, next, skip);

        return sb.toString();
    }

    private String trim(String line) {
        String tline = line.trim();
        boolean trimmed = false;
        if (prefix != null && tline.startsWith(prefix)) {
            tline = tline.substring(prefix.length());
            trimmed = true;
        }
        if (suffix != null && tline.endsWith(suffix)) {
            tline = tline.substring(0, tline.length() - suffix.length());
            trimmed = true;
        }
        if (trimmed) {
            trimmedLines++;
            return tline;
        }
        return line;
    }

    protected void editFile(String file) throws BuildException {
        trimmedLines = 0;
        int nextLines = 0;
        int skipLines = 0;

        List<String> lines = IOUtils.readFile(file);

        if (verbose) {
            log(buildLogMessage());
        }

        StringBuilder sb = new StringBuilder();

        if (contain != null) {
            for (String line : lines) {
                if (nextLines > 0) {
                    nextLines--;
                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                    } else {
                        appendline(sb, trim(line));
                    }
                    continue;

                } else if (containRE != null && containRE.matcher(line).find()
                        || containRE == null && contain != null && line.contains(contain)) {
                    nextLines = next;
                    skipLines = skip;

                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                    } else {
                        appendline(sb, trim(line));
                    }

                } else {
                    appendline(sb, line);
                }
            }
        } else {
            boolean inBlock = false;
            for (String line : lines) {
                // enter 'from' ~ 'until' block
                if (!inBlock) {
                    if (fromRE != null && fromRE.matcher(line).find()
                            || fromRE == null && from != null && line.contains(from)) {
                        skipLines = skip;
                        inBlock = true;

                        if (skipLines > 0) {
                            skipLines--;
                            appendline(sb, line);
                        } else {
                            appendline(sb, trim(line));
                        }
                        continue;
                    }
                }

                // exit block
                if (inBlock) {
                    if (untilRE != null && untilRE.matcher(line).find()
                            || untilRE == null && until != null && line.contains(until)) {
                        skipLines = 0;
                        inBlock = false;
                        appendline(sb, trim(line));
                        continue;
                    }
                }

                if (inBlock) {
                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                    }
                    appendline(sb, trim(line));
                    {
                        appendline(sb, line);
                    }
                }
            }
        }

        if (trimmedLines > 0) {
            IOUtils.writeFile(file, sb.toString());
        }

        if (verbose) {
            if (trimmedLines > 1) {
                log(String.format("%d lines were trimmed in %s\n", trimmedLines, file));
            } else {
                log(String.format("%d line was trimmed in %s\n", trimmedLines, file));
            }
        }
    }

    protected void checkAttributes() throws BuildException {
        super.checkAttributes();

        if (contain == null && (from == null || until == null)) {
            throw new BuildException("Attribute 'contain' or attributes 'from' and 'until' are required");
        }
        if (prefix == null && suffix == null) {
            throw new BuildException("Attribute 'prefix' or 'suffix' is required");
        }
    }

}
