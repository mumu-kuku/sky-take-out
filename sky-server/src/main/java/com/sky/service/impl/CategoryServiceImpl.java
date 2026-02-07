package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.MessageConstant.CATEGORY_BE_RELATED_BY_DISH;
import static com.sky.constant.MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setCreateUser(BaseContext.getCurrentId());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        category.setUpdateTime(LocalDateTime.now());
        category.setStatus(0);
        categoryMapper.insert(category);
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        List<Category> categoryList = categoryMapper.list(type);
        return categoryList;
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO.getType(), categoryPageQueryDTO.getName());

        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    /**
     * 启用禁用分类
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateUser(BaseContext.getCurrentId());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void delete(Long id) {
        Integer categoryDishCount = dishMapper.selectCountByCategoryId(id);
        if (categoryDishCount > 0) {
            throw new DeletionNotAllowedException(CATEGORY_BE_RELATED_BY_DISH);
        }
        Integer categorySetmealCount = setmealMapper.selectCountByCategoryId(id);
        if (categorySetmealCount > 0) {
            throw new DeletionNotAllowedException(CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.delete(id);
    }
}
