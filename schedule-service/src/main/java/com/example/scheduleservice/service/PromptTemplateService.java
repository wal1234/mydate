package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scheduleservice.entity.PromptTemplate;
import com.example.scheduleservice.entity.PromptVersion;
import com.example.scheduleservice.mapper.PromptTemplateMapper;
import com.example.scheduleservice.mapper.PromptVersionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateService extends ServiceImpl<PromptTemplateMapper, PromptTemplate> {

    private final PromptTemplateMapper templateMapper;
    private final PromptVersionMapper versionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<PromptTemplate> getUserTemplates(Long userId) {
        QueryWrapper<PromptTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .orderByDesc("updated_at");
        return templateMapper.selectList(wrapper);
    }

    public PromptTemplate getTemplateById(Long templateId) {
        return templateMapper.selectById(templateId);
    }

    @Transactional
    public PromptTemplate createTemplate(Long userId, String name, String description, 
                                         String content, List<Map<String, String>> variables) {
        PromptTemplate template = new PromptTemplate();
        template.setUserId(userId);
        template.setTemplateName(name);
        template.setDescription(description);
        template.setTemplateContent(content);
        
        try {
            template.setVariables(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            log.error("Failed to serialize variables", e);
            template.setVariables("[]");
        }
        
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.insert(template);

        createVersion(template.getId(), content, "Initial version");

        return template;
    }

    @Transactional
    public PromptTemplate updateTemplate(Long templateId, String name, String description, 
                                        String content, List<Map<String, String>> variables) {
        PromptTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("Template not found");
        }

        if (name != null) {
            template.setTemplateName(name);
        }
        if (description != null) {
            template.setDescription(description);
        }
        if (content != null) {
            template.setTemplateContent(content);
        }
        if (variables != null) {
            try {
                template.setVariables(objectMapper.writeValueAsString(variables));
            } catch (Exception e) {
                log.error("Failed to serialize variables", e);
            }
        }

        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);

        return template;
    }

    public void deleteTemplate(Long templateId) {
        PromptTemplate template = templateMapper.selectById(templateId);
        if (template != null) {
            template.setDeleted(1);
            templateMapper.updateById(template);
        }
    }

    @Transactional
    public PromptVersion createVersion(Long templateId, String content, String releaseNotes) {
        QueryWrapper<PromptVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId)
               .eq("deleted", 0)
               .orderByDesc("version_number");
        wrapper.last("LIMIT 1");
        
        PromptVersion lastVersion = versionMapper.selectOne(wrapper);
        int newVersion = (lastVersion != null ? lastVersion.getVersionNumber() : 0) + 1;

        PromptVersion version = new PromptVersion();
        version.setTemplateId(templateId);
        version.setVersionNumber(newVersion);
        version.setContent(content);
        version.setReleaseNotes(releaseNotes);
        version.setIsActive(0);
        version.setCreatedAt(LocalDateTime.now());
        version.setUpdatedAt(LocalDateTime.now());
        versionMapper.insert(version);

        return version;
    }

    public List<PromptVersion> getTemplateVersions(Long templateId) {
        QueryWrapper<PromptVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId)
                .eq("deleted", 0)
                .orderByDesc("version_number");
        return versionMapper.selectList(wrapper);
    }

    public PromptVersion getActiveVersion(Long templateId) {
        QueryWrapper<PromptVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId)
                .eq("is_active", 1)
                .eq("deleted", 0)
                .last("LIMIT 1");
        return versionMapper.selectOne(wrapper);
    }

    @Transactional
    public void activateVersion(Long versionId) {
        PromptVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new RuntimeException("Version not found");
        }

        QueryWrapper<PromptVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", version.getTemplateId())
                .eq("deleted", 0);
        List<PromptVersion> allVersions = versionMapper.selectList(wrapper);
        
        for (PromptVersion v : allVersions) {
            if (v.getIsActive() != null && v.getIsActive() == 1) {
                v.setIsActive(0);
                versionMapper.updateById(v);
            }
        }

        version.setIsActive(1);
        versionMapper.updateById(version);

        PromptTemplate template = templateMapper.selectById(version.getTemplateId());
        if (template != null) {
            template.setTemplateContent(version.getContent());
            template.setUpdatedAt(LocalDateTime.now());
            templateMapper.updateById(template);
        }
    }

    @Transactional
    public void rollbackToVersion(Long versionId) {
        PromptVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new RuntimeException("Version not found");
        }

        String newContent = version.getContent();
        createVersion(version.getTemplateId(), newContent, 
                      "Rollback to version " + version.getVersionNumber());
        
        activateVersion(versionId);
    }

    public List<Map<String, String>> parseVariables(String templateContent) {
        List<Map<String, String>> variables = new ArrayList<>();
        
        if (templateContent == null || templateContent.isEmpty()) {
            return variables;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{(\\w+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(templateContent);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            boolean exists = variables.stream()
                    .anyMatch(v -> varName.equals(v.get("name")));
            
            if (!exists) {
                Map<String, String> variable = new HashMap<>();
                variable.put("name", varName);
                variable.put("type", "string");
                variable.put("required", "true");
                variables.add(variable);
            }
        }
        
        return variables;
    }
}
