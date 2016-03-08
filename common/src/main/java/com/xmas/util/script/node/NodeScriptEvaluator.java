package com.xmas.util.script.node;

import com.xmas.exceptions.ProcessingException;
import com.xmas.util.script.ScriptEvaluator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Qualifier("nodeScriptEvaluator")
public class NodeScriptEvaluator implements ScriptEvaluator {

    private static final Logger logger = LogManager.getLogger();

    public static final String SCRIPT_FILE = "/script/script.sc";

    @Value("${node.global.lib}")
    private String nodePath;

    @Override
    public void evaluate(String scriptFileName, String workDir, Map<String, String> args) {
        try {

            Process process = Runtime.getRuntime()
                    .exec(buildExecString(workDir, args), getEnvironment(), new File(workDir));

            logScriptExecutingProcess(process);

            int processResult = process.exitValue();

            if (processResult != 0) {
                String error = getError(process.getErrorStream());
                processError(error);
            }
        } catch (IOException | ProcessingException e) {
            throw new ProcessingException(e);
        }
    }

    private void logScriptExecutingProcess(Process process){
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader stdReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        logStrings(errorReader.lines(), Level.ERROR);
        logStrings(stdReader.lines(), Level.DEBUG);
    }

    private void logStrings(Stream<String> stream, Level level){
        stream.forEach(s -> logger.log(level, s));
    }

    private void processError(String executionResult) {
        Pattern noModuleErrorPattern = Pattern.compile("Error: Cannot find module '([a-z]+)'");
        Matcher matcher = noModuleErrorPattern.matcher(executionResult);
        if (matcher.find()) {
            try {
                tryToInstallRequiredModule(matcher.group(1));
            } catch (ProcessingException pe){
                throw new ProcessingException("Error during evaluating Node script. Exit code: " + executionResult);
            }
        } else {
            throw new ProcessingException("Error during evaluating Node script. Exit code: " + executionResult);
        }
    }

    private String buildExecString(String workDir, Map<String, String> args) {
        return new ArrayList<String>() {{
            add("node");
            add(workDir + SCRIPT_FILE);
            addAll(buildScriptArgsString(args));
        }}.stream().collect(Collectors.joining(" "));
    }

    private List<String> buildScriptArgsString(Map<String, String> args) {
        return args.keySet().stream()
                .map(key -> key + " " + args.get(key))
                .collect(Collectors.toList());
    }

    private String[] getEnvironment(){
        return new String[]{"NODE_PATH="+nodePath};
    }

    private String getError(InputStream errorStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private void tryToInstallRequiredModule(String moduleName) {
        try {
            Process process = new ProcessBuilder()
                    .command("npm", "install", "-g", moduleName)
                    .start();

            int processResult = process.waitFor();

            if (processResult != 0) {
                String error = getError(process.getErrorStream());
                throw new ProcessingException("Error during installing Node module. Exit code: " + processResult + ".\n" +
                        error);
            }
        } catch (IOException | InterruptedException | ProcessingException e) {
            throw new ProcessingException(e);
        }
    }

}
