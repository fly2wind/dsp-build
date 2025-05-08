/*
 * Copyright 2022 [name of copyright owner]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.rules;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForbidOverridingManagedPlugins implements EnforcerRule {

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();

        MavenProject project;
        try {
            project = (MavenProject) helper.evaluate("${project}");
            if (project == null) {
                throw new EnforcerRuleException("${project} is null");
            }

            List<Plugin> nonmanagedPlugins = new ArrayList<>();
            List<Plugin> overriddenPlugins = new ArrayList<>();

            // 获取当前项目管理的依赖项
            Map<String, Plugin> pluginMgtMap = getManagedPluginsAsMap(project);

            // 获取当前项目定义的依赖项
            List<Plugin> plugins = project.getOriginalModel().getBuild().getPlugins();

            // 检查当前项目定义的依赖项是否在父级POM中已经定义
            for (Plugin plugin : plugins) {
                Plugin effectivePlugin = pluginMgtMap.get(plugin.getKey());
                if (effectivePlugin == null) {
                    nonmanagedPlugins.add(plugin);
                } else {
                    if (plugin.getVersion() != null) {
                        overriddenPlugins.add(plugin);
                    }
                }
            }

            if (!nonmanagedPlugins.isEmpty() || !overriddenPlugins.isEmpty()) {
                log.error("Forbid Overriding Managed Plugins Rule: ");
                log.error("===========================================================");
                if (!nonmanagedPlugins.isEmpty()) {
                    for (Plugin plugin : nonmanagedPlugins) {
                        log.warn(String.format("建议集中被管理的插件版本: %s:%s", plugin.getKey(), plugin.getVersion()));
                    }
                }

                if (!overriddenPlugins.isEmpty()) {
                    for (Plugin plugin : overriddenPlugins) {
                        log.error(String.format("禁止覆盖被管理的插件版本: %s:%s", plugin.getKey(), plugin.getVersion()));
                        log.error("<plugins>");
                        log.error("    <plugin>");
                        log.error("        <groupId>" + plugin.getGroupId() + "</groupId>");
                        log.error("        <artifactId>" + plugin.getArtifactId() + "</artifactId>");
                        log.error("        <version>" + plugin.getVersion() + "</version>  <!-- 请移除覆盖的版本定义  -->");
                        log.error("    </plugin>");
                        log.error("</plugins>");
                    }

                    throw new EnforcerRuleException("  *  Failing because of overridden managed dependencies");
                }

            }
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Cannot resolve expression: " + e.getCause(), e);
        }
    }

    public boolean isCacheable() {
        return false;
    }

    public boolean isResultValid(EnforcerRule enforcerRule) {
        return false;
    }

    public String getCacheId() {
        return "0";
    }

    private Map<String, Plugin> getManagedPluginsAsMap(MavenProject project) {
        Map<String, Plugin> pluginMap = new HashMap<>();

        if (project.getPluginManagement() != null) {
            for (Plugin plugin : project.getPluginManagement().getPlugins()) {
                pluginMap.put(plugin.getKey(), plugin);
            }
        }
        return pluginMap;
    }
}
