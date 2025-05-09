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

import com.sample.releaser.core.buildsystem.Bom;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public final class TaskArguments implements Serializable {
    private static final long serialVersionUID = 1L;

    public final Bom bom;


    private TaskArguments(Bom bom) {
        this.bom = bom;
    }

    public static TaskArguments forRelease(Bom bom) {
        return new TaskArguments(bom);
    }
}
