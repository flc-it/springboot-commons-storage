/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flcit.springboot.commons.storage.task;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executor;

import org.flcit.commons.core.util.ThreadUtils;

/*
 * Pour les tâches sur un répertoire cifs
 */
/**
 * 
 * @since 
 * @author Florian Lestic
 */
abstract class AbstractScanFilesBackgroundTask extends AbstractFilesBackgroundTask {

    private static final long NORMAL_SLEEP = 3000;
    private static final long EXCEPTION_SLEEP = 15000;

    protected AbstractScanFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor) {
        super(path, filenameFilter, executor);
    }

    protected AbstractScanFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor) {
        super(path, fileFilter, executor);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (active()) {
                    this.refreshFiles();
                }
                ThreadUtils.sleep(NORMAL_SLEEP);
            } catch (Exception e) {
                getLogger().error("AbstractScanFilesBackgroundTask run", e);
                ThreadUtils.sleep(EXCEPTION_SLEEP);
            }
            if (Thread.interrupted()) {
                return;
            }
        }
    }

    @Override
    protected void startRegister() throws IOException { }

    @Override
    protected void stopRegister() { }

    protected boolean active() {
        return true;
    }

}
