package net.ldcc.playground.aop;

import net.ldcc.playground.annotation.DbType;
import net.ldcc.playground.dao.BaseDao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

@Aspect
@Component
public class DataSourceAspect {
    private final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    private final ApplicationContext context;

    public DataSourceAspect(ApplicationContext context) {
        this.context = context;
    }

    @Around("execution(* net.ldcc.playground.dao..*.*(..))")
    public Object switchDataSource(ProceedingJoinPoint pjp) throws Throwable {
        @SuppressWarnings("rawtypes")
        Class cls = pjp.getSignature().getDeclaringType();
        String clsName = pjp.getSignature().getDeclaringTypeName();
        String mtdName = pjp.getSignature().getName();

        // StackTrace to find the controller/service method has called current method
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // > [DEBUG] Print StackTrace
        Arrays.stream(stackTrace).forEach(t -> logger.debug("StackTrace : {}.{}", t.getClassName(), t.getMethodName()));

        // > Find the current method's position
        Iterator<StackTraceElement> iter = Arrays.stream(stackTrace).iterator();
        while (iter.hasNext()) {
            StackTraceElement element = iter.next();
            if (element.getClassName().contains(clsName) && element.getMethodName().equals(mtdName)) {
                break;
            }
        }

        // > Check 'DbType' Annotation
        DbType.Profile profile = null;
        if (iter.hasNext()) {
            StackTraceElement stSvc = iter.next(); // (DAO 메소드를 호출한 Service)

            // >> Service
            @SuppressWarnings("rawtypes")
            Class clsSvc = Class.forName(stSvc.getClassName());
            Method mtdSvc = Arrays.stream(clsSvc.getDeclaredMethods())
                    .filter(m -> m.getName().equals(stSvc.getMethodName()))
                    .findFirst()
                    .orElse(null);

            if (clsSvc.isAnnotationPresent(DbType.class)) {
                DbType dbType = (DbType) clsSvc.getAnnotation(DbType.class);
                profile = dbType.profile();
            } else if (mtdSvc != null && mtdSvc.isAnnotationPresent(DbType.class)) {
                DbType dbType = mtdSvc.getAnnotation(DbType.class);
                profile = dbType.profile();
            }

            // >> Controller
            if (iter.hasNext() && profile == null) {
                StackTraceElement stCtrl = iter.next(); // (Service 메소드를 호출한 Controller)

                @SuppressWarnings("rawtypes")
                Class clsCtrl = Class.forName(stCtrl.getClassName());
                Method mtdCtrl = Arrays.stream(clsCtrl.getDeclaredMethods())
                        .filter(m -> m.getName().equals(stCtrl.getMethodName()))
                        .findFirst()
                        .orElse(null);

                if (clsCtrl.isAnnotationPresent(DbType.class)) {
                    DbType dbType = (DbType) clsCtrl.getAnnotation(DbType.class);
                    profile = dbType.profile();
                } else if (mtdCtrl != null && mtdCtrl.isAnnotationPresent(DbType.class)) {
                    DbType dbType = mtdCtrl.getAnnotation(DbType.class);
                    profile = dbType.profile();
                }
            }
        }

        // [DEBUG] Selected DbType Profile
        logger.debug("DbType.Profile={}", profile);

        // Switch data source
        @SuppressWarnings("rawtypes")
        BaseDao dao = (BaseDao) context.getBean(cls);
        Object result;

        // > Lock/UnLock data source
        synchronized (dao) {
            if (profile != null) {
                DbType.Profile prevProfile = dao.getProfile();
                dao.setDataSource(profile);
                result = pjp.proceed();
                dao.setDataSource(prevProfile);

            } else {
                result = pjp.proceed();
            }
        }

        return result;
    }
}
