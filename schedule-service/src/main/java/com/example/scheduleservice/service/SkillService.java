package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scheduleservice.entity.Skill;
import com.example.scheduleservice.entity.SkillCategory;
import com.example.scheduleservice.entity.SkillVersion;
import com.example.scheduleservice.mapper.SkillMapper;
import com.example.scheduleservice.mapper.SkillCategoryMapper;
import com.example.scheduleservice.mapper.SkillVersionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService extends ServiceImpl<SkillMapper, Skill> {

    private final SkillMapper skillMapper;
    private final SkillCategoryMapper categoryMapper;
    private final SkillVersionMapper versionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Skill> getUserSkills(Long userId) {
        QueryWrapper<Skill> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .eq("status", 1)
                .orderByDesc("priority")
                .orderByDesc("created_at");
        return skillMapper.selectList(wrapper);
    }

    public List<Skill> getSkillsByCategory(Long userId, Long categoryId) {
        QueryWrapper<Skill> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("category_id", categoryId)
                .eq("deleted", 0)
                .eq("status", 1)
                .orderByDesc("priority");
        return skillMapper.selectList(wrapper);
    }

    public Skill getSkillById(Long skillId) {
        return skillMapper.selectById(skillId);
    }

    public Skill getSkillByCode(Long userId, String skillCode) {
        QueryWrapper<Skill> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("skill_code", skillCode)
                .eq("deleted", 0)
                .last("LIMIT 1");
        return skillMapper.selectOne(wrapper);
    }

    @Transactional
    public Skill createSkill(Long userId, Map<String, Object> skillData) {
        Skill skill = new Skill();
        skill.setUserId(userId);
        skill.setSkillName((String) skillData.get("skillName"));
        skill.setSkillCode((String) skillData.get("skillCode"));
        skill.setDescription((String) skillData.get("description"));
        skill.setSkillType((String) skillData.get("skillType"));
        skill.setIcon((String) skillData.get("icon"));
        skill.setColor((String) skillData.get("color"));
        skill.setStatus(1);
        skill.setIsDefault(0);
        skill.setPriority(skillData.get("priority") != null ? (Integer) skillData.get("priority") : 0);
        
        if (skillData.containsKey("parameters")) {
            try {
                skill.setParameters(objectMapper.writeValueAsString(skillData.get("parameters")));
            } catch (Exception e) {
                log.error("Failed to serialize parameters", e);
            }
        }
        
        if (skillData.containsKey("promptTemplate")) {
            skill.setPromptTemplate((String) skillData.get("promptTemplate"));
        }
        
        if (skillData.containsKey("maxTokens")) {
            skill.setMaxTokens((Integer) skillData.get("maxTokens"));
        }
        
        if (skillData.containsKey("temperature")) {
            skill.setTemperature((Double) skillData.get("temperature"));
        }
        
        if (skillData.containsKey("modelName")) {
            skill.setModelName((String) skillData.get("modelName"));
        }
        
        if (skillData.containsKey("categoryId")) {
            skill.setCategoryId(Long.valueOf(skillData.get("categoryId").toString()));
        }
        
        skill.setCreatedAt(LocalDateTime.now());
        skill.setUpdatedAt(LocalDateTime.now());
        skillMapper.insert(skill);

        if (skill.getPromptTemplate() != null) {
            createVersion(skill.getId(), skill.getPromptTemplate(), "Initial version");
        }

        return skill;
    }

    @Transactional
    public Skill updateSkill(Long skillId, Map<String, Object> skillData) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new RuntimeException("Skill not found");
        }

        if (skillData.containsKey("skillName")) {
            skill.setSkillName((String) skillData.get("skillName"));
        }
        if (skillData.containsKey("description")) {
            skill.setDescription((String) skillData.get("description"));
        }
        if (skillData.containsKey("skillType")) {
            skill.setSkillType((String) skillData.get("skillType"));
        }
        if (skillData.containsKey("icon")) {
            skill.setIcon((String) skillData.get("icon"));
        }
        if (skillData.containsKey("color")) {
            skill.setColor((String) skillData.get("color"));
        }
        if (skillData.containsKey("status")) {
            skill.setStatus((Integer) skillData.get("status"));
        }
        if (skillData.containsKey("priority")) {
            skill.setPriority((Integer) skillData.get("priority"));
        }
        if (skillData.containsKey("parameters")) {
            try {
                skill.setParameters(objectMapper.writeValueAsString(skillData.get("parameters")));
            } catch (Exception e) {
                log.error("Failed to serialize parameters", e);
            }
        }
        if (skillData.containsKey("promptTemplate")) {
            skill.setPromptTemplate((String) skillData.get("promptTemplate"));
        }
        if (skillData.containsKey("maxTokens")) {
            skill.setMaxTokens((Integer) skillData.get("maxTokens"));
        }
        if (skillData.containsKey("temperature")) {
            skill.setTemperature((Double) skillData.get("temperature"));
        }
        if (skillData.containsKey("modelName")) {
            skill.setModelName((String) skillData.get("modelName"));
        }
        if (skillData.containsKey("categoryId")) {
            skill.setCategoryId(Long.valueOf(skillData.get("categoryId").toString()));
        }

        skill.setUpdatedAt(LocalDateTime.now());
        skillMapper.updateById(skill);

        return skill;
    }

    public void deleteSkill(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill != null) {
            skill.setDeleted(1);
            skillMapper.updateById(skill);
        }
    }

    @Transactional
    public SkillVersion createVersion(Long skillId, String promptTemplate, String releaseNotes) {
        QueryWrapper<SkillVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("skill_id", skillId)
               .eq("deleted", 0)
               .orderByDesc("version_number")
               .last("LIMIT 1");
        
        SkillVersion lastVersion = versionMapper.selectOne(wrapper);
        int newVersion = (lastVersion != null ? lastVersion.getVersionNumber() : 0) + 1;

        SkillVersion version = new SkillVersion();
        version.setSkillId(skillId);
        version.setVersionNumber(newVersion);
        version.setVersionName("v" + newVersion + ".0");
        version.setPromptTemplate(promptTemplate);
        version.setReleaseNotes(releaseNotes);
        version.setIsActive(0);
        version.setUsageCount(0);
        version.setCreatedAt(LocalDateTime.now());
        version.setUpdatedAt(LocalDateTime.now());
        versionMapper.insert(version);

        return version;
    }

    public List<SkillVersion> getSkillVersions(Long skillId) {
        QueryWrapper<SkillVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("skill_id", skillId)
                .eq("deleted", 0)
                .orderByDesc("version_number");
        return versionMapper.selectList(wrapper);
    }

    public SkillVersion getActiveVersion(Long skillId) {
        QueryWrapper<SkillVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("skill_id", skillId)
                .eq("is_active", 1)
                .eq("deleted", 0)
                .last("LIMIT 1");
        return versionMapper.selectOne(wrapper);
    }

    @Transactional
    public void activateVersion(Long versionId) {
        SkillVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new RuntimeException("Version not found");
        }

        QueryWrapper<SkillVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("skill_id", version.getSkillId())
                .eq("deleted", 0);
        List<SkillVersion> allVersions = versionMapper.selectList(wrapper);
        
        for (SkillVersion v : allVersions) {
            if (v.getIsActive() != null && v.getIsActive() == 1) {
                v.setIsActive(0);
                versionMapper.updateById(v);
            }
        }

        version.setIsActive(1);
        versionMapper.updateById(version);

        Skill skill = skillMapper.selectById(version.getSkillId());
        if (skill != null) {
            skill.setPromptTemplate(version.getPromptTemplate());
            skill.setUpdatedAt(LocalDateTime.now());
            skillMapper.updateById(skill);
        }
    }

    public void incrementVersionUsage(Long versionId) {
        SkillVersion version = versionMapper.selectById(versionId);
        if (version != null) {
            version.setUsageCount(version.getUsageCount() + 1);
            version.setUpdatedAt(LocalDateTime.now());
            versionMapper.updateById(version);
        }
    }

    public List<SkillCategory> getUserCategories(Long userId) {
        QueryWrapper<SkillCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }

    @Transactional
    public SkillCategory createCategory(Long userId, String name, String code, String description) {
        SkillCategory category = new SkillCategory();
        category.setUserId(userId);
        category.setCategoryName(name);
        category.setCategoryCode(code);
        category.setDescription(description);
        category.setSortOrder(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        SkillCategory category = categoryMapper.selectById(categoryId);
        if (category != null) {
            QueryWrapper<Skill> wrapper = new QueryWrapper<>();
            wrapper.eq("category_id", categoryId)
                    .eq("deleted", 0);
            List<Skill> skills = skillMapper.selectList(wrapper);
            for (Skill skill : skills) {
                skill.setCategoryId(null);
                skillMapper.updateById(skill);
            }
            
            category.setDeleted(1);
            categoryMapper.updateById(category);
        }
    }

    public Map<String, Object> getSkillWithDetails(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("skill", skill);
        
        if (skill.getCategoryId() != null) {
            SkillCategory category = categoryMapper.selectById(skill.getCategoryId());
            result.put("category", category);
        }
        
        List<SkillVersion> versions = getSkillVersions(skillId);
        result.put("versions", versions);
        
        SkillVersion activeVersion = getActiveVersion(skillId);
        result.put("activeVersion", activeVersion);

        return result;
    }

    public List<Skill> searchSkills(Long userId, String keyword) {
        QueryWrapper<Skill> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .and(w -> w.like("skill_name", keyword)
                        .or()
                        .like("description", keyword))
                .orderByDesc("priority");
        return skillMapper.selectList(wrapper);
    }
}
