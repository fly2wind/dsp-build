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

package com.sample.releaser.tasks.release;

import com.sample.releaser.core.Releaser;
import com.sample.releaser.tasks.Task;
import com.sample.releaser.tasks.TaskArguments;
import com.sample.releaser.tasks.TaskResult;
import org.springframework.stereotype.Component;

@Component
public class UpdatingPomsReleaseTask implements Task {

    public static final int ORDER = 10;

    private final Releaser releaser;

    public UpdatingPomsReleaseTask(Releaser releaser) {
        this.releaser = releaser;
    }

    @Override
    public String name() {
        return "updatePoms";
    }

    @Override
    public String shortName() {
        return "u";
    }

    @Override
    public String header() {
        return "UPDATING VERSIONS";
    }

    @Override
    public String description() {
        return "Update versions from the BOM";
    }

    @Override
    public TaskResult runTask(TaskArguments args) throws RuntimeException {
        //return this.releaser.updateProjectFromBom(args.project, args.projects, args.versionFromBom);
        System.out.println("---------------run job2: mvn version:set-property dsp-boot.version-1.2.1 - from remote");

        releaser.updateProjectFromBom(args.bom);

        return TaskResult.success();
    }


    @Override
    public int getOrder() {
        return UpdatingPomsReleaseTask.ORDER;
    }
}
