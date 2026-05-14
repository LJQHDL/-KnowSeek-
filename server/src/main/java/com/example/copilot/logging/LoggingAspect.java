package com.example.copilot.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("execution(* com.example.copilot.controller..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logInvocation("CONTROLLER", joinPoint);
    }

    @Around("execution(* com.example.copilot.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logInvocation("SERVICE", joinPoint);
    }

    private Object logInvocation(String layer, ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String arguments = Arrays.stream(joinPoint.getArgs())
                .map(arg -> LoggingUtils.sanitizeObject(arg, objectMapper))
                .reduce((left, right) -> left + ", " + right)
                .orElse("");

        long startTime = System.currentTimeMillis();
        log.debug("[{}-START] {}.{} args=[{}]", layer, className, methodName, arguments);

        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.debug("[{}-END] {}.{} cost={}ms result={}",
                    layer,
                    className,
                    methodName,
                    cost,
                    LoggingUtils.sanitizeObject(result, objectMapper));
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[{}-ERROR] {}.{} cost={}ms args=[{}]",
                    layer,
                    className,
                    methodName,
                    cost,
                    arguments,
                    ex);
            throw ex;
        }
    }
}
