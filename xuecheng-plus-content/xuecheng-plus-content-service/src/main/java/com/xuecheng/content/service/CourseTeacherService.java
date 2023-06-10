package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author: xiaocai
 * @since: 2023/06/09/22:04
 */
public interface CourseTeacherService {
    List<CourseTeacher> getCourseTeacherList(Long courseId);

    CourseTeacher saveOrUpdateCourseTeacher(CourseTeacher courseTeacher);

    void deleteCourseTeacher(Long courseId, Long teacherId);
}
