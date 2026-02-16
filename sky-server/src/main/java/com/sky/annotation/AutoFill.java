package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

import java.lang.annotation.ElementType;

// 自动填充注解，用于标记需要自动填充的方法
// 用于mapper控制层
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 数据库操作类型
     OperationType value();
}
