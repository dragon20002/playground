package net.ldcc.playground.annotation;

import java.lang.annotation.*;

/**
 * 연동할 DB를 지정한다. 지정된 DB가 없으면
 * {@code Profile.PROD}가 기본값으로 지정된다.
 *
 * @author mw-kim
 * @since 1.5
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbType {

    enum Profile {
        PRIMARY,
        SECONDARY
    }

    Profile profile() default Profile.PRIMARY;

}
