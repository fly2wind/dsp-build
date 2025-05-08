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

package com.sample.releaser.core.git;

import com.sample.releaser.core.buildsystem.BomRepository;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class GitRepo implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(GitRepo.class);

    private final File gitDir;

    private final GitFactory gitFactory;


    public GitRepo(BomRepository repository) {
        this.gitDir = new File(repository.getLocalDirectory(), ".releaser/git");
        this.gitFactory = new GitFactory(repository);
    }

    public File clone(URIish projectUri) {
        try {
            if (gitDir.exists()) {
                logger.info("Cloned directory [{}] already exists, clean up the directory", gitDir);
                FileUtils.deleteDirectory(gitDir);
            }

            logger.info("Cloning repo from [{}] to directory [{}]", projectUri, gitDir);
            Git git = gitFactory.getCloneCommand().setURI(projectUri.toString()).setDirectory(gitDir).call();
            if (git != null) {
                git.close();
            }
            File clonedRepo = git.getRepository().getWorkTree();
            logger.info("Cloned repo to [{}]", clonedRepo);
            return clonedRepo;
        } catch (Exception e) {
            throw new IllegalStateException("Exception occurred while cloning repo", e);
        }
    }

    void checkout(String branch) {
        try {
            logger.info("Checking out branch [{}] for repo [{}]", branch, this.gitDir);
            Git git = this.gitFactory.open(this.gitDir);

            //checkoutBranch(this.basedir, branch);
            logger.info("Successfully checked out the branch [{}]", branch);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Reset changes.
     */
    public void reset() {
        try (Git git = gitFactory.open(this.gitDir)) {
            logger.info("Resetting changes for repo [{}]", this.gitDir);
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            logger.info("Successfully reset any changes");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Fetch changes.
     */
    public void fetch() {
        try (Git git = this.gitFactory.open(this.gitDir)) {
            logger.info("Pull changes for repo [{}]", this.gitDir);
            git.fetch().call();
            logger.info("Successfully pulled the changes");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(gitDir);
    }
}