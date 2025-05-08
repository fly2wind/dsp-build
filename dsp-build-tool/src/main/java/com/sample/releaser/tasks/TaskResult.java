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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 任务执行结果: 包含执行任务时抛出的异常列表
 */
public class TaskResult implements Serializable {

    private final List<Exception> exceptions = new LinkedList<>();

    public static TaskResult success() {
        return new TaskResult();
    }

    public static TaskResult failure() {
        return new TaskResult();
    }
}
