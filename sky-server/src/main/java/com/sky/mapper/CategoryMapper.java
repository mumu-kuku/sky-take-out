package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 新增分类
     * @param category
     */
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user)" +
            "values(#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /**
     * 分类查询
     * @param type
     * @return
     */
    List<Category> list(Integer type);

    /**
     * 分类分页查询
     * @param type
     * @param name
     * @return
     */
    Page<Category> pageQuery(Integer type, String name);

    void update(Category category);

    /**
     * 根据 id 删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void delete(Long id);
}
