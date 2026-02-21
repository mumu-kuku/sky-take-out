package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 统计菜品关联的套餐数量
     * @param dishIds
     * @return
     */
    Integer selectBatchCountByDishId(List<Long> dishIds);

     /**
      * 根据菜品id查询关联的套餐id
      * @param dishId
      * @return
      */
    @Select("select setmeal_id from setmeal_dish where dish_id = #{dishId}")
    List<Long> selectSetmealIdByDishId(Long dishId);

     /**
      * 批量新增套餐菜品关联关系
      * @param setmealDishes
      */
    void insertBatch(List<SetmealDish> setmealDishes);

     /**
      * 根据套餐id查询套餐菜品关联关系
      * @param setmealId
      * @return
      */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> selectBySetmealId(Long setmealId);

    /**
     * 根据套餐id删除套餐菜品关联关系
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据套餐id查询菜品id
     * @param setmealId
     * @return
     */
    @Select("select dish_id from setmeal_dish where setmeal_id = #{setmealId}")
    List<Long> selectDishIdBySetmealId(Long setmealId);
}
