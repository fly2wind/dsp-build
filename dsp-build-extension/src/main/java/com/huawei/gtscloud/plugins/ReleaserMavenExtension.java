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

package com.sample.plugins;

import com.sample.releaser.core.Releaser;
import com.sample.releaser.core.ReleaserProperties;
import com.sample.releaser.core.ReleaserPropertiesUpdater;
import com.sample.releaser.core.buildsystem.Bom;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

@Named("releaserMavenExtension")
@Singleton
public class ReleaserMavenExtension extends AbstractMavenLifecycleParticipant implements LogEnabled {

    protected static final String DSP_VERSION_PROPERTIES_FILENAME = "version.properties";

    @Inject
    private Logger logger;

    @Inject
    private RuntimeInformation runtime;

    private ReleaserProperties releaserProperties;

    private final ReleaserPropertiesUpdater releaserPropertiesUpdater;
    private Releaser releaser;

    public ReleaserMavenExtension() {
        this.releaserProperties = new ReleaserProperties();
        this.releaserPropertiesUpdater = new ReleaserPropertiesUpdater();
    }

    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        System.out.println("999999999999999999999999999999999999");

        session.getCurrentProject().getDependencies().forEach(d -> System.out.println(d.getManagementKey() + d.getVersion()));
        super.afterProjectsRead(session);
    }

    public void afterSessionStart(MavenSession session) throws MavenExecutionException {
        System.out.println("33333333");
        super.afterSessionStart(session);
        try {
            releaserProperties = releaserPropertiesUpdater.updateProperties(releaserProperties, new File(session.getExecutionRootDirectory()));
            // 设置远程仓库
            List<ArtifactRepository> artifactRepositories = session.getRequest().getRemoteRepositories();
            artifactRepositories.forEach(artifactRepository -> {
                if ("mrm-maven-plugin".equals(artifactRepository.getId())) {
                    if (releaserProperties.getRepository().isSourceMaven()) {
                        releaserProperties.getRepository().setUrl(artifactRepository.getUrl());
                    }
                }
                releaserProperties.getRepository().getResolvers().add(artifactRepository.getUrl());
            });

            // 加载本地参数文件
            File envFile = new File(session.getExecutionRootDirectory(), "env.properties");
            if(envFile.exists()) {
                session.getSystemProperties().load(new FileReader(envFile));
            }

            // 更新POM
            releaser = new Releaser(releaserProperties);
            Bom bom = releaser.discoverFromRepository(releaserProperties.getRepository(), false);
            releaser.updateProjectFromBom(bom);
        } catch (Exception e) {
            logger.error("Missing build configuration file in root directory or wrong BOM repository address");
            throw new MavenExecutionException("Exception", e);
        }
    }

    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        logger.info("清理目录");
        File releaserDir = new File(releaserProperties.getWorkingDir(), ".releaser");
        if (releaserDir.exists()) {
            try {
                FileUtils.deleteDirectory(releaserDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}
