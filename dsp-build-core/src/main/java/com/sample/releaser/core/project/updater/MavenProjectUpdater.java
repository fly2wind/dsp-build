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

package com.sample.releaser.core.project.updater;

import com.sample.releaser.core.buildsystem.Bom;
import com.sample.releaser.core.project.ProjectUpdater;
import com.sample.releaser.core.project.updater.maven.MavenContext;
import com.sample.releaser.core.project.updater.maven.MavenUpdater;
import org.apache.maven.model.Model;
import org.codehaus.mojo.versions.api.PomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MavenProjectUpdater implements ProjectUpdater {

    private static final Logger logger = LoggerFactory.getLogger(MavenProjectUpdater.class);

    private final MavenUpdater mavenUpdater;

    public MavenProjectUpdater() {
        this.mavenUpdater = new MavenUpdater();
    }

    @Override
    public boolean isApplicable(File projectDir) {
        File pomFile = new File(projectDir, "pom.xml");
        return pomFile.exists();
    }

    @Override
    public void updatePoms(File projectDir, Bom bom) {
        try {
            File rootPom = new File(projectDir, "pom.xml");
            if (!rootPom.exists()) {
                logger.info("No pom.xml present, skipping!");
                return;
            }
            Model model = PomHelper.getRawModel(rootPom);
            Files.walkFileTree(projectDir.toPath(), new PomVisitor(mavenUpdater, model, bom));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * POM文件遍历
     */
    private static final class PomVisitor extends SimpleFileVisitor<Path> {
        private static final String POM_XML = "pom.xml";

        private static final String POM_FOR_DOWNLOAD_XML = "pom_for_download.xml";

        private final List<String> ignoredPomRegex = Arrays.asList("^.*\\.releaser/.*$", "^.*\\.releaserGit/.*$", "^.*\\.releaserRepo/.*$", "^.*/target/.*$");

        private final MavenUpdater mavenUpdater;

        private final Model root;

        private final Bom bom;

        private PomVisitor(MavenUpdater mavenUpdater, Model root, Bom bom) {
            this.mavenUpdater = mavenUpdater;
            this.root = root;
            this.bom = bom;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attr) {
            File file = path.toFile();
            if (POM_XML.equals(file.getName()) || POM_FOR_DOWNLOAD_XML.equals(file.getName())) {
                if (ignoredPomRegex.stream().anyMatch(file.getPath()::matches)) {
                    logger.debug("ignoring file [{}] since it's on a list of patterns to ignore", file);
                    return FileVisitResult.CONTINUE;
                }

                try {
                    MavenContext change = mavenUpdater.updateModel(root, file, bom);
                    mavenUpdater.writer(change);
                } catch (Exception e) {
                    e.printStackTrace();
                    return FileVisitResult.CONTINUE;
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
