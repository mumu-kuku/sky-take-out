package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {
    /**
     * 根据分类 id 查询菜品
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer selectCountByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 分页查询菜品和菜品所属分类名称
     * @param dish
     * @return
     */
    Page<DishVO> pageQuery(Dish dish);

    /**
     * 根据 id 批量查询菜品
     * @param ids
     * @return
     */
    List<Dish> selectBatchById(List<Long> ids);

     /**
      * 根据 id 批量删除菜品
      * @param ids
      */
    void delete(List<Long> ids);

     /**
      * 根据 id 查询菜品
      * @param id
      * @return
      */
    Dish selectById(Long id);

     /**
      * 更新菜品
      * @param dish
      */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

     /**
      * 根据分类 id 查询菜品
      * @param categoryId
      * @return
      */
    List<Dish> selectByCategoryId(Long categoryId);

     /**
      * 条件查询菜品和口味
      * @param dish
      * @return
      */
    List<Dish> list(Dish dish);
}
