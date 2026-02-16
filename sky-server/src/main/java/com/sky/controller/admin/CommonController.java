package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api()
public class CommonController {
    @Autowired
    AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        log.info("正在上传文件{}", file);
        try {
            String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传文件失败{}", e);
            e.printStackTrace();
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
