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

package com.sample.releaser.core.project.updater.maven;

import com.ctc.wstx.stax.WstxInputFactory;
import com.sample.releaser.core.buildsystem.Bom;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.codehaus.mojo.versions.api.PomHelper;
import org.codehaus.mojo.versions.change.VersionChange;
import org.codehaus.mojo.versions.change.VersionChanger;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;
import org.codehaus.stax2.XMLInputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MavenUpdater {

    private static final Logger logger = LoggerFactory.getLogger(MavenUpdater.class);

    public MavenContext updateModel(Model rootPom, File pom, Bom bom) {
        try {
            Model model = PomHelper.getRawModel(pom);

            MavenContext context = new MavenContext(rootPom, bom, pom);

            updateParent(context, model, bom);
            updateDependencies(context, model, bom);
            updateProperties(context, model, bom);

            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateParent(MavenContext context, Model pom, Bom bom) {
        //如果没有父级，则跳过
        if (pom.getParent() == null || StringUtils.isBlank(pom.getParent().getVersion())) {
            logger.info("can't set the value for parent... will skip");
            return;
        }

        String groupId = PomHelper.getGroupId(pom);
        String artifactId = pom.getParent().getArtifactId();
        String oldVersion = pom.getParent().getVersion();

        logger.info("search a version [{}:{}]:{}", groupId, artifactId, oldVersion);

        //String newVersion = bom.getCMCVersion();
        String newVersion = artifactId.equals(context.getModel().getArtifactId()) ? bom.getCMCVersion() : null;

        if (StringUtils.isBlank(newVersion)) {
            if (StringUtils.isNotBlank(pom.getParent().getRelativePath())) {
                newVersion = pom.getParent().getVersion();
            } else {
                logger.warn("there is no info on the [{}:{}] version", groupId, artifactId);
                return;
            }
        }


        if (oldVersion.equals(newVersion)) {
            logger.info("not change");
        } else {
            logger.info("change a version [{}:{}]:{} to {}", groupId, artifactId, oldVersion, newVersion);
            context.getVersionChanges().add(new VersionChange(groupId, artifactId, oldVersion, newVersion));
            //addVersionChange(groupId, artifactId, oldVersion, version);
        }


        logger.debug("found version is [{}]", oldVersion);

    }

    private void updateDependencies(MavenContext context, Model pom, Bom bom) {
        if (pom.getDependencies() == null) {
            logger.debug("can't update dependencies... will skip");
            return;
        }

        pom.getDependencies().forEach(dep -> {
            if (bom.getVersions().containsKey(dep.getManagementKey())) {
                String oldVersion = dep.getVersion();

                if (StringUtils.isNotBlank(oldVersion)) {
                    //如果是变量
                    if (oldVersion.startsWith("${")) {
                        String versionKey = oldVersion.replaceAll("\\$\\{", "").replaceAll("\\}", "");
                        if(versionKey.contains("project.") || versionKey.contains("parent.")) {
                            logger.debug("Skip updating properties: " + versionKey);
                            return;
                        }
                        if (bom.hasProperty(versionKey)) {
                            logger.info("Will updating properties: " + versionKey);
                            return;
                        }
                    }

                    String newVersion = bom.getVersions().get(dep.getManagementKey());

                    if (!oldVersion.equals(newVersion)) {
                        context.getVersionChanges().add(new VersionChange(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), newVersion));
                    }
                }
            }
        });

    }

    private void updateProperties(MavenContext context, Model model, Bom bom) {
        if (model.getProperties() == null) {
            logger.info("can't update properties... will skip");
            return;
        }

        model.getProperties().forEach((key, oldValue) -> {
            if (bom.hasProperty(key)) {
                String newValue = bom.getProperty(key);

                if (!oldValue.equals(newValue)) {
                    context.getPropertyChanges().add(new PropertyChange((String) key, (String) oldValue, newValue));
                }
            }
        });

    }

    public void writer(MavenContext context) throws Exception {
        StringBuilder input = PomHelper.readXmlFile(context.getRootFile());

        MavenChangerFactory factory = new MavenChangerFactory(context.getModel(), createModifiedPomXER(input), logger);

        VersionChanger versionChanger = factory.newVersionChanger(true, true, true, true);
        for (VersionChange versionChange : context.getVersionChanges()) {
            versionChanger.apply(versionChange);
        }
        PropertyChanger propertychanger = factory.newPropertyChanger();
        for (PropertyChange propertyChange : context.getPropertyChanges()) {
            propertychanger.apply(propertyChange);
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(context.getRootFile().toPath()), StandardCharsets.UTF_8))) {
            bw.write(input.toString());
        }
        logger.info("Flushed changes to the pom file [{}]", context.getRootFile());
    }

    private ModifiedPomXMLEventReader createModifiedPomXER(StringBuilder input) {
        ModifiedPomXMLEventReader newPom = null;
        try {
            //不使用SPI加载, 因为MAVEN EXTENSION的ClassLoader冲突
            WstxInputFactory inputFactory = new WstxInputFactory();
            inputFactory.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE);
            newPom = new ModifiedPomXMLEventReader(input, inputFactory, null);
        } catch (XMLStreamException e) {
            logger.error("Exception occurred while trying to parse pom", e);
        }
        return newPom;
    }


}

