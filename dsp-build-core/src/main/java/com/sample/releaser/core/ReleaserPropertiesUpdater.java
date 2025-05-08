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

package com.sample.releaser.core;

import com.sample.releaser.core.buildsystem.BomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * 通过配置文件更新Releaser配置
 */
public class ReleaserPropertiesUpdater {
    private static final Logger logger = LoggerFactory.getLogger(ReleaserPropertiesUpdater.class);

    private static final String RELEASER_GIT_URL = "releaser.git.url";

    private static final String RELEASER_GIT_BRANCH = "releaser.git.branch";

    private static final String RELEASER_GIT_USERNAME = "releaser.git.username";

    private static final String RELEASER_GIT_PASSWORD = "releaser.git.password";

    private static final String RELEASER_REPOSITORY_COORDINATE = "releaser.repository.coordinate";
    private static final String RELEASER_REPOSITORY_BASELINE = "releaser.repository.baseline";

    public ReleaserProperties updateProperties(ReleaserProperties properties, File projectDir) {
        properties.setWorkingDir(projectDir.getAbsolutePath());

        File configFile = new File(projectDir, "releaser.properties");
        if (configFile.exists()) {
            try {
                Properties props = new Properties();
                props.load(new FileReader(configFile));

                String localRepo;
                if (System.getProperty("maven.repo.local") != null) {
                    localRepo = System.getProperty("maven.repo.local");
                } else {
                    localRepo = System.getProperty("user.dir");
                }

                // update git config
                properties.getRepository().setSource(BomRepository.SourceType.valueOf(props.getProperty("releaser.repository.source", "MAVEN")));
                properties.getRepository().setUrl(props.getProperty("releaser.repository.url", "https://mirrors.huaweicloud.com/repository/product_maven/"));
                properties.getRepository().setCoordinate(props.getProperty("releaser.repository.coordinate", "com.sample:dsp-bom"));
                properties.getRepository().setBaseline(props.getProperty("releaser.repository.baseline", "1.0.0-SNAPSHOT"));
                properties.getRepository().setLocalDirectory(localRepo);
                properties.getRepository().setUsername(props.getProperty("releaser.repository.username", null));
                properties.getRepository().setUsername(props.getProperty("releaser.repository.password", null));

                String[] resolvers = props.getProperty("releaser.repository.resolvers", "https://mirrors.huaweicloud.com/repository/maven/").split(";");
                for (String resolver: resolvers) {
                    properties.getRepository().getResolvers().add(resolver);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            logger.info("releaser.properties not found. Trusted Release feature off.");
        }

        return properties;
    }
}
