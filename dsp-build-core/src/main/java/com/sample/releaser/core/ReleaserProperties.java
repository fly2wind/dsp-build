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
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ReleaserProperties {

    private String workingDir = System.getProperty("user.dir");

    private BomRepository repository = new BomRepository();

    private Command command = new Command();

    @Getter
    @Setter
    public static class Command implements Serializable {
        private String build = "mvn clean install -B {{systemProps}}";

        private String deploy = "mvn deploy -DskipTests {{systemProps}}";
    }
}

