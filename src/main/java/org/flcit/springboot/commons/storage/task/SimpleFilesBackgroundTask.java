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
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.Executor;

import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestClientResponseException;

import org.flcit.commons.core.file.util.FileUtils;
import org.flcit.springboot.commons.storage.exception.DoublonException;
import org.flcit.springboot.commons.storage.exception.RejetException;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public abstract class SimpleFilesBackgroundTask extends AbstractScanFilesBackgroundTask {

    private final boolean deleteFileOnCompleted;
    private final boolean archivageOnCompleted;
    private final Path targetDirectoryOnCompleted;
    private final boolean deleteFileOnException;
    private final Path targetDirectoryOnException;
    private final boolean checkFileExistsOnCompleted;
    private final Path targetDirectoryOnDoublonException;
    private Integer year;
    private Integer month;
    private Integer day;

    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = null;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = null;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor, boolean deleteOnCompletedAndException) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = deleteOnCompletedAndException;
        this.deleteFileOnException = deleteOnCompletedAndException;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = null;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor, boolean deleteOnCompleted) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = deleteOnCompleted;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = null;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor, boolean deleteOnCompleted, boolean deleteFileOnException) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = deleteOnCompleted;
        this.deleteFileOnException = deleteFileOnException;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = null;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor, boolean deleteOnCompleted, boolean deleteFileOnException) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = deleteOnCompleted;
        this.deleteFileOnException = deleteFileOnException;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = null;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor, Path targetDirectoryOnException) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = true;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor, Path targetDirectoryOnException) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = true;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = false;
        this.targetDirectoryOnCompleted = null;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor, Path targetDirectoryOnCompleted, Path targetDirectoryOnException) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = true;
        this.targetDirectoryOnCompleted = targetDirectoryOnCompleted;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = null;
    }
    
    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor, Path targetDirectoryOnCompleted, Path targetDirectoryOnException, Path targetDirectoryOnDoublonException) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = true;
        this.targetDirectoryOnCompleted = targetDirectoryOnCompleted;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = targetDirectoryOnDoublonException;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor, Path targetDirectoryOnCompleted, Path targetDirectoryOnException) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = true;
        this.targetDirectoryOnCompleted = targetDirectoryOnCompleted;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor, Path targetDirectoryOnCompleted, Path targetDirectoryOnException, Path targetDirectoryOnDoublonException) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = true;
        this.targetDirectoryOnCompleted = targetDirectoryOnCompleted;
        this.checkFileExistsOnCompleted = false;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = targetDirectoryOnDoublonException;
    }

    protected SimpleFilesBackgroundTask(Path path, FilenameFilter filenameFilter, Executor executor, Path targetDirectoryOnCompleted, boolean checkFileExistsOnCompleted, Path targetDirectoryOnException) {
        super(path, filenameFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = true;
        this.targetDirectoryOnCompleted = targetDirectoryOnCompleted;
        this.checkFileExistsOnCompleted = checkFileExistsOnCompleted;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = null;
    }

    protected SimpleFilesBackgroundTask(Path path, FileFilter fileFilter, Executor executor, Path targetDirectoryOnCompleted, boolean checkFileExistsOnCompleted, Path targetDirectoryOnException) {
        super(path, fileFilter, executor);
        this.deleteFileOnCompleted = false;
        this.deleteFileOnException = false;
        this.archivageOnCompleted = true;
        this.targetDirectoryOnCompleted = targetDirectoryOnCompleted;
        this.checkFileExistsOnCompleted = checkFileExistsOnCompleted;
        this.targetDirectoryOnException = targetDirectoryOnException;
        this.targetDirectoryOnDoublonException = null;
    }

    @Override
    protected void onTaskException(final Path file, Exception e) {
        super.onTaskException(file, e);
        if (e instanceof DoublonException) {
            moveOrDelete(file, true, targetDirectoryOnDoublonException);
        } else {
            Path target = moveOrDelete(file, deleteFileOnException, e instanceof RejetException && ((RejetException) e).getTargetDirectory() != null ? ((RejetException) e).getTargetDirectory() : targetDirectoryOnException);
            if (target != null) {
                writeErrorFile(target, e);
            }
        }
    }

    private void writeErrorFile(Path target, Exception e) {
        if (target == null || e == null) {
            return;
        }
        Path errorFile = Files.isDirectory(target) ? target.resolve("error.log") : FileUtils.setExtension(target, "_error.log");
        getLogger().info("SimpleFilesBackgroundTask writeErrorFile - {}", errorFile);
        try (PrintStream ps = new PrintStream(errorFile.toFile())) {
            if (e instanceof RestClientResponseException) {
                ps.println("Response body :");
                ps.write(((RestClientResponseException) e).getResponseBodyAsByteArray());
                ps.println();
            }
            e.printStackTrace(ps);
        } catch (IOException ioe) {
            getLogger().error("SimpleFilesBackgroundTask writeErrorFile - " + errorFile, ioe);
        }
    }

    @Override
    protected void afterTaskCompleted(final Path file) {
        moveOrDelete(file, deleteFileOnCompleted, getPathArchivage(targetDirectoryOnCompleted, archivageOnCompleted));
    }

    @SuppressWarnings("deprecation")
    private boolean saveDate(Date current) {
        boolean newDate = false;
        if (year == null || year != current.getYear()) {
            year = current.getYear();
            newDate = true;
        }
        if (month == null || month != current.getMonth()) {
            month = current.getMonth();
            newDate = true;
        }
        if (day == null || day != current.getDate()) {
            day = current.getDate();
            newDate = true;
        }
        return newDate;
    }

    private final Path getPathArchivage(Path directory, boolean archivage) {
        if (directory != null && archivage) {
            return getPathArchivage(directory);
        }
        return directory;
    }

    private final synchronized Path getPathArchivage(Path directory) {
        boolean newDate = saveDate(new Date());
        Path currentPath = directory.resolve(String.valueOf((year + 1900))).resolve(month < 9 ? "0" + (month + 1) : String.valueOf((month + 1))).resolve(day.toString());
        if (newDate) {
            try {
                Files.createDirectories(currentPath);
            } catch (IOException e) {
                getLogger().error("SimpleFilesBackgroundTask getPathArchivage - " + currentPath, e);
                return directory;
            }
        }
        return currentPath;
    }

    private Path moveOrDelete(final Path file, boolean delete, Path target) {
        if (target != null) {
            return move(file, target);
        } else if (delete) {
            delete(file);
        }
        return null;
    }

    private Path move(final Path file, Path target) {
        try {
            Path dest = target.resolve(file.getFileName());
            if (Files.isDirectory(file)) {
                FileUtils.moveDirectory(file, dest);
                return dest;
            } else if (!checkFileExistsOnCompleted || Files.exists(file)) {
                Files.move(file, dest, StandardCopyOption.REPLACE_EXISTING);
                return dest;
            }
        } catch (IOException e) {
            getLogger().error("SimpleFilesBackgroundTask move - " + file, e);
        }
        return null;
    }

    private void delete(final Path file) {
        try {
            FileSystemUtils.deleteRecursively(file);
        } catch (IOException e) {
            getLogger().error("SimpleFilesBackgroundTask delete - " + file, e);
        }
    }

}
