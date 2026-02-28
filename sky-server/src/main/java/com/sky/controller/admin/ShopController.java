package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags ="管理端店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 修改店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改店铺营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("将店铺状态设置为:{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        return Result.success();
    }

    /**
     * 管理端获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("管理端获取店铺营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("管理端获取店铺营业状态, 当前状态是:{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
