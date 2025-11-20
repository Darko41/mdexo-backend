package com.doublez.backend.testscenarios;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SystemMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitoringAspect.class);

    @Around("execution(* com.doublez.backend.service..*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) {
                logger.warn("⏱️ Slow operation detected: {}.{} took {}ms", 
                    className, methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Error in {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}