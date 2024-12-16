package com.flavienhtz.api.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@Aspect
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    // return type, method name, parameters, args
    @Before("execution(* com.flavienhtz.api.controller.*.*(..))")
    public void logMethodCall(JoinPoint joinPoint) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.info("Method called {}", joinPoint.getSignature().getName() + " " + timestamp);
    }

    @After("execution(* com.flavienhtz.api.controller.*.*(..))")
    public void logMethodExecuted(JoinPoint joinPoint) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.info("Method executed {}", joinPoint.getSignature().getName() + " " + timestamp);
    }

    @AfterThrowing("execution(* com.flavienhtz.api.controller.*.*(..))")
    public void logMethodThrowing(JoinPoint joinPoint) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.info("Method throwing error {}", joinPoint.getSignature().getName() + " " + timestamp);
    }

}
