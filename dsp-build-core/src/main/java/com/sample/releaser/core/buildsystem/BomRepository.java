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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class BomRepository implements Serializable {

    private SourceType source;

    /**
     * 版本仓库地址
     * - Git类型:   'git仓库的地址'
     * - MAVEN类型: 'maven仓库地址'
     */
    private String url = "https://mirrors.huaweicloud.com/repository/maven/";
    /**
     * 版本仓库的坐标
     * - Git类型:   'git仓库的地址'
     * - MAVEN类型: 'groupId:artifactId'
     */
    private String coordinate;

    /**
     * 版本仓库的基线
     * - Git类型:   'git的分支'
     * - MAVEN类型: '版本号'
     */
    private String baseline;

    /**
     * 版本仓库的本地缓存目录
     */
    private String localDirectory;

    /**
     * 版本仓库的远程库地址，用于解决依赖版本信息
     */
    private List<String> resolvers = new ArrayList<>();

    /**
     * 版本仓库用户名
     */
    private String username;

    /**
     * 版本仓库密码
     */
    private String password;


    public boolean isSourceGit() {
        return SourceType.GIT.equals(source);
    }

    public boolean isSourceMaven() {
        return SourceType.MAVEN.equals(source);
    }


    public void addResolver(String resolver) {
        if(resolvers == null) {
            resolvers = new ArrayList<>();
        }
        resolvers.add(resolver);
    }

    public enum SourceType {
        GIT, MAVEN
    }
}
