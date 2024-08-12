package com.itcz.czAIdada.scoring;

import com.itcz.czAIdada.model.entity.App;
import com.itcz.czAIdada.model.entity.UserAnswer;

import java.util.List;

/**
 * 自定义打分类应用评分策略
 *
 */

public class CustomScoreScoringStrategy implements ScoringStrategy{
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        return null;
    }
}
