package my.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

public class DeleteLine extends EditLine {

    protected void editFile(String file) throws BuildException {
        List<String> lines = IOUtils.readFile(file);

        if (verbose) {
            StringBuilder sb = new StringBuilder();
            if (contain != null) {
                sb.append(String.format("Delete lines which contain '%s'", contain));
            } else {
                sb.append(String.format("Delete lines from '%s' until '%s'", from, until));
            }
            addNextAndSkipLinesMessage(sb, next, skip);
            log(sb.toString());
        }

        int nextLines = 0;
        int skipLines = 0;
        int deletedLines = 0;

        StringBuilder sb = new StringBuilder();

        if (contain != null) {
            for (String line : lines) {
                if (nextLines > 0) {
                    nextLines--;
                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                    } else {
                        deletedLines++;
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
                        deletedLines++;
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
                            deletedLines++;
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
                        deletedLines++;
                        continue;
                    }
                }

                if (inBlock) {
                    if (skipLines > 0) {
                        skipLines--;
                        appendline(sb, line);
                    }
                    deletedLines++;
                } else {
                    appendline(sb, line);
                }
            }
        }

        if (deletedLines > 0) {
            IOUtils.writeFile(file, sb.toString());
        }

        if (verbose) {
            String msg;
            if (deletedLines > 1) {
                msg = String.format("%d lines were deleted in %s\n", deletedLines, file);
            } else {
                msg = String.format("%d line was deleted in %s\n", deletedLines, file);
            }
            log(msg);
        }
    }

    protected void checkAttributes() throws BuildException {
        super.checkAttributes();

        if (contain == null && (from == null || until == null)) {
            throw new BuildException("Attribute 'contain' or attributes 'from' and 'until' are required");
        }
    }

}
