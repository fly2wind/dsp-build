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

import com.sample.releaser.core.buildsystem.Bom;
import lombok.Getter;
import org.apache.maven.model.Model;
import org.codehaus.mojo.versions.change.VersionChange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class MavenContext {
    private final Model model;

    private final Bom bom;

    private final List<VersionChange> versionChanges = new ArrayList<>();

    private final List<PropertyChange> propertyChanges = new ArrayList<>();


    private final File rootFile;

    public MavenContext(Model model, Bom bom, File rootFile) {
        this.model = model;
        this.bom = bom;
        this.rootFile = rootFile;
    }
}
