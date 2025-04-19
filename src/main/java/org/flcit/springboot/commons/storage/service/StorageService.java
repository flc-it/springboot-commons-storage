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

package org.flcit.springboot.commons.storage.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.flcit.commons.core.file.util.FileUtils;
import org.flcit.springboot.commons.core.file.util.ResponseFileUtils;
import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.commons.storage.configuration.StorageProperties;
import org.flcit.springboot.commons.storage.exception.StorageException;
import org.flcit.springboot.commons.storage.exception.StorageFileNotFoundException;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@Service
public class StorageService {

    private static final String FORMAT_NOT_READ_FILE = "Could not read file: %s";

    private final Path rootLocation;

    @Autowired
    private StorageService(StorageProperties storageProperties) {
        this.rootLocation = storageProperties.getLocation();
    }

    /**
     * @param file
     * @param directory
     * @param maxLength
     * @return
     */
    public Path copyWithUniqueId(MultipartFile file, String directory, int maxLength) {
        return copy(file, directory, FileUtils.addUniqueId(file.getOriginalFilename(), maxLength));
    }

    /**
     * @param file
     * @param directory
     * @param name
     * @return
     */
    public Path copy(MultipartFile file, String directory, String name) {
        return copy(file, load(directory), name);
    }

    private Path copy(MultipartFile file, Path path, String name) {
        if (file == null) {
            throw new StorageException("Failed to store nullable file: " + name);
        }
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file: " + file.getOriginalFilename());
        }
        try {
            path = path.resolve(StringUtils.toIso88591(org.springframework.util.StringUtils.hasLength(name) ? name : file.getOriginalFilename()));
            file.transferTo(path);
            return path;
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * @param directory
     * @param filename
     * @return
     */
    public boolean delete(String directory, String filename) {
        return delete(load(directory, filename));
    }

    /**
     * @param path
     * @return
     */
    public boolean delete(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new StorageException("Could not delete file: " + path, e);
        }
    }

    private Path load(String directory) {
        return rootLocation.resolve(directory);
    }

    private Path load(String directory, String filename) {
        return this.load(directory).resolve(filename);
    }

    /**
     * @param directory
     * @param filename
     * @return
     * @throws IOException
     */
    public OutputStream loadAsOutputStream(String directory, String filename) throws IOException {
        return Files.newOutputStream(this.load(directory).resolve(filename));
    }

    /**
     * @param directory
     * @param filename
     * @return
     * @throws IOException
     */
    public InputStream loadAsInputStream(String directory, String filename) throws IOException {
        return Files.newInputStream(this.load(directory).resolve(filename));
    }

    private Resource loadAsResource(String directory, String filename) {
        return loadAsResource(load(directory, filename));
    }

    private Resource loadAsResource(Path file) {
        try {
            final Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(String.format(FORMAT_NOT_READ_FILE, file.getFileName()));
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException(String.format(FORMAT_NOT_READ_FILE, file.getFileName()), e);
        }
    }

    /**
     * @param directory
     * @param filename
     * @param newName
     * @return
     */
    public ResponseEntity<Resource> loadAsResponseEntity(String directory, String filename, String newName) {
        return ResponseFileUtils.get(loadAsResource(directory, filename), newName);
    }

}
