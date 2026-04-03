package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.Skill;
import com.example.scheduleservice.entity.SkillCategory;
import com.example.scheduleservice.entity.SkillVersion;
import com.example.scheduleservice.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public Result<List<Skill>> getSkills(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            
            List<Skill> skills;
            if (categoryId != null) {
                skills = skillService.getSkillsByCategory(userId, categoryId);
            } else {
                skills = skillService.getUserSkills(userId);
            }
            return Result.ok(skills);
        } catch (Exception e) {
            log.error("获取技能列表失败", e);
            return Result.fail("获取技能列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{skillId}")
    public Result<Map<String, Object>> getSkillDetails(@PathVariable Long skillId) {
        try {
            Map<String, Object> details = skillService.getSkillWithDetails(skillId);
            if (details == null) {
                return Result.fail("技能不存在");
            }
            return Result.ok(details);
        } catch (Exception e) {
            log.error("获取技能详情失败", e);
            return Result.fail("获取技能详情失败：" + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<Skill>> searchSkills(
            @RequestParam(required = false) Long userId,
            @RequestParam String keyword) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<Skill> skills = skillService.searchSkills(userId, keyword);
            return Result.ok(skills);
        } catch (Exception e) {
            log.error("搜索技能失败", e);
            return Result.fail("搜索技能失败：" + e.getMessage());
        }
    }

    @GetMapping("/by-code")
    public Result<Skill> getSkillByCode(
            @RequestParam(required = false) Long userId,
            @RequestParam String skillCode) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            Skill skill = skillService.getSkillByCode(userId, skillCode);
            if (skill == null) {
                return Result.fail("技能不存在");
            }
            return Result.ok(skill);
        } catch (Exception e) {
            log.error("获取技能失败", e);
            return Result.fail("获取技能失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Skill> createSkill(
            @RequestParam(required = false) Long userId,
            @RequestBody Map<String, Object> skillData) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            Skill skill = skillService.createSkill(userId, skillData);
            return Result.ok(skill);
        } catch (Exception e) {
            log.error("创建技能失败", e);
            return Result.fail("创建技能失败：" + e.getMessage());
        }
    }

    @PutMapping("/{skillId}")
    public Result<Skill> updateSkill(
            @PathVariable Long skillId,
            @RequestBody Map<String, Object> skillData) {
        try {
            Skill skill = skillService.updateSkill(skillId, skillData);
            return Result.ok(skill);
        } catch (Exception e) {
            log.error("更新技能失败", e);
            return Result.fail("更新技能失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{skillId}")
    public Result<Void> deleteSkill(@PathVariable Long skillId) {
        try {
            skillService.deleteSkill(skillId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("删除技能失败", e);
            return Result.fail("删除技能失败：" + e.getMessage());
        }
    }

    @GetMapping("/{skillId}/versions")
    public Result<List<SkillVersion>> getVersions(@PathVariable Long skillId) {
        try {
            List<SkillVersion> versions = skillService.getSkillVersions(skillId);
            return Result.ok(versions);
        } catch (Exception e) {
            log.error("获取版本列表失败", e);
            return Result.fail("获取版本列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{skillId}/versions/active")
    public Result<SkillVersion> getActiveVersion(@PathVariable Long skillId) {
        try {
            SkillVersion version = skillService.getActiveVersion(skillId);
            if (version == null) {
                return Result.fail("没有激活的版本");
            }
            return Result.ok(version);
        } catch (Exception e) {
            log.error("获取激活版本失败", e);
            return Result.fail("获取激活版本失败：" + e.getMessage());
        }
    }

    @PostMapping("/{skillId}/versions")
    public Result<SkillVersion> createVersion(
            @PathVariable Long skillId,
            @RequestBody Map<String, Object> request) {
        try {
            String promptTemplate = request.get("promptTemplate") != null ?
                    request.get("promptTemplate").toString() : null;
            String releaseNotes = request.get("releaseNotes") != null ?
                    request.get("releaseNotes").toString() : "";

            if (promptTemplate == null) {
                Skill skill = skillService.getSkillById(skillId);
                if (skill != null) {
                    promptTemplate = skill.getPromptTemplate();
                }
            }

            SkillVersion version = skillService.createVersion(skillId, promptTemplate, releaseNotes);
            return Result.ok(version);
        } catch (Exception e) {
            log.error("创建版本失败", e);
            return Result.fail("创建版本失败：" + e.getMessage());
        }
    }

    @PostMapping("/versions/{versionId}/activate")
    public Result<Void> activateVersion(@PathVariable Long versionId) {
        try {
            skillService.activateVersion(versionId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return Result.fail("激活版本失败：" + e.getMessage());
        }
    }

    @GetMapping("/categories")
    public Result<List<SkillCategory>> getCategories(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<SkillCategory> categories = skillService.getUserCategories(userId);
            return Result.ok(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.fail("获取分类列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/categories")
    public Result<SkillCategory> createCategory(
            @RequestParam(required = false) Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            String name = request.get("name").toString();
            String code = request.get("code").toString();
            String description = request.get("description") != null ?
                    request.get("description").toString() : "";
            
            SkillCategory category = skillService.createCategory(userId, name, code, description);
            return Result.ok(category);
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return Result.fail("创建分类失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        try {
            skillService.deleteCategory(categoryId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return Result.fail("删除分类失败：" + e.getMessage());
        }
    }
}
