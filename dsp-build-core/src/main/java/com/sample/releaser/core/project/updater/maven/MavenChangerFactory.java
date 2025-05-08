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

package com.sample.releaser.core.project.updater.maven;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.versions.api.PomHelper;
import org.codehaus.mojo.versions.change.VersionChangerFactory;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;
import org.slf4j.Logger;

public class MavenChangerFactory extends VersionChangerFactory {

    public MavenChangerFactory(Model model, ModifiedPomXMLEventReader pom, Logger logger) {
        this.setModel(model);
        this.setPom(pom);
        this.setLog(new MavenLog(logger));
    }

    public synchronized PropertyChanger newPropertyChanger() {
        return propertyChange -> {
            if (PomHelper.setPropertyVersion(getPom(), null, propertyChange.getName(), propertyChange.getNewValue())) {
                getLog().info("    Updating properties " + propertyChange.getName());
                getLog().info("        from value " + propertyChange.getOldValue() + " to " + propertyChange.getNewValue());
            }
        };
    }

    private static class MavenLog implements Log {

        private final Logger delegate;

        private final String context;

        public MavenLog(Logger delegate) {
            this("Exception", delegate);
        }

        public MavenLog(String context, Logger delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        @Override
        public boolean isDebugEnabled() {
            return this.delegate.isDebugEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return this.delegate.isInfoEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return this.delegate.isWarnEnabled();
        }


        @Override
        public boolean isErrorEnabled() {
            return this.delegate.isErrorEnabled();
        }


        @Override
        public void debug(CharSequence content) {
            this.delegate.debug(content.toString());
        }

        @Override
        public void debug(CharSequence content, Throwable error) {
            this.delegate.debug(content.toString(), error);
        }

        @Override
        public void debug(Throwable error) {
            debug(context, error);
        }

        @Override
        public void info(CharSequence content) {
            this.delegate.info(content.toString());
        }

        @Override
        public void info(CharSequence content, Throwable error) {
            this.delegate.info(content.toString(), error);
        }

        @Override
        public void info(Throwable error) {
            info(context, error);
        }

        @Override
        public void warn(CharSequence content) {
            this.delegate.warn(content.toString());
        }

        @Override
        public void warn(CharSequence content, Throwable error) {
            this.delegate.warn(content.toString(), error);
        }

        @Override
        public void warn(Throwable error) {
            warn(context, error);
        }

        @Override
        public void error(CharSequence content) {
            this.delegate.error(content.toString());
        }

        @Override
        public void error(CharSequence content, Throwable error) {
            this.delegate.error(content.toString(), error);
        }

        @Override
        public void error(Throwable error) {
            error(context, error);
        }
    }
}
