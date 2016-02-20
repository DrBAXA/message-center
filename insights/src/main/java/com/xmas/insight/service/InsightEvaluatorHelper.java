package com.xmas.insight.service;

import com.xmas.entity.EntityHelper;
import com.xmas.insight.dao.InsightEvaluatorRepository;
import com.xmas.insight.entity.Insight;
import com.xmas.insight.entity.InsightEvaluator;
import com.xmas.util.FileUtil;
import com.xmas.util.data.FileSystemData;
import com.xmas.util.script.ScriptFileUtil;
import com.xmas.util.script.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class InsightEvaluatorHelper {

    @Autowired
    private InsightEvaluatorRepository repository;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private EntityHelper<Insight, InsightEvaluator> insightHelper;

    private static String EVALUATORS_BASE_DIR = InsightEvaluatorHelper.class.getResource("/insights").getPath();

    public void saveInsightEvaluator(InsightEvaluator evaluator, MultipartFile scriptFile){
        File evaluatorDir = FileUtil.createRandomNameDirInThis(EVALUATORS_BASE_DIR);
        ScriptFileUtil.saveScript(evaluatorDir.getAbsolutePath(), scriptFile);
        evaluator.setDirectoryPath(evaluatorDir.toPath().getFileName().toString());
        saveToDB(evaluator);
    }

    private void saveToDB(InsightEvaluator evaluator){
        repository.save(evaluator);
    }

    public void evaluate(InsightEvaluator evaluator){
        FileSystemData fileSystemData  = new FileSystemData(getDataDirFullPath(evaluator));
        LocalDateTime evaluationTime = fileSystemData.evaluateData(null);
        scriptService.evaluate(evaluator.getScriptType(), getDataDirFullPath(evaluator), evaluator.getScriptArgs());
        evaluator.setLastTimeEvaluated(evaluationTime);
        insightHelper.save(getDataDirFullPath(evaluator), evaluator);
        fileSystemData.packageEvaluatedFiles();
    }

    private String getDataDirFullPath(InsightEvaluator evaluator) {
        return this.getClass().getResource("/insights/").getPath() + evaluator.getDirectoryPath();
    }

}
