package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    Integer selectBatchCountByDishId(List<Long> dishIds);

    @Select("select setmeal_id from setmeal_dish where dish_id = #{dishId}")
    List<Long> selectSetmealIdByDishId(Long dishId);
}
