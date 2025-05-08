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

package com.sample.releaser.core.buildsystem;

import java.util.HashMap;
import java.util.Map;

/**
 * 版本配套信息
 */
public class Bom {
    private final boolean isRelease;

    private final Map<String, String> parameters = new HashMap<>();

    /**
     * 依赖属性信息
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * 依赖版本信息
     * MAVEN:
     * key:  'groupId:artifactId:packaging'
     * value: version
     */
    private final Map<String, String> versions = new HashMap<>();


    public Bom(boolean isRelease) {
        this.isRelease = isRelease;
    }

    public boolean isRelease() {
        return isRelease;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean hasProperty(Object key) {
        return properties.containsKey(key);
    }

    public String getProperty(Object key) {
        return properties.get(key);
    }

    public void putProperty(Object key, Object value) {
        properties.put(String.valueOf(key), String.valueOf(value));
    }

    public Map<String, String> getVersions() {
        return versions;
    }

    public String getCMCVersion() {
        if (parameters.containsKey("cmc_version")) {
            return parameters.get("cmc_version");
        }
        return null;
    }
}
