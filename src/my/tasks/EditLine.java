package my.tasks;

import java.util.regex.Pattern;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

public class EditLine extends Task {
    protected String file, files;

    protected String contain, from, until;

    protected Pattern containRE, fromRE, untilRE;

    protected int next, skip;

    protected boolean verbose = true;

    protected boolean debugOn;

    public void setFile(String s) {
        file = s;
    }

    public void setFiles(String s) {
        files = s;
    }

    public void setContain(String s) {
        if (s.startsWith("RE: ")) {
            containRE = Pattern.compile(s.substring(4));
        }
        contain = s;
    }

    public void setFrom(String s) {
        if (s.startsWith("RE: ")) {
            fromRE = Pattern.compile(s.substring(4));
        }
        from = s;
    }

    public void setUntil(String s) {
        if (s.startsWith("RE: ")) {
            untilRE = Pattern.compile(s.substring(4));
        }
        until = s;
    }

    public void setNext(int n) {
        next = n;
    }

    public void setSkip(int n) {
        skip = n;
    }

    public void setVerbose(Boolean b) {
        verbose = b;
    }

    protected void debug(String msg) {
        if (debugOn) {
            log("DEBUG: " + msg);
        }
    }

    protected void appendline(StringBuilder sb, String line) {
        sb.append(line);
        sb.append('\n');
    }

    protected void editFile(String file) throws BuildException {
    }

    protected void addNextAndSkipLinesMessage(StringBuilder sb, int next, int skip) {
        if (next > 1) {
            sb.append(String.format(" and the next %d lines", next));
        } else if (next == 1) {
            sb.append(" and the next line");
        }
        if (skip > 0) {
            sb.append(String.format(" (%d %s skipped)", skip, (skip > 1) ? "lines" : "line"));
        }
    }

    protected void checkAttributes() throws BuildException {
        if (file == null && files == null) {
            throw new BuildException("Attribute 'file' or 'files' is required");
        }
        if (contain != null && (from != null || until != null)) {
            throw new BuildException("Attribute 'contain' can not be used with 'from' or 'until'");
        }
        if (from != null && until == null || from == null && until != null) {
            throw new BuildException("Attribute 'from' and 'until' should be used together");
        }
        if (next < 0) {
            throw new BuildException("Attribute 'next' value must not be negative");
        }
        if (next > 0 && contain == null) {
            throw new BuildException("Attribute 'next' requires 'contain' is set");
        }
        if (next > 0 && skip > 0 && skip > next) {
            throw new BuildException("Attribute 'skip' must be less than 'next'");
        }
    }

    public void execute() {
        checkAttributes();

        if (getProject().getProperty("my.tasks.debug") != null) {
            debugOn = true;
        }

        if (files != null) {
            for (String file : files.split(",")) {
                editFile(file.trim());
            }
        } else {
            editFile(file.trim());
        }
    }
}
