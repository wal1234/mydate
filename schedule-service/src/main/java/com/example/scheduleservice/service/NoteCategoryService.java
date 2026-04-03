package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.scheduleservice.entity.NoteCategory;
import com.example.scheduleservice.mapper.NoteCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCategoryService {

    private final NoteCategoryMapper categoryMapper;

    public List<NoteCategory> list(Long userId) {
        LambdaQueryWrapper<NoteCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteCategory::getDeleted, 0);
        wrapper.orderByAsc(NoteCategory::getSortOrder);
        wrapper.orderByAsc(NoteCategory::getId);
        return categoryMapper.selectList(wrapper);
    }

    public NoteCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }

    public NoteCategory create(NoteCategory category) {
        categoryMapper.insert(category);
        log.info("创建笔记分类成功, id={}", category.getId());
        return category;
    }

    public NoteCategory update(Long id, NoteCategory category) {
        category.setId(id);
        categoryMapper.updateById(category);
        log.info("更新笔记分类成功, id={}", id);
        return categoryMapper.selectById(id);
    }

    public void delete(Long id) {
        NoteCategory category = new NoteCategory();
        category.setId(id);
        category.setDeleted(1);
        categoryMapper.updateById(category);
        log.info("删除笔记分类成功, id={}", id);
    }
}
