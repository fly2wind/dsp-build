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
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForbidOverridingManagedDependencies implements EnforcerRule {

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();

        MavenProject project;
        try {
            project = (MavenProject) helper.evaluate("${project}");
            if (project == null) {
                throw new EnforcerRuleException("${project} is null");
            }

            List<Dependency> nonmanagedDeps = new ArrayList<>();
            List<Dependency> overriddenDeps = new ArrayList<>();

            // 获取当前项目管理的依赖项
            Map<String, Dependency> depMgtMap = getManagedDependenciesAsMap(project);

            // 获取当前项目定义的依赖项
            List<Dependency> dependencies = project.getOriginalModel().getDependencies();

            // 检查当前项目定义的依赖项是否在父级POM中已经定义
            for (Dependency dependency : dependencies) {
                Dependency effectiveDependency = depMgtMap.get(dependency.getManagementKey());
                if (effectiveDependency == null) {
                    nonmanagedDeps.add(dependency);
                } else {
                    if (dependency.getVersion() != null) {
                        overriddenDeps.add(dependency);
                    }
                }
            }

            if (!nonmanagedDeps.isEmpty() || !overriddenDeps.isEmpty()) {
                log.error("Forbid Overriding Managed Dependencies Rule: ");
                log.error("===========================================================");
                if (!nonmanagedDeps.isEmpty()) {
                    for (Dependency dependency : nonmanagedDeps) {
                        log.warn(String.format("建议集中被管理的依赖版本: %s:%s", dependency.getManagementKey(), dependency.getVersion()));
                    }
                }

                if (!overriddenDeps.isEmpty()) {
                    for (Dependency dependency : overriddenDeps) {
                        log.error(String.format("禁止覆盖被管理的依赖版本: %s:%s", dependency.getManagementKey(), dependency.getVersion()));
                        log.error("<dependencies>");
                        log.error("    <dependency>");
                        log.error("        <groupId>" + dependency.getGroupId() + "</groupId>");
                        log.error("        <artifactId>" + dependency.getArtifactId() + "</artifactId>");
                        log.error("        <version>" + dependency.getVersion() + "</version>  <!-- 请移除覆盖的版本定义  -->");
                        log.error("    </dependencies>");
                        log.error("</dependency>");
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

    private Map<String, Dependency> getManagedDependenciesAsMap(MavenProject project) {
        Map<String, Dependency> dependencyMap = new HashMap<>();

        if (project.getDependencyManagement() != null) {
            for (Dependency dependency : project.getDependencyManagement().getDependencies()) {
                dependencyMap.put(dependency.getManagementKey(), dependency);
            }
        }
        return dependencyMap;
    }
}
