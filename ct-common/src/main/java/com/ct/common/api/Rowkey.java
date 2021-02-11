package com.ct.common.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rowkey {
}
