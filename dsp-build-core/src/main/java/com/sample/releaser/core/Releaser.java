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

import com.sample.releaser.core.buildsystem.Bom;
import com.sample.releaser.core.buildsystem.BomParser;
import com.sample.releaser.core.buildsystem.BomRepository;
import com.sample.releaser.core.buildsystem.parsers.GitBomParser;
import com.sample.releaser.core.buildsystem.parsers.MavenBomParser;
import com.sample.releaser.core.project.ProjectBomUpdater;
import com.sample.releaser.core.project.ProjectUpdater;
import com.sample.releaser.core.project.updater.ProjectUpdaterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Releaser {
    private static final Logger logger = LoggerFactory.getLogger(Releaser.class);

    private final ReleaserProperties properties;


    private final ProjectBomUpdater projectBomUpdater;

    public Releaser(ReleaserProperties properties) {
        List<BomParser> bomParsers = new ArrayList<>();
        bomParsers.add(new GitBomParser());
        bomParsers.add(new MavenBomParser());

        this.properties = properties;
        this.projectBomUpdater = new ProjectBomUpdater(properties, bomParsers);
    }

    public Bom discoverFromRepository(BomRepository repository, boolean isRelease) {
        return projectBomUpdater.discoverFromRepository(repository, isRelease);
    }

    public void updateProjectFromBom(Bom bom) {
        logger.info("Start update the project with CMC versions [{}]", bom.getCMCVersion());

        File baseDir = new File(properties.getWorkingDir());

        ProjectUpdaterFactory factory = new ProjectUpdaterFactory();
        ProjectUpdater projectUpdater = factory.create(baseDir);
        projectUpdater.updatePoms(baseDir, bom);

    }
}
