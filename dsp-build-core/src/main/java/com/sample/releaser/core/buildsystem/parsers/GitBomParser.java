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

package com.sample.releaser.core.buildsystem.parsers;

import com.sample.releaser.core.buildsystem.Bom;
import com.sample.releaser.core.buildsystem.BomRepository;
import com.sample.releaser.core.git.GitRepo;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Git仓库的BOM发现处理器
 */
public class GitBomParser extends AbstractBomParser {

    private static final Map<URIish, File> CACHE = new ConcurrentHashMap<>();

    @Override
    public boolean isApplicable(BomRepository repository) {
        return repository.isSourceGit();
    }

    @Override
    public Bom process(BomRepository repository, boolean isRelease) {
        try {
            URIish urIish = new URIish(repository.getUrl());

            File clonedRepository = CACHE.computeIfAbsent(urIish, urIish1 -> {
                GitRepo gitRepo = new GitRepo(repository);
                return gitRepo.clone(urIish);
            });
            if (clonedRepository.exists()) {
                logger.info("Bom repository has already been cloned. will try to reset the current branch and fetch the latest changes.");
                try {
                    GitRepo gitRepo = new GitRepo(repository);
                    gitRepo.reset();
                    //gitRepo.fetch();
                } catch (Exception ex) {
                    logger.warn("Couldn't reset / fetch the repository, will continue", ex);
                    throw new IllegalStateException(ex);
                }

                File bomFile = new File(clonedRepository, repository.getCoordinate());
                if (bomFile.exists()) {
                    return parserBom(bomFile, repository, isRelease);
                }
            }
        } catch (URISyntaxException e) {
            logger.error("Git repository URL format error", e);
            throw new IllegalStateException(e);
        }
        return null;
    }
}
