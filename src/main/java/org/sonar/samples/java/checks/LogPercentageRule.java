package org.sonar.samples.java.checks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;

@Rule(key = "LogPercentage", tags = {"log"})
public class LogPercentageRule implements JavaFileScanner {
    Pattern infoPattern = Pattern.compile("(LOGGER.info\\(.*\\);)");
    Pattern debugPattern = Pattern.compile("(LOGGER.debug\\(.*\\);)");

    @Override
    public void scanFile(JavaFileScannerContext context) {
        if (context.getFileLines().isEmpty()) {
            return;
        }

        int totalLinesCount = context.getFileLines().size();

        int infoLogCount = countLogLevelInFile(infoPattern, context.getFileContent());
        int debugLogCount = countLogLevelInFile(debugPattern, context.getFileContent());

        int totalLogCount = infoLogCount + debugLogCount;

        Matcher matcher = infoPattern.matcher(context.getFileContent());
        while (matcher.find()) {
            ++totalLogCount;
        }

        if (totalLogCount > 0) {
            double logLinesPercentage = ((double) totalLogCount / totalLinesCount) * 100;
            if (logLinesPercentage > 5)
            context.reportIssue(this, context.getTree(), String.format("Log percentage is above 5 %% (%.2f %%)", logLinesPercentage));
        }
    }

    private int countLogLevelInFile(Pattern logPattern, String fileContent) {
        Matcher matcher = logPattern.matcher(fileContent);
        int infoLogCount = 0;
        while (matcher.find()) {
            ++infoLogCount;
        }
        return infoLogCount;
    }
}
