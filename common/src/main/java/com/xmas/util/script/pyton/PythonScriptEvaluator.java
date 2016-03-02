package com.xmas.util.script.pyton;

import com.xmas.exceptions.ProcessingException;
import com.xmas.util.script.ScriptEvaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Qualifier("pythonScriptEvaluator")
public class PythonScriptEvaluator implements ScriptEvaluator {

    private static final Logger logger = LogManager.getLogger();

    public static final String DIR_ARG_NAME = "question_dir";
    public static final String SCRIPT_FILE = "/script/script.sc";

    @Autowired
    private PythonInterpreterManager interpreterManager;

    @Override
    public void evaluate(String script, String workDir, Map<String, String> args) {
        try {

            Process process = Runtime.getRuntime()
                    .exec(buildExecString(workDir, args),getEnv() , new File(workDir));

            int processResult = process.waitFor();

            if (processResult != 0) {
                String error = getError(process.getErrorStream());
                throw new ProcessingException(error);
            }
        } catch (IOException | InterruptedException | ProcessingException e) {
            logger.error("Error during executind script " + workDir + SCRIPT_FILE);
            logger.debug(e.getMessage(), e);
            throw new ProcessingException(e);
        }
    }

    private String[] getEnv(){
        List<String> res = System.getenv()
                .entrySet()
                .stream()
                .map(e -> e.getKey() + " " + e.getValue())
                .collect(Collectors.toList());

        return res.toArray(new String[res.size()]);
    }

    private String buildExecString(String workDir, Map<String, String> args) {
        return new ArrayList<String>() {{
            add(workDir + SCRIPT_FILE);
            addAll(buildScriptArgsString(args));
        }}.stream().collect(Collectors.joining(" "));
    }

    private List<String> buildScriptArgsString(Map<String, String> args) {
        return args.keySet().stream()
                .map(key -> key + " " + args.get(key))
                .collect(Collectors.toList());
    }

    private String getError(InputStream errorStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
        return reader.lines().collect(Collectors.joining("\n"));
    }

}
