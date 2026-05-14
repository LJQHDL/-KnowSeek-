package com.example.copilot.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class})
})
public class MybatisSqlLoggingInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = normalizeSql(boundSql.getSql());

        long startTime = System.currentTimeMillis();
        try {
            Object result = invocation.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.debug("""
                    [SQL]
                    mapper={}
                    sql={}
                    params={}
                    cost={}ms
                    """,
                    mappedStatement.getId(),
                    sql,
                    parameter,
                    cost
            );
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("""
                    [SQL-ERROR]
                    mapper={}
                    sql={}
                    params={}
                    cost={}ms
                    """,
                    mappedStatement.getId(),
                    sql,
                    parameter,
                    cost,
                    ex
            );
            throw ex;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private String normalizeSql(String sql) {
        if (!StringUtils.hasText(sql)) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }
}
