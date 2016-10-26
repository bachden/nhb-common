package com.nhb.common.annotations;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ FIELD, METHOD, TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transparent {

}
