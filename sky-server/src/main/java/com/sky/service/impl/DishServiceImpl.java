package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    SetmealMapper setmealMapper;

    /**
     * 新增菜品和菜品口味
     * @param dishDTO
     */
    @Override
    // 多次sql操作，开启事务
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 新增菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(0);
        dishMapper.insert(dish);
        // 新增口味
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),  dishPageQueryDTO.getPageSize());
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishPageQueryDTO, dish);
        Page<DishVO> dishVOPage = dishMapper.pageQuery(dish);
        PageResult pageResult = new PageResult();
        pageResult.setRecords(dishVOPage.getResult());
        pageResult.setTotal(dishVOPage.getTotal());
        return pageResult;
    }

    /**
     * 批量删除菜品与菜品口味
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        // 启售的菜品不应删除
        List<Dish> dishes = dishMapper.selectBatchById(ids);
        for (Dish dish : dishes) {
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 存在关联的套餐不应删除
        if (setmealDishMapper.selectBatchCountByDishId(ids) > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品
        dishMapper.delete(ids);
        // 删除菜品的口味
        dishFlavorMapper.deleteByDishId(ids);
    }

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品与菜品口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        // 修改菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        // 修改菜品口味
        dishFlavorMapper.deleteByDishId(Collections.singletonList(dishDTO.getId()));
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 启售/停售菜品
     * @param id
     * @param status
     */
    @Override
    @Transactional
    public void startOrStop(Long id, Integer status) {
        Dish dish = dishMapper.selectById(id);
        //  如果要将菜品停售，需要将关联的套餐一起停售
        if (status.equals(StatusConstant.DISABLE)) {
            List<Long> setmealIds = setmealDishMapper.selectSetmealIdByDishId(dish.getId());
            if (setmealIds != null && !setmealIds.isEmpty()) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = new Setmeal();
                    setmeal.setId(setmealId);
                    setmeal.setStatus(status);
                    setmealMapper.update(setmeal);
                }
            }
        }
        dish.setStatus(status);
        dishMapper.update(dish);
    }
}
