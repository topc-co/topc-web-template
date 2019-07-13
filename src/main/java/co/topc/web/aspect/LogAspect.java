package co.topc.web.aspect;

import co.topc.web.commons.utils.StreamUtil;
import co.topc.web.commons.utils.TopcCollectionUtils;
import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author RuntimeExcepti0n
 * @date 2019/7/4 23:34
 */
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogAspect {
    private  Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* co.topc.web.*.*.controller.*.*(..))")
    public Object requestLog(ProceedingJoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getName();
        String className = methodSignature.getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();
        List<Object>  requestAgs = StreamUtil.streamOf(args)
                                    .filter(arg -> arg instanceof HttpServletRequest)
                                    .collect(Collectors.toList());
        //打印request参数
        if(TopcCollectionUtils.isNotEmpty(requestAgs)){
            HttpServletRequest request = (HttpServletRequest) requestAgs.get(0);
            String remoteAddr = request.getRemoteAddr();
            logger.info("request class: {} ##method: {} ##remoteAddr: {}",className,methodName,remoteAddr);
            logger.info("## user-agent:{}",request.getHeader("user-agent"));
            logger.info("## Origin:{}",request.getHeader("Origin"));
            logger.info("## Referer:{}",request.getHeader("Referer"));

        }
        //打印非request参数
        List<Object> logArgs = StreamUtil.streamOf(args)
                .filter(arg ->!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse))
                .collect(Collectors.toList());
        String argwStr = JSON.toJSONString(logArgs);
        logger.info("className:{}## method {}## args:{}",className,methodName,argwStr);
        Object result = null;
        try {
             result = joinPoint.proceed();
            logger.info("className:{}## method {}## result:{}",className,methodName,result);
        } catch (Throwable throwable) {
            logger.error("日志打印异常",throwable);
        }
        return  result;
    }
}
