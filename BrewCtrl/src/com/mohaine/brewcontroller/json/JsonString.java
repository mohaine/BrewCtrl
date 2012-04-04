package com.mohaine.brewcontroller.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to mark a string field as already being valid json. The
 * Encoder will copy its value unescaped into the encoded json.
 * 
 * @author graesslem
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonString {

}
