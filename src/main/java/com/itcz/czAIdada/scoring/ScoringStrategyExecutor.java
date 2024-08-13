package com.itcz.czAIdada.scoring;

import com.itcz.czAIdada.annotation.ScoringStrategyConfig;
import com.itcz.czAIdada.common.ErrorCode;
import com.itcz.czAIdada.exception.BusinessException;
import com.itcz.czAIdada.model.entity.App;
import com.itcz.czAIdada.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {

    // 策略列表
    @Resource
    private List<ScoringStrategy> scoringStrategyList;


    /**
     * 评分
     *
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        Integer appType = app.getAppType();
        Integer appScoringStrategy = app.getScoringStrategy();
        if (appType == null || appScoringStrategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }
        // 根据注解获取策略
        for (ScoringStrategy strategy : scoringStrategyList) {
            //这里使用反射
            //getClass().isAnnotationPresent() 是Java中用于检查当前对象是否包含某个注解的方法。
            // 它是一个静态方法，适用于所有类和接口的对象实例。
            // 当你需要确定某个类或方法是否有特定的注解时，你可以通过这种方式获取其Class对象，
            // 然后调用 isAnnotationPresent() 来判断是否存在指定的注解。
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == appScoringStrategy) {
                    return strategy.doScore(choiceList, app);
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }
}
