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
import com.sample.releaser.core.buildsystem.BomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * MAVEN仓库的BOM发现处理器
 */
public class MavenBomParser extends AbstractBomParser {

    private static final Logger logger = LoggerFactory.getLogger(MavenBomParser.class);

    @Override
    public boolean isApplicable(BomRepository repository) {
        return repository.isSourceMaven();
    }

    @Override
    public Bom process(BomRepository repository, boolean isRelease) {
        try {
            File pomFile = resolve(repository);

            if (pomFile != null) {
                return parserBom(pomFile, repository, isRelease);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
