package com.doublez.backend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.doublez.backend.enums.PermissionType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    PermissionType value();
    int agencyIdIndex() default -1;
    int listingIdIndex() default -1;
    int agentIdIndex() default -1;
}

