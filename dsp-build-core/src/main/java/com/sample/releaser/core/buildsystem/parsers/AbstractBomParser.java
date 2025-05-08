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

package com.sample.releaser.core.buildsystem.parsers;

import com.sample.releaser.core.buildsystem.Bom;
import com.sample.releaser.core.buildsystem.BomParser;
import com.sample.releaser.core.buildsystem.BomRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.mojo.versions.api.PomHelper;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public abstract class AbstractBomParser implements BomParser {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractBomParser.class);

    private RepositorySystem system;

    public AbstractBomParser() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                exception.printStackTrace();
                logger.error("{} for {} wtih {}", type, impl, exception);
            }
        });
        this.system = locator.getService(RepositorySystem.class);
    }

    protected File resolve(BomRepository repository) throws ArtifactResolutionException {
        if (repository.isSourceMaven()) {
            String coordinate = repository.getCoordinate() + ":" + "pom" + ":" + repository.getBaseline();

            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(new DefaultArtifact(coordinate));
            request.addRepository(new RemoteRepository.Builder("bom", "default", repository.getUrl()).build());

            ArtifactResult result = system.resolveArtifact(createSession(repository), request);
            if (result.isResolved()) {
                return result.getArtifact().getFile();
            } else {
                return null;
            }
        }
        return null;
    }

    protected Bom parserBom(File bomFile, BomRepository repository, boolean isRelease) {
        Bom bom = new Bom(isRelease);
        try {
            Model model = PomHelper.getRawModel(bomFile);

            // 解析参数
            // ----------------------------------------------------------------------------------------------------
            if (model.getProfiles() != null) {
                String env;
                if (bom.isRelease()) {
                    env = "release";
                } else {
                    env = "develop";
                }

                Profile profile = model.getProfiles().stream().filter(p -> env.equals(p.getId())).findFirst()
                        .orElseThrow(() -> new IllegalStateException("not found parameters"));
                if (profile.getProperties() != null) {
                    logger.info("Finding build parameter of materials......");
                    profile.getProperties().forEach((k, v) -> {
                        logger.info("    Found parameter {}", k);
                        logger.info("        value is {}", v);
                        bom.getParameters().put((String) k, (String) v);
                    });
                }
            }

            // 解析属性
            // ----------------------------------------------------------------------------------------------------
            if (model.getProperties() != null) {
                logger.info("Finding build property of materials......");
                model.getProperties().forEach((k, v) -> bom.getProperties().put(String.valueOf(k), String.valueOf(v)));
                bom.getProperties().forEach((k, v) -> {
                    if (v.startsWith("${")) {
                        v = v.replaceAll("\\$\\{", "").replaceAll("}", "");
                        if (bom.getProperties().containsKey(v)) {
                            bom.getProperties().put(k, bom.getProperties().get(v));
                        }
                    }
                });
                logger.info("    Found {} properties", model.getProperties().size());
            }

            ArtifactDescriptorResult descriptor = system.readArtifactDescriptor(createSession(repository), createRequest(model.toString(), repository));

            // 解析依赖
            // ----------------------------------------------------------------------------------------------------
            if (descriptor.getDependencies() != null) {
                logger.info("Finding dependency of materials......");
                descriptor.getDependencies().forEach(dep -> bom.getVersions().put(getManagementKey(dep.getArtifact()), dep.getArtifact().getVersion()));
                logger.info("    Found {}  dependencies", descriptor.getDependencies().size());
            }

            // 解析被管理的依赖（覆盖上面）
            // ----------------------------------------------------------------------------------------------------
            if (descriptor.getManagedDependencies() != null) {
                logger.info("Finding managed dependency of materials......");
                descriptor.getManagedDependencies().forEach(dep -> bom.getVersions().put(getManagementKey(dep.getArtifact()), dep.getArtifact().getVersion()));
                logger.info("    Found {}  managed dependencies", descriptor.getManagedDependencies().size());
            }
        } catch (IOException | ArtifactDescriptorException e) {
            logger.error("BOM not found or parser failed", e);
        }
        return bom;
    }

    private RepositorySystemSession createSession(BomRepository repository) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(new File(repository.getLocalDirectory(), ".releaser/repo"));
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
        return session;
    }

    private ArtifactDescriptorRequest createRequest(String coords, BomRepository repository) {
        ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();
        request.setArtifact(new DefaultArtifact(coords));
        for (String resolver : repository.getResolvers()) {
            request.addRepository(new RemoteRepository.Builder(String.valueOf(resolver.hashCode()), "default", resolver).build());
        }
        return request;
    }

    private String getManagementKey(Artifact artifact) {
        return String.format("%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension());
    }
}
