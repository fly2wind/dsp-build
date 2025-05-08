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

package com.sample.releaser.tasks;

import com.sample.releaser.core.ReleaserProperties;
import com.sample.releaser.core.exception.BuildUnstableException;
import com.sample.releaser.options.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.function.Function;

/**
 * 基础任务接口
 */
public interface Task extends Ordered, Function<TaskArguments, TaskResult> {

    Logger logger = LoggerFactory.getLogger(Task.class);

    String name();

    String shortName();

    String header();

    String description();

    default void setup(Options options, ReleaserProperties properties) {
        Map<String, String> envs = System.getenv();
    }

    default TaskResult apply(TaskArguments args) {
        try {
            logger.info("Running a task [{}] with arguments [{}]", name(), args);
            return runTask(args);
        } catch (BuildUnstableException ex) {
            logger.error(ex.toString());
            ex.printStackTrace();
            return null;
            //return ExecutionResult.unstable(ex);
        } catch (Exception ex) {
            logger.error(ex.toString());
            ex.printStackTrace();
            return null;
            //return ExecutionResult.failure(ex);
        }
    }

    TaskResult runTask(TaskArguments args) throws RuntimeException;
}
