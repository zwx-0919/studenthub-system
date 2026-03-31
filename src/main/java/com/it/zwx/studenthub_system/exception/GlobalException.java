package com.it.zwx.studenthub_system.exception;

import com.it.zwx.studenthub_system.pojo.entity.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//用于在Spring框架中定义全局控制器，Aop的思想
//应用发生错误时，spring会通过和这个注解，找到错误处理的方法
//结合 @ControllerAdvice 和 @ResponseBody，以实现对控制器异常的全局处理和统一响应的功能。
//确保返回的结果会被序列化为 JSON 格式，并能够处理全局的异常。
@RestControllerAdvice
public class GlobalException {

    //用来处理异常
    //@ExceptionHandler：用来标记一个方法为异常处理方法。
    // 这个方法会被 Spring 框架调用，当控制器中出现指定类型的异常时。
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        //异常信息输出到控制台，方便我们调试
        e.printStackTrace();
        //getMessage()封装的错误信息。有一些情况可能没有
        //StringUtils spring提供的工具类。
        //用于处理字符串操作。hasLength 方法用于检查字符串是否有长度，即非空且长度大于零。
        return Result.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败");
    }
}
