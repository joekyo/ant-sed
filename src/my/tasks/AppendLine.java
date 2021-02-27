package my.tasks;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;

public class AppendLine extends EditLine {

    private String after, before;

    private Pattern afterRE, beforeRE;

    private String text, concatFile;

    public void setAdd(String s) {
        text = s;
    }

    public void setConcat(String s) {
        concatFile = s;
    }

    public void setAfter(String s) {
        if (s.startsWith("RE: ")) {
            afterRE = Pattern.compile(s.substring(4));
        }
        after = s;
    }

    public void setBefore(String s) {
        if (s.startsWith("RE: ")) {
            beforeRE = Pattern.compile(s.substring(4));
        }
        before = s;
    }

    private int appendOrConcat(StringBuilder sb, String text, List<String> concatLines) {
        if (text != null) {
            appendline(sb, text);
            return 1;
        }
        for (String line : concatLines) {
            appendline(sb, line);
        }
        return concatLines.size();
    }

    protected void editFile(String file) throws BuildException {
        List<String> concatLines = null;
        if (concatFile != null) {
            concatLines = IOUtils.readFile(concatFile);
        }

        List<String> lines = IOUtils.readFile(file);

        if (verbose) {
            StringBuilder sb = new StringBuilder();
            if (text != null) {
                sb.append(String.format("Append line '%s'", text));
            } else {
                sb.append(String.format("Append content of %s", concatFile));
            }
            if (before != null) {
                sb.append(String.format(" before '%s'", before));
            } else if (after != null) {
                sb.append(String.format(" after '%s'", after));
            }
            log(sb.toString());
        }

        int addLines = 0;

        StringBuilder sb = new StringBuilder();

        if (before == null && after == null) {
            for (String line : lines) {
                appendline(sb, line);
            }
            addLines += appendOrConcat(sb, text, concatLines);
        } else {
            for (String line : lines) {
                if (beforeRE != null && beforeRE.matcher(line).find()
                        || beforeRE == null && before != null && line.contains(before)) {

                    addLines += appendOrConcat(sb, text, concatLines);
                }

                appendline(sb, line);

                if (afterRE != null && afterRE.matcher(line).find()
                        || afterRE == null && after != null && line.contains(after)) {

                    addLines += appendOrConcat(sb, text, concatLines);
                }
            }
        }

        if (addLines > 0) {
            IOUtils.writeFile(file, sb.toString());
        }

        if (verbose) {
            String msg;
            if (addLines > 1) {
                msg = String.format("%d lines were added in %s", addLines, file);
            } else {
                msg = String.format("%d line was added in %s", addLines, file);
            }
            log(msg);
        }
    }

    protected void checkAttributes() throws BuildException {
        super.checkAttributes();

        if (text == null && concatFile == null) {
            throw new BuildException("Attribute 'add' or 'concat' is required");
        }
        if (text != null && concatFile != null) {
            throw new BuildException("Attribute 'add' can not be used with 'concat");
        }
    }

}
