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

package com.sample.releaser;

import com.sample.releaser.core.Releaser;
import com.sample.releaser.core.ReleaserProperties;
import com.sample.releaser.options.Options;
import com.sample.releaser.tasks.Task;
import com.sample.releaser.tasks.TaskResult;
import com.sample.releaser.tasks.TasksFactory;
import com.sample.releaser.tasks.TasksRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;

@Configuration
public class ReleaserConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "releaser")
    ReleaserProperties releaserProperties() {
        return new ReleaserProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    ReleaserGoal releaserRunner(ReleaserProperties properties, TasksFactory tasksFactory, TasksRunner tasksRunner) {
        return new ReleaserGoalProvider(properties, tasksFactory, tasksRunner);
    }

    @Bean
    @ConditionalOnMissingBean
    Releaser releaser(ReleaserProperties releaserProperties) {
        return new Releaser(releaserProperties);
    }

    /**
     * Cli执行Goal的默认提供者: 根据选项编排需要执行的任务
     */
    public static class ReleaserGoalProvider implements ReleaserGoal {
        private final ReleaserProperties properties;

        private final TasksFactory tasksFactory;

        private final TasksRunner tasksRunner;

        public ReleaserGoalProvider(ReleaserProperties properties, TasksFactory tasksFactory, TasksRunner tasksRunner) {
            this.properties = properties;
            this.tasksFactory = tasksFactory;
            this.tasksRunner = tasksRunner;
        }

        @Override
        public TaskResult release() {
            return release(new Options());
        }

        @Override
        public TaskResult release(Options options) {
            LinkedList<Task> tasks = tasksFactory.release(options);
            tasksRunner.runReleaseTasks(options, tasks);
            return TaskResult.success();
        }
    }
}
