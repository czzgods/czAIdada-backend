package com.itcz.czAIdada.scoring;

import com.itcz.czAIdada.common.ErrorCode;
import com.itcz.czAIdada.exception.BusinessException;
import com.itcz.czAIdada.model.entity.App;
import com.itcz.czAIdada.model.entity.UserAnswer;
import com.itcz.czAIdada.model.enums.AppScoringStrategyEnum;
import com.itcz.czAIdada.model.enums.AppTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Deprecated
public class ScoringStrategyContext {

    @Resource
    private CustomScoreScoringStrategy customScoreScoringStrategy;

    @Resource
    private CustomTestScoringStrategy customTestScoringStrategy;

    /**
     * 评分
     *
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    UserAnswer doScore(List<String> choiceList, App app) throws Exception{
        //获取APP的类型
        AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(app.getAppType());
        //获取APP的判题策略
        AppScoringStrategyEnum appScoringStrategyEnum = AppScoringStrategyEnum.getEnumByValue(app.getScoringStrategy());
        if(appTypeEnum == null || appScoringStrategyEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用配置有误，未找到匹配的APP类型或者策略");
        }
        //根据不同的APP类型和评分策略，选择对应的评分策略
        switch (appTypeEnum){
            case SCORE:
                switch (appScoringStrategyEnum){
                    case CUSTOM:
                        return customScoreScoringStrategy.doScore(choiceList,app);
                    case AI:
                        break;
                }
                break;
            case TEST:
                switch (appScoringStrategyEnum) {
                    case CUSTOM:
                        return customTestScoringStrategy.doScore(choiceList, app);
                    case AI:
                        break;
                }
                break;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用配置有误，未找到匹配的APP类型或者策略");
    }
}
