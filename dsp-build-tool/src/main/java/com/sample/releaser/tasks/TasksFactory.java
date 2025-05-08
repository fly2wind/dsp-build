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

import com.sample.releaser.options.Options;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.LinkedList;

/**
 * 任务工厂: 根据类型和选项编排创建需要执行的任务
 */
public class TasksFactory {

    private final ApplicationContext context;

    public TasksFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * 生成release的任务链
     *
     * @return release tasks
     */
    public LinkedList<Task> release(Options options) {
        LinkedList<Task> tasks = new LinkedList<>(context.getBeansOfType(Task.class).values());
        tasks.sort(AnnotationAwareOrderComparator.INSTANCE);
        return tasks;
    }
}
