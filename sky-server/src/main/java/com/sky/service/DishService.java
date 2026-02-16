package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和菜品口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    DishVO getById(Long id);

    /**
     * 修改菜品和菜品口味
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     * 启售/停售菜品
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);
}
