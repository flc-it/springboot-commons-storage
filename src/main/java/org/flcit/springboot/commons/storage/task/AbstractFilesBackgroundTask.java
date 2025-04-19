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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileSystemUtils;

import org.flcit.commons.core.executor.SingleTaskThreadExecutor;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
abstract class AbstractFilesBackgroundTask implements InitializingBean, Runnable, ApplicationListener<ContextClosedEvent> {

    protected final Path path;
    private final FilenameFilter filenameFilter;
    private final FileFilter fileFilter;
    private long retryDelayOnException = 900000;
    private final Executor executor;
    private Thread thread;
    private final Set<Path> currentFiles = ConcurrentHashMap.newKeySet(10);
    private boolean shutdown;

    protected AbstractFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor) {
        this.path = path;
        this.filenameFilter = filenameFilter;
        this.fileFilter = null;
        this.executor = executor;
    }

    protected AbstractFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor) {
        this.path = path;
        this.filenameFilter = null;
        this.fileFilter = fileFilter;
        this.executor = executor;
    }

    protected abstract void process(final Path file) throws IOException;
    protected abstract Logger getLogger();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.running()) {
            if (this.purgeBeforeStart()) {
                new SingleTaskThreadExecutor().execute(this::purge);
            }
            this.start();
        }
    }

    protected File[] getFiles() {
        if (filenameFilter != null) {
            return this.path.toFile().listFiles(filenameFilter);
        }
        return this.fileFilter != null ? this.path.toFile().listFiles(fileFilter) : this.path.toFile().listFiles();
    }

    protected Stream<Path> getStreamFiles() throws IOException {
        return filenameFilter != null || fileFilter != null ? Files.walk(this.path, 1).skip(1).filter(this::accept) : Files.walk(this.path, 1).skip(1);
    }

    protected synchronized void refreshFiles() throws IOException {
        try (Stream<Path> files = getStreamFiles()) {
            files.forEach(this::startTask);
        }
    }

    protected void startTask(final Path file) {
        if (!currentFiles.add(file)) {
            return;
        }
        executeTask(file);
    }

    private void executeTask(final Path file) {
        if (shutdown) {
            return;
        }
        executor.execute(() -> this.executeCommand(file));
    }

    private void retryTaskOnException(final Path file, final Exception e) {
        if (shutdown) {
            return;
        }
        if (getLogger().isInfoEnabled()) {
            getLogger().info(String.format("%s retryTaskOnException - %s", ClassUtils.getShortName(this.getClass()), file), e);
        }
        if (executor instanceof ThreadPoolTaskScheduler) {
            ((ThreadPoolTaskScheduler) executor).schedule(() -> this.executeCommand(file), new Date(System.currentTimeMillis() + retryDelayOnException));
        } else {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("{} retryTaskOnException is not delayed !", ClassUtils.getShortName(this.getClass()));
            }
            executeTask(file);
        }
    }

    private void executeCommand(final Path file) {
        long time = System.currentTimeMillis();
        if (getLogger().isInfoEnabled()) {
            getLogger().info("{} executeCommand - {}", ClassUtils.getShortName(this.getClass()), file);
        }
        try {
            process(file);
            afterTaskCompleted(file);
            remove(file);
        } catch (Exception e) {
            if (retryOnTaskException(file, e)) {
                retryTaskOnException(file, e);
            } else {
                onTaskException(file, e);
                remove(file);
            }
        }
        if (getLogger().isInfoEnabled()) {
            getLogger().info("{} executeCommand - {} process in {} ms", ClassUtils.getShortName(this.getClass()), file, System.currentTimeMillis() - time);
        }
    }

    /**
     * @param file This Path may be use for further computation in overriding classes
     * @param e This Exception may be use for further computation in overriding classes
     */
    protected boolean retryOnTaskException(final Path file, Exception e) {
        return false;
    }

    protected void afterTaskCompleted(final Path file) {    }

    protected void onTaskException(final Path file, Exception e) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(String.format("AbstractFilesBackgroundTask onTaskException - %s", file), e);
        }
    }

    private synchronized boolean remove(final Path file) {
        return currentFiles.remove(file);
    }

    protected boolean accept(Path directory, Path filename) {
        return (filenameFilter == null && fileFilter == null) || (filenameFilter != null && filenameFilter.accept(directory.toFile(), filename.getFileName().toString())) || (fileFilter != null && fileFilter.accept(directory.resolve(filename).toFile()));
    }

    private boolean accept(Path file) {
        return accept(file.getParent(), file.getFileName());
    }

    protected abstract void startRegister() throws IOException;

    private void start() throws IOException {
        if (this.thread == null) {
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            this.thread.setName(ClassUtils.getShortName(this.getClass()) + "-watch");
        }
        startRegister();
        thread.start();
    }

    private boolean running() {
        return this.thread != null && this.thread.isAlive();
    }

    protected abstract void stopRegister();

    private void stopThread() {
        if (this.thread != null && this.thread != Thread.currentThread()) {
            this.thread.interrupt();
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.shutdown = true;
        this.stopRegister();
        this.stopThread();
        if (this.executor instanceof ExecutorConfigurationSupport) {
            ((ExecutorConfigurationSupport) (this.executor)).shutdown();
        } else if (this.executor instanceof ExecutorService) {
            ((ExecutorService) (this.executor)).shutdown();
        }
    }

    protected boolean purgeBeforeStart() {
        return true;
    }

    protected void purge() {
        try (Stream<Path> walk = Files.walk(this.path, 1)) {
            long maxTime = System.currentTimeMillis() - 86400000;
            walk.skip(1).filter(pathToFilter -> {
                try {
                    return !accept(pathToFilter) && Files.getLastModifiedTime(pathToFilter).toMillis() < maxTime;
                } catch (IOException e) {
                    if (getLogger().isWarnEnabled()) {
                        getLogger().warn(String.format("AbstractFilesBackgroundTask purge accept - %s", pathToFilter), e);
                    }
                }
                return false;
            }).forEach(t -> {
                try {
                    FileSystemUtils.deleteRecursively(t);
                } catch (IOException e) {
                    if (getLogger().isWarnEnabled()) {
                        getLogger().warn(String.format("AbstractFilesBackgroundTask purge delete - %s", path), e);
                    }
                }
            });
        } catch (IOException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn(String.format("AbstractFilesBackgroundTask purge - %s", path), e);
            }
        }
    }

}
