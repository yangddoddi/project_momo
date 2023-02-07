package com.example.momobe.common.log.aop;

import com.example.momobe.common.log.entity.LogHistory;
import com.example.momobe.common.log.service.LogHistoryService;
import com.example.momobe.security.domain.JwtTokenUtil;
import com.example.momobe.security.enums.SecurityConstants;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.example.momobe.security.enums.SecurityConstants.*;

@Aspect
@RequiredArgsConstructor
public class LoggingAspect {
    private final LogHistoryService logService;
    private final JwtTokenUtil jwtTokenUtil;
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void onRequest() {}

    @Around("onRequest()")
    public Object advice(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            System.out.println("로그 생성중..");
            HttpServletRequest request = getHttpServletRequest();

            String requestMethod = request.getMethod();
            String requestIP = request.getRemoteAddr();
            String requestURI = request.getRequestURI();
            String tokenHeader = request.getHeader(JWT_HEADER);
            String userSequenceId = null;

            if (isGetMethod(requestMethod)) {
                joinPoint.proceed();
            }

            if (hasSecurityHeader(tokenHeader)) {
                userSequenceId = extractUserId(tokenHeader);
            }

            LogHistory log = createLog(requestMethod, requestIP, requestURI, tokenHeader, userSequenceId);

            return saveAndReturn(joinPoint, log);
        } catch (Exception e) {
            log.error("", e);
            return joinPoint.proceed();
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        return request;
    }

    private String extractUserId(String tokenHeader) {
        String userSequenceId;
        Claims claims = jwtTokenUtil.parseAccessToken(tokenHeader);
        userSequenceId = String.valueOf(claims.get(ID));
        return userSequenceId;
    }

    private LogHistory createLog(String requestMethod, String requestIP, String requestURI, String tokenHeader, String userSequenceId) {
        return LogHistory.builder()
                .httpMethod(requestMethod)
                .operatorIP(requestIP)
                .operatorId(StringUtils.hasText(tokenHeader) ? "NULL" : userSequenceId)
                .requestUri(requestURI)
                .build();
    }

    private Object saveAndReturn(ProceedingJoinPoint joinPoint, LogHistory log) throws Throwable {
        logService.saveLog(log);
        return joinPoint.proceed();
    }

    private boolean hasSecurityHeader(String tokenHeader) {
        return StringUtils.hasText(tokenHeader);
    }

    private boolean isGetMethod(String requestMethod) {
        return requestMethod.toUpperCase().equals(HttpMethod.GET.toString());
    }
}
