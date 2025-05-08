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

import com.sample.releaser.core.ReleaserProperties;
import com.sample.releaser.core.buildsystem.Bom;
import com.sample.releaser.core.buildsystem.BomCompositeParser;
import com.sample.releaser.core.buildsystem.BomParser;
import com.sample.releaser.core.buildsystem.BomRepository;

import java.util.List;

/**
 * BOM更新
 */
public class ProjectBomUpdater {
    private final ReleaserProperties properties;

    private final List<BomParser> bomParsers;

    public ProjectBomUpdater(ReleaserProperties properties, List<BomParser> bomParsers) {
        this.properties = properties;
        this.bomParsers = bomParsers;
    }

    /**
     * 从BOM仓库获取版本配套清单
     *
     * @param repository BOM仓库
     * @param isRelease  是否Release
     * @return 版本配套清单
     */
    public Bom discoverFromRepository(BomRepository repository, Boolean isRelease) {
        return bomCompositeResolver().process(repository, isRelease);
    }

    private BomCompositeParser bomCompositeResolver() {
        return new BomCompositeParser(this.bomParsers);
    }
}
