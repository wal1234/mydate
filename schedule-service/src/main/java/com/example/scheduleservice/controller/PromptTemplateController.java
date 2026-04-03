package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.PromptTemplate;
import com.example.scheduleservice.entity.PromptVersion;
import com.example.scheduleservice.service.PromptRenderService;
import com.example.scheduleservice.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/prompt-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;
    private final PromptRenderService promptRenderService;

    @GetMapping
    public Result<List<PromptTemplate>> getTemplates(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<PromptTemplate> templates = promptTemplateService.getUserTemplates(userId);
            return Result.ok(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.fail("获取模板列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{templateId}")
    public Result<PromptTemplate> getTemplate(@PathVariable Long templateId) {
        try {
            PromptTemplate template = promptTemplateService.getTemplateById(templateId);
            if (template == null) {
                return Result.fail("模板不存在");
            }
            return Result.ok(template);
        } catch (Exception e) {
            log.error("获取模板失败", e);
            return Result.fail("获取模板失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<PromptTemplate> createTemplate(@RequestBody Map<String, Object> request) {
        try {
            Long userId = request.get("userId") != null ?
                    Long.valueOf(request.get("userId").toString()) : 1L;
            String name = request.get("name").toString();
            String description = request.get("description") != null ?
                    request.get("description").toString() : "";
            String content = request.get("content").toString();

            List<Map<String, String>> variables = promptTemplateService.parseVariables(content);

            PromptTemplate template = promptTemplateService.createTemplate(
                    userId, name, description, content, variables);

            return Result.ok(template);
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return Result.fail("创建模板失败：" + e.getMessage());
        }
    }

    @PutMapping("/{templateId}")
    public Result<PromptTemplate> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> request) {
        try {
            String name = request.get("name") != null ?
                    request.get("name").toString() : null;
            String description = request.get("description") != null ?
                    request.get("description").toString() : null;
            String content = request.get("content") != null ?
                    request.get("content").toString() : null;

            List<Map<String, String>> variables = null;
            if (content != null) {
                variables = promptTemplateService.parseVariables(content);
            }

            PromptTemplate template = promptTemplateService.updateTemplate(
                    templateId, name, description, content, variables);

            return Result.ok(template);
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return Result.fail("更新模板失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{templateId}")
    public Result<Void> deleteTemplate(@PathVariable Long templateId) {
        try {
            promptTemplateService.deleteTemplate(templateId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return Result.fail("删除模板失败：" + e.getMessage());
        }
    }

    @GetMapping("/{templateId}/versions")
    public Result<List<PromptVersion>> getVersions(@PathVariable Long templateId) {
        try {
            List<PromptVersion> versions = promptTemplateService.getTemplateVersions(templateId);
            return Result.ok(versions);
        } catch (Exception e) {
            log.error("获取版本列表失败", e);
            return Result.fail("获取版本列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{templateId}/versions/active")
    public Result<PromptVersion> getActiveVersion(@PathVariable Long templateId) {
        try {
            PromptVersion version = promptTemplateService.getActiveVersion(templateId);
            if (version == null) {
                return Result.fail("没有激活的版本");
            }
            return Result.ok(version);
        } catch (Exception e) {
            log.error("获取激活版本失败", e);
            return Result.fail("获取激活版本失败：" + e.getMessage());
        }
    }

    @PostMapping("/{templateId}/versions")
    public Result<PromptVersion> createVersion(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> request) {
        try {
            String content = request.get("content") != null ?
                    request.get("content").toString() : null;
            String releaseNotes = request.get("releaseNotes") != null ?
                    request.get("releaseNotes").toString() : "";

            if (content == null) {
                PromptTemplate template = promptTemplateService.getTemplateById(templateId);
                if (template != null) {
                    content = template.getTemplateContent();
                }
            }

            PromptVersion version = promptTemplateService.createVersion(
                    templateId, content, releaseNotes);

            return Result.ok(version);
        } catch (Exception e) {
            log.error("创建版本失败", e);
            return Result.fail("创建版本失败：" + e.getMessage());
        }
    }

    @PostMapping("/versions/{versionId}/activate")
    public Result<Void> activateVersion(@PathVariable Long versionId) {
        try {
            promptTemplateService.activateVersion(versionId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return Result.fail("激活版本失败：" + e.getMessage());
        }
    }

    @PostMapping("/versions/{versionId}/rollback")
    public Result<Void> rollbackVersion(@PathVariable Long versionId) {
        try {
            promptTemplateService.rollbackToVersion(versionId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("回滚版本失败", e);
            return Result.fail("回滚版本失败：" + e.getMessage());
        }
    }

    @PostMapping("/render")
    public Result<String> renderTemplate(@RequestBody Map<String, Object> request) {
        try {
            String template = request.get("template").toString();
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.get("variables");

            if (!promptRenderService.validateTemplate(template)) {
                return Result.fail("模板语法错误：花括号不匹配");
            }

            Map<String, String> errors = promptRenderService.validateVariables(template, variables);
            if (!errors.isEmpty()) {
                return Result.fail("缺少必需的变量：" + errors.keySet().toString());
            }

            String rendered = promptRenderService.render(template, variables);
            return Result.ok(rendered);
        } catch (Exception e) {
            log.error("渲染模板失败", e);
            return Result.fail("渲染模板失败：" + e.getMessage());
        }
    }
}
