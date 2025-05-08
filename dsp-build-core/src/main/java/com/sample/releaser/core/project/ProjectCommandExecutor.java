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

package com.sample.releaser.core.project;

import com.sample.releaser.core.ReleaserProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class ProjectCommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandPicker.class);

    private static final boolean IS_OS_WINDOWS = System.getProperty("os.name").startsWith("Windows");

    private static final String[] OS_OPERATORS = {"|", "<", ">", "||", "&&"};

    public void build(ReleaserProperties properties, String originalVersion, String versionFromGit) {
        logger.info("exec build command in project dir: {}", properties.getWorkingDir());

        // mvn versions:set -DnewVersion=CMC.VERSION
        // mvn versions:update-parent
        // mvn package && mvn deploy

        String command = "mvn package";


        String[] commands = command.split(" ");


        runCommand(new File(properties.getWorkingDir()), commands);
    }

    private void runCommand(File projectRootDir, String[] commands) {

        String[] commandsToRun = commands;

        String lastArg = String.join(" ", commands);
        if (IS_OS_WINDOWS) {
            commandsToRun = new String[]{"cmd", "/c", lastArg};
        } else {
            if (Arrays.stream(OS_OPERATORS).anyMatch(lastArg::contains)) {
                commandsToRun = new String[]{"/bin/bash", "-c", lastArg};
            }
        }

        ProcessExecutor processExecutor = new ProcessExecutor().directory(projectRootDir).command(commandsToRun).destroyOnExit().readOutput(true)
                .redirectOutputAlsoTo(Slf4jStream.of("releaser.commands").asInfo())
                .redirectErrorAlsoTo(Slf4jStream.of("releaser.commands").asWarn());
        try {
            processExecutor.execute();
        } catch (IOException | TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runCommand() {

    }


    //String buildCommand(ProjectVersion version) {
    //    if (projectType == ProjectType.MAVEN) {
    //        return mavenCommandWithSystemProps(releaserProperties.getMaven().getBuildCommand(), version);
    //    }
    //    return bashCommandWithSystemProps(releaserProperties.getBash().getBuildCommand());
    //}

    //private File project


    static class CommandPicker {
        private final ReleaserProperties releaserProperties;

        CommandPicker(ReleaserProperties releaserProperties) {
            this.releaserProperties = releaserProperties;
        }
    }


}
