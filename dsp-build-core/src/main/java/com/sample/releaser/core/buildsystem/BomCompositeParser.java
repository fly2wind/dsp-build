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

import java.util.List;

public class BomCompositeParser implements BomParser {

    private final List<BomParser> resolvers;

    public BomCompositeParser(List<BomParser> resolvers) {
        this.resolvers = resolvers;
    }

    public boolean isApplicable(BomRepository repository) {
        return resolvers.stream().anyMatch(b -> b.isApplicable(repository));
    }

    public Bom process(BomRepository repository, boolean isRelease) {
        return firstMatching(repository).process(repository, isRelease);
    }

    private BomParser firstMatching(BomRepository repository) {
        return this.resolvers.stream().filter(b -> b.isApplicable(repository)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Can't find a matching parser"));
    }

}
