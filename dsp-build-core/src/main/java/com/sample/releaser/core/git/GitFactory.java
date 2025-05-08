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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

class GitFactory {
    private static final Logger logger = LoggerFactory.getLogger(GitFactory.class);

    private final JschConfigSessionFactory factory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
            session.setConfig("StrictHostKeyChecking", "no");
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            //Connector connector = null;
            //try {
            //    if (SSHAgentConnector.isConnectorAvailable()) {
            //        USocketFactory usf = new JNAUSocketFactory();
            //        connector = new SSHAgentConnector(usf);
            //    }
            //    log.info("Successfully connected to an agent");
            //} catch (AgentProxyException e) {
            //    log.error("Exception occurred while trying to connect to agent. Will create"
            //            + "the default JSch connection", e);
            //    return super.createDefaultJSch(fs);
            //}
            final JSch jsch = super.createDefaultJSch(fs);
            //if (connector != null) {
            //    JSch.setConfig("PreferredAuthentications", "publickey,password");
            //    IdentityRepository identityRepository = new RemoteIdentityRepository(connector);
            //    jsch.setIdentityRepository(identityRepository);
            //}
            return jsch;
        }
    };

    private final CredentialsProvider provider;

    private final TransportConfigCallback callback;

    private GitFactory() {
        this.provider = null;
        this.callback = null;
    }

    public GitFactory(BomRepository repository) {
        if (StringUtils.isNotEmpty(repository.getUsername())) {
            logger.info("Passed username and password - will set a custom credentials provider");
            this.provider = new UsernamePasswordCredentialsProvider(repository.getUsername(), repository.getPassword());
        } else {
            logger.info("No custom credentials provider will be set");
            this.provider = null;
        }
        this.callback = transport -> {
            if (transport instanceof SshTransport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(this.factory);
            }
        };
    }

    Git open(File file) {
        try {
            return Git.open(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    Git init(File file) {
        try {
            return Git.init().setDirectory(file).call();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    CloneCommand getCloneCommand() {
        return Git.cloneRepository().setCredentialsProvider(this.provider).setTransportConfigCallback(this.callback);
    }

    PushCommand push(Git git) {
        return git.push().setCredentialsProvider(this.provider).setTransportConfigCallback(this.callback);
    }
}