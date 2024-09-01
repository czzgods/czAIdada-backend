/*
package com.itcz.czAIdada.aop;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RedissonClient;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class NumberUpdateAspect {
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;
    @Resource
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisLock redisLock;
    @Resource
    private AsyncTaskExecutor asyncServiceExecutor;
    private static final String UPDATE_COUNT_LOCK="lock:update:";
    private static final String INTERFACE = ":interface:";

    @Pointcut("@annotation(com.itcz.czword.interfaces.annotation.NumberUpdate)")
    public void pt(){}

    @Around("pt()")
    public Object NumberUpdateAspect(ProceedingJoinPoint joinPoint) {
        Object proceed;
        long startTime;
        long endTime;
        Object[] args = joinPoint.getArgs();
        //这里是获取被拦截方法的第一个参数
        User user = (User) args[0];
        //这里是获取被拦截方法的第二个参数
        Long interfaceId = (Long) args[1];
        Long userId = user.getId();
        startTime = System.currentTimeMillis();
       */
/* //创建锁对象
        RLock lock = redissonClient.getLock(UPDATE_COUNT_LOCK + userId);*//*

        //尝试获取锁
        boolean lock = redisLock.lock(UPDATE_COUNT_LOCK + userId, null);
        //下面这段while循环里的代码并不是使用死循环来不断重新获取锁，
        // 而是在等待时间内尝试获取锁，如果成功则执行目标方法，否则等待一段时间再次尝试。
        while (true) {
            try {
                if (lock) {
                    //这里获取锁成功后判断该用户是否有使用次数
                    if (hasCount(joinPoint, userId, interfaceId)) {
                        try {
                            //如果有次数，就放行
                            proceed = joinPoint.proceed();
                        } catch (Throwable e) {
                            //如果异常的类型是我们自定义的异常，就转换成我们自定义的异常抛出
                            if (e.getClass().equals(BusinessException.class)) {
                                throw (BusinessException) e;
                            }
                            throw new RuntimeException(e);
                        }
                    } else {
                        //表示该用户没有使用次数
                        throw new BusinessException(ErrorCode.INTERFACE_COUNT_ERROR);
                    }
                    //到这里说明用户已经调用接口成功，接下来要实现更新使用次数
                    updateUseCount(joinPoint, userId, interfaceId);
                    endTime = System.currentTimeMillis();
                    log.error("接口执行时间耗时:" + (endTime - startTime) + "毫秒");
                    return proceed;
                }else {
                    try {
                        Thread.sleep(10l);
                        log.error("线程休眠-尝试获取锁");
                        //线程休眠100毫秒，再次尝试获取锁
                        lock = redisLock.lock(UPDATE_COUNT_LOCK + userId, null);
                        log.error("获取锁状态:"+lock);
                    }catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                //释放锁
                redisLock.unlock(UPDATE_COUNT_LOCK+userId,null);
            }
        }
    }

    */
/**
     * 判断用户是否有接口使用次数
     * @param joinPoint
     * @param userId
     * @param interfaceId
     * @return
     *//*

    private boolean hasCount(ProceedingJoinPoint joinPoint, Long userId, Long interfaceId) {
        UserInterfaceInfo userInterfaceInfo;
        //先尝试从redis中获取数据
        String userInterfaceStr = redisTemplate.opsForValue().get(InterfaceConstant.USER_INTERFACE_LOCK + userId + INTERFACE + interfaceId);
        if(!StringUtils.hasText(userInterfaceStr)){
            //到这里说明redis中没有数据，此时需要从数据库中查询数据
            LambdaQueryWrapper<UserInterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserInterfaceInfo::getUserId,userId);
            queryWrapper.eq(UserInterfaceInfo::getInterfaceInfoId,interfaceId);
            userInterfaceInfo = userInterfaceInfoMapper.selectOne(queryWrapper);
            if(userInterfaceInfo == null){
                throw new BusinessException(ErrorCode.INTERFACE_USER_ERROR);
            }
            //保存数据信息到redis中
            String toJSONString = JSON.toJSONString(userInterfaceInfo);
            redisTemplate.opsForValue().set(InterfaceConstant.USER_INTERFACE_LOCK + userId + INTERFACE + interfaceId,toJSONString,3,TimeUnit.MINUTES);
        }else {
            //到这里说明redis中查询到了数据
            userInterfaceInfo = JSON.parseObject(userInterfaceStr, UserInterfaceInfo.class);
        }
        //判定是否有剩余次数
        Integer leftNum = userInterfaceInfo.getLeftNum();
        if(leftNum <= 0){
            return false;
        }
        return true;
    }

    */
/**
     * 用户调用接口后更新剩余使用次数
     * @param joinPoint
     * @param userId
     * @param interfaceId
     *//*

    private synchronized void updateUseCount(ProceedingJoinPoint joinPoint, Long userId, Long interfaceId) {
        //从redis中获取数据信息
        //这里应该是可以从redis中获取到数据信息的，因为前面在判断用户是否有使用次数的时候已经把数据添加到redis中了
        String JSONStr = redisTemplate.opsForValue().get(InterfaceConstant.USER_INTERFACE_LOCK + userId + INTERFACE + interfaceId);
        UserInterfaceInfo userInterfaceInfo = JSON.parseObject(JSONStr, UserInterfaceInfo.class);
        userInterfaceInfo.setTotalNum(userInterfaceInfo.getTotalNum()+1);
        userInterfaceInfo.setLeftNum(userInterfaceInfo.getLeftNum()-1);
        String toJSONString = JSON.toJSONString(userInterfaceInfo);
        redisTemplate.opsForValue().set(InterfaceConstant.USER_INTERFACE_LOCK + userId + INTERFACE + interfaceId,toJSONString);
        //异步修改数据库信息
        //asyncServiceExecutor 是一个执行异步任务的执行器（ExecutorService）。
        //execute 方法用于提交一个任务给执行器执行。
        asyncServiceExecutor.execute(new UpdateCountThread(userInterfaceInfo,userInterfaceInfoMapper));
    }

    */
/**
     * 静态内部类，实现异步把redis中的数据更新到数据库中
     *//*

    @AllArgsConstructor
    private static class UpdateCountThread implements Runnable{
        private UserInterfaceInfo userInterfaceInfo;
        private UserInterfaceInfoMapper userInterfaceInfoMapper;
        @Override
        public void run() {
            try {
                //线程休眠一秒再插入
                Thread.sleep(1000);
                userInterfaceInfoMapper.updateById(userInterfaceInfo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
*/
