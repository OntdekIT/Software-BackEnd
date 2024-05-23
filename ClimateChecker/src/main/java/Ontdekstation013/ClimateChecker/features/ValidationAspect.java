package Ontdekstation013.ClimateChecker.features;

import Ontdekstation013.ClimateChecker.features.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ValidationAspect {

    private final UserService userService;
    private final JWTService jwtService;

    public ValidationAspect(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Pointcuts for UserController methods
    @Pointcut("execution(* Ontdekstation013.ClimateChecker.features.user.endpoint.UserController.*(..))")
    public void userControllerMethods() {
    }

    // Pointcuts for AuthController methods
    @Pointcut("execution(* Ontdekstation013.ClimateChecker.features.authentication.endpoint.AuthController.*(..))")
    public void authControllerMethods() {
    }

    // Pointcut for AdminController methods
    @Pointcut("execution(* Ontdekstation013.ClimateChecker.features.admin.endpoint.AdminController.*(..))")
    public void adminMethods() {
    }

    // Combined pointcut for controllers excluding specific methods
    @Pointcut("(userControllerMethods() || authControllerMethods()) "
            + "&& !execution(* Ontdekstation013.ClimateChecker.features.authentication.endpoint.AuthController.createNewUser(..)) " +
            "&& !execution(* Ontdekstation013.ClimateChecker.features.authentication.endpoint.AuthController.loginUser(..)) " +
            "&& !execution(* Ontdekstation013.ClimateChecker.features.authentication.endpoint.AuthController.verifyEmailCode(..))"
    )
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object validateToken(ProceedingJoinPoint joinPoint) throws Throwable {
        Cookie[] cookies;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            if(jwtService.verifyJWT(cookies[0].getValue()))
            {
                return joinPoint.proceed();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }

    @Around("adminMethods()")
    public Object validateAdminRights(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            if(jwtService.verifyJWT(cookies[0].getValue()))
            {
                Long userID = jwtService.getIdFromJWT(cookies[0].getValue());
                boolean isAdmin = userService.checkAdmin(userID);
                if(isAdmin)
                {
                    return joinPoint.proceed();
                }
            }

        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }

}