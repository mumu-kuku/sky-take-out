package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理 SQL 异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        // 判断当前异常信息，判断是否为唯一字段重复
        if (message.contains("Duplicate entry")) {
            // Duplicate entry 'username' for key 'employee.idx_username'
            String[] split = message.split(" ");
            String duplicateValue = split[2];
            // 返回该唯一字段值重复的错误信息
            return Result.error(duplicateValue + MessageConstant.ALREADY_EXISTS);
        } else {
            // 返回未知错误
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
