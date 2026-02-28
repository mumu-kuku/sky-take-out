package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("正在新增菜品");
        dishService.saveWithFlavor(dishDTO);

        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("正在分页查询菜品");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("正在根据 ids:{} 删除菜品", ids);
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品和口味")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("正在根据 id{} 查询菜品和口味", id);
        DishVO dishVo = dishService.getById(id);
        return Result.success(dishVo);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "setmealCache", key = "#dishDTO.categoryId")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("正在修改id为{}的菜品", dishDTO.getId());
        dishService.update(dishDTO);

        // 修改菜品需要同步清理菜品缓存，保证时效性
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        log.info("已删除菜品缓存:{}", key);

        return Result.success();
    }

    /**
     * 启售/停售菜品
     * @param id
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(Long id,  @PathVariable Integer status) {
        log.info("正在根据 id:{} 启售/停售菜品", id);
        dishService.startOrStop(id, status);

        // 停售启售菜品需要同步清理菜品缓存，保证时效性
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        log.info("已删除菜品缓存:{}", keys);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("正在根据分类id:{} 查询菜品", categoryId);
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }
}
