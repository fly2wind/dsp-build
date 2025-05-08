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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DSP统一构建工具CLI入口
 * usage: cli [<goal>] [options]
 */
@SpringBootApplication
public class ReleaserCommandLineRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ReleaserCommandLineRunner.class);

    private final ReleaserGoal releaserGoal;

    public ReleaserCommandLineRunner(ReleaserGoal releaserGoal) {
        this.releaserGoal = releaserGoal;
    }

    @Override
    public void run(String... args) {
        logger.info("EXECUTING : command line runner");
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

        String dir = System.getProperty("user.dir");

        for (int i = 0; i < args.length; ++i) {
            logger.info("args[{}]: {}", i, args[i]);
        }

        //解析参数，执行对应的Goal

        releaserGoal.release();
    }

    public static void main(String[] args) {
        logger.info("STARTING THE APPLICATION");
        SpringApplication application = new SpringApplication(ReleaserCommandLineRunner.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
        logger.info("APPLICATION FINISHED");
    }
}
