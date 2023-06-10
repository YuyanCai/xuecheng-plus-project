package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/14 12:11
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count + 1;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null) {
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //确定排序字段，找到它的同级节点个数，排序字段就是个数加1  select count(1) from teachplan where course_id=117 and parentid=268
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, parentid);
            teachplan.setOrderby(teachplanCount);
            teachplanMapper.insert(teachplan);

        } else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数复制到teachplan
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }

    @Override
    public void delTeachplan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer grade = teachplan.getGrade();
        if (grade == 1) {
//            大章节
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getParentid, teachplanId);
            Integer count = teachplanMapper.selectCount(wrapper);
            if (count > 0) {
                throw new XueChengPlusException("课程计划信息还有子级信息，无法操作");
            }
            teachplanMapper.deleteById(teachplanId);
        } else if (grade == 2) {
//            小章节
            teachplanMapper.deleteById(teachplanId);
            LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);
            teachplanMediaMapper.delete(wrapper);
        }
    }

    @Override
    public void orderByUpOrDown(String moveType, Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer grade = teachplan.getGrade();

        if (moveType.equals("moveup") || moveType.equals("movedown") && (grade == 1 || grade == 2)) {
            changePostition(teachplan, moveType);
        }

    }

    private void changePostition(Teachplan teachplan, String moveType) {

//        定位上一个标题或下一个标题
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        int offset = moveType.equals("moveup") ? -1 : 1;

        wrapper.eq(Teachplan::getOrderby, teachplan.getOrderby() + offset)
                .eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getGrade, teachplan.getGrade());
        Teachplan beforeTeachPlan = teachplanMapper.selectOne(wrapper);
        if (beforeTeachPlan == null) {
            throw new XueChengPlusException("不能移动了");
        }
        beforeTeachPlan.setOrderby(teachplan.getOrderby());
        teachplanMapper.updateById(beforeTeachPlan);

//        定位当前标题
        Teachplan currentTachPlan = teachplanMapper.selectById(teachplan.getId());
        currentTachPlan.setOrderby(teachplan.getOrderby() + offset);
        teachplanMapper.updateById(currentTachPlan);

    }


}
