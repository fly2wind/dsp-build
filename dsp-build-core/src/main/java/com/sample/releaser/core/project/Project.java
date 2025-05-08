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

package com.sample.releaser.core.project;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * 工程项目
 */
@Getter
@Setter
public class Project {
    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目版本
     */
    private String version;


    /**
     * 项目目录
     */
    private File directory;

    /**
     * 项目类型
     */
    private BuildType type;


    public Project(String name, String version, BuildType type, File directory) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.directory = directory;
    }

    public boolean isMavenProject() {
        return BuildType.MAVEN.equals(type);
    }

    public enum BuildType {
        MAVEN,
        GRADLE,
        NPM
    }
}
