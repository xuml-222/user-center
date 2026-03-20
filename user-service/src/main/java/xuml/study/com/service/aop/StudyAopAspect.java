package xuml.study.com.service.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StudyAopAspect {
    @Pointcut("execution(* xuml.study.com.service.service.impl.StudyConfigServiceImpl.*(..))")
    public void pointcut() {

    }
    @Before("pointcut()")
    private void Before() {
        
        System.out.println("......before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("......after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("......afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("......afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("......around ...start");
        // 打印入参日志
        Object[] args = proceedingJoinPoint.getArgs();
        if (args != null && args.length > 0) {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                params.append("arg[").append(i).append("]=").append(args[i]).append(", ");
            }
            System.out.println("入参: " + params.toString());
        } else {
            System.out.println("入参: 无");
        }
        Object proceed = proceedingJoinPoint.proceed();
        System.out.println("......around ...end");
        return proceed;
    }

}
