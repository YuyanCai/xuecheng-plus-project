package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/12 14:49
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoriesList = courseCategoryMapper.selectListCourseCategoryTreeDto();
        List<CourseCategoryTreeDto> collect = courseCategoriesList.stream()
                .filter(courseCategory -> courseCategory.getParentid().equals(id))
                .map(courseCategory -> {
                    courseCategory.setChildrenTreeNodes(getChildens(courseCategory, courseCategoriesList));
                    return courseCategory;
                }).sorted((menu1, menu2) -> {
                    return menu1.getOrderby() - menu2.getOrderby();
                }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 递归三要素：
     * 1.确定递归的参数和返回值
     * 2.确定递归的结束条件
     * 3.确定递归的逻辑
     *
     * @param level1CourseCategory
     * @param courseCategoriesList
     * @return
     */
    private List<CourseCategoryTreeDto> getChildens(CourseCategoryTreeDto level1CourseCategory, List<CourseCategoryTreeDto> courseCategoriesList) {
        List<CourseCategoryTreeDto> children = courseCategoriesList.stream()
                .filter(categoryEntity -> {
                    return categoryEntity.getParentid().equals(level1CourseCategory.getId());
                }).map((categoryEntity) -> {
                    categoryEntity.setChildrenTreeNodes(getChildens(categoryEntity, courseCategoriesList));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    return menu1.getOrderby() - menu2.getOrderby();
                }).collect(Collectors.toList());
        if (children.isEmpty()) {
            return null; // 如果没有子分类，将 children 设置为 null
        } else {
            return children;
        }
    }
}
