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

import com.sample.releaser.core.Releaser;
import com.sample.releaser.core.ReleaserProperties;
import com.sample.releaser.core.buildsystem.Bom;
import com.sample.releaser.options.Options;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 任务执行器: 通过Spring Batch执行任务链
 */
public class TasksRunner implements Closeable {

    private final ReleaserProperties properties;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobLauncher jobLauncher;

    private final Releaser releaser;


    public TasksRunner(ReleaserProperties properties, JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobLauncher jobLauncher, Releaser releaser) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.properties = properties;
        this.releaser = releaser;
    }

    public TaskResult runReleaseTasks(Options options, LinkedList<Task> tasks) {
        Flow flow = releaseFlow(tasks);
        Job job = jobBuilderFactory.get("release").start(flow).build().build();
        return runJob(job);
    }

    private TaskResult runJob(Job job) {
        try {
            JobExecution execution = this.jobLauncher.run(job, new JobParameters());
            if (!ExitStatus.COMPLETED.equals(execution.getExitStatus())) {
                return TaskResult.failure();
            }
            return TaskResult.success();
        } catch (JobExecutionException | UnexpectedJobExecutionException ex) {
            return TaskResult.failure();
        }
    }

    private Flow releaseFlow(LinkedList<Task> tasks) {
        Iterator<? extends Task> iterator = tasks.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("release_" + System.currentTimeMillis());

        TaskArgumentsSupplier argsSupplier = new TaskArgumentsSupplier(() -> {
            Bom bom = releaser.discoverFromRepository(properties.getRepository(), false);
            return TaskArguments.forRelease(bom);
        });

        flowBuilder.start(createStep(iterator.next(), argsSupplier));
        while (iterator.hasNext()) {
            flowBuilder.next(createStep(iterator.next(), argsSupplier));
        }

        return flowBuilder.build();
    }

    private Step createStep(Task task, TaskArgumentsSupplier argsSupplier) {
        return stepBuilderFactory.get(task.name()).tasklet((contribution, chunkContext) -> {
            task.apply(argsSupplier.get());
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Override
    public void close() throws IOException {

    }
}
