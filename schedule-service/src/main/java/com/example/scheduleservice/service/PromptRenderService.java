package com.example.scheduleservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PromptRenderService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile("\\{\\{#if\\s+(\\w+)\\}\\}(.*?)\\{\\{/if\\}\\}", Pattern.DOTALL);
    private static final Pattern LOOP_PATTERN = Pattern.compile("\\{\\{#each\\s+(\\w+)\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);

    public String render(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        String result = template;

        result = renderConditionals(result, variables);
        
        result = renderLoops(result, variables);

        Matcher matcher = VARIABLE_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        result = sb.toString();

        return result;
    }

    private String renderConditionals(String template, Map<String, Object> variables) {
        Matcher matcher = CONDITIONAL_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String content = matcher.group(2);
            
            Object value = variables.get(varName);
            boolean conditionMet = value != null && !value.toString().isEmpty();
            
            String replacement = conditionMet ? content : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String renderLoops(String template, Map<String, Object> variables) {
        Matcher matcher = LOOP_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String loopContent = matcher.group(2);
            
            Object value = variables.get(varName);
            StringBuilder replacement = new StringBuilder();
            
            if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        String rendered = render(loopContent, itemMap);
                        replacement.append(rendered);
                    } else {
                        String rendered = loopContent.replace("{{this}}", item.toString());
                        replacement.append(rendered);
                    }
                }
            }
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public Map<String, Object> extractVariables(String template) {
        Map<String, Object> variables = new HashMap<>();
        
        if (template == null || template.isEmpty()) {
            return variables;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String varName = matcher.group(1);
            variables.put(varName, null);
        }
        
        return variables;
    }

    public boolean validateTemplate(String template) {
        if (template == null || template.isEmpty()) {
            return true;
        }

        int openCount = 0;
        for (char c : template.toCharArray()) {
            if (c == '{') openCount++;
            else if (c == '}') openCount--;
            
            if (openCount < 0) return false;
        }
        
        return openCount == 0;
    }

    public String addVariable(String template, String varName, Object defaultValue) {
        if (!template.contains("{{" + varName + "}}")) {
            template += "\n\n{{" + varName + "}}";
        }
        return template;
    }

    public String removeVariable(String template, String varName) {
        template = template.replace("{{" + varName + "}}", "");
        return template;
    }

    public Map<String, String> validateVariables(String template, Map<String, Object> provided) {
        Map<String, String> errors = new HashMap<>();
        Map<String, Object> required = extractVariables(template);
        
        for (String varName : required.keySet()) {
            if (!provided.containsKey(varName) || provided.get(varName) == null) {
                errors.put(varName, "Required variable '" + varName + "' is missing");
            }
        }
        
        return errors;
    }
}
