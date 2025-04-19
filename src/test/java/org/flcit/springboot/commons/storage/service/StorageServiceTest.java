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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockConstructionWithAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import org.flcit.commons.core.functional.runnable.RunnableException;
import org.flcit.springboot.commons.storage.configuration.StorageProperties;
import org.flcit.springboot.commons.storage.exception.StorageException;
import org.flcit.springboot.commons.storage.exception.StorageFileNotFoundException;

class StorageServiceTest {

    private static final WebApplicationContextRunner RUNNER = getWebApplicationContextRunner();

    @Test
    void deleteKo() throws Exception {
        RUNNER.run(context -> {
            final StorageService service = context.getBean(StorageService.class);
            mockFiles(() -> {
                when(Files.deleteIfExists(any(Path.class))).thenThrow(IOException.class);
                assertThrows(StorageException.class, () -> service.delete("files", "test.json"));
            });
        });
    }

    @Test
    void deleteOk() throws Exception {
        RUNNER.run(context ->
            mockFiles(() -> assertFalse(context.getBean(StorageService.class).delete("files", System.currentTimeMillis() + "_test.json")))
        );
    }

    @Test
    void loadAsResponseEntityKo() {
        RUNNER.run(context -> {
            try (MockedConstruction<UrlResource> mock = mockConstructionWithAnswer(UrlResource.class, (c) -> { throw new MalformedURLException(); })) {
                final StorageService service = context.getBean(StorageService.class);
                final String filename = System.currentTimeMillis() + "_test.json";
                assertThrows(StorageFileNotFoundException.class, () -> service.loadAsResponseEntity("files", filename, "test.json"));
            }
            try (MockedConstruction<UrlResource> mock = mockConstruction(UrlResource.class, (m, c) -> {
                when(m.exists()).thenReturn(false);
                when(m.isReadable()).thenReturn(false);
            })) {
                final StorageService service = context.getBean(StorageService.class);
                final String filename = System.currentTimeMillis() + "_test.json";
                assertThrows(StorageFileNotFoundException.class, () -> service.loadAsResponseEntity("files", filename, "test.json"));
            }
        });
    }

    @Test
    void loadAsResponseEntityOk() {
        RUNNER.run(context -> {
            final String filenameTechnique = System.currentTimeMillis() + "_test.json";
            final String filename = "test.json";
            try (MockedConstruction<UrlResource> mock = mockConstruction(UrlResource.class, (m, c) -> {
                when(m.getFilename()).thenReturn(filenameTechnique);
                when(m.exists()).thenReturn(true);
            })) {
                checkLoadAsResponseEntityOk(context.getBean(StorageService.class).loadAsResponseEntity("files", filenameTechnique, filename), filename);
            }
            try (MockedConstruction<UrlResource> mock = mockConstruction(UrlResource.class, (m, c) -> {
                when(m.getFilename()).thenReturn(filenameTechnique);
                when(m.exists()).thenReturn(false);
                when(m.isReadable()).thenReturn(true);
            })) {
                checkLoadAsResponseEntityOk(context.getBean(StorageService.class).loadAsResponseEntity("files", filenameTechnique, filename), filename);
            }
        });
    }

    private static final void checkLoadAsResponseEntityOk(final ResponseEntity<?> response, final String filename) {
        assertInstanceOf(Resource.class, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertEquals(filename, response.getHeaders().getContentDisposition().getFilename());
    }

    @Test
    void loadAsInputStreamOk() {
        RUNNER.run(context ->
            mockFiles(() -> {
                when(Files.newInputStream(any(Path.class))).thenReturn(new ByteArrayInputStream(new byte[0]));
                assertNotNull(context.getBean(StorageService.class).loadAsInputStream("files", System.currentTimeMillis() + "_test.json"));
            })
        );
    }

    @Test
    void loadAsOutputStreamOk() {
        RUNNER.run(context ->
            mockFiles(() -> {
                when(Files.newOutputStream(any(Path.class))).thenReturn(new ByteArrayOutputStream());
                assertNotNull(context.getBean(StorageService.class).loadAsOutputStream("files", System.currentTimeMillis() + "_test.json"));
            })
        );
    }

    @Test
    void copyKo() {
        RUNNER.run(context -> {
            final StorageService service = context.getBean(StorageService.class);
            assertThrows(StorageException.class, () -> service.copy(null, "files", "test.json"));
            final MultipartFile mf = mock(MultipartFile.class);
            when(mf.isEmpty()).thenReturn(true);
            assertThrows(StorageException.class, () -> service.copy(mf, "files", "test.json"));
            when(mf.isEmpty()).thenReturn(false);
            doThrow(IOException.class).when(mf).transferTo(any(Path.class));
            assertThrows(StorageException.class, () -> service.copy(mf, "files", "test.json"));
        });
    }

    @Test
    void copyOk() {
        RUNNER.run(context -> {
            final StorageService service = context.getBean(StorageService.class);
            final MultipartFile mf = mock(MultipartFile.class);
            when(mf.getOriginalFilename()).thenReturn("test.json");
            final Path res = service.copy(mf, "files", "test-name.json");
            assertNotNull(res);
            assertEquals("test-name.json", res.toFile().getName());
            assertEquals("test.json", service.copy(mf, "files", null).toFile().getName());
        });
    }

    @Test
    void copyWithUniqueIdOk() {
        RUNNER.run(context -> {
            final StorageService service = context.getBean(StorageService.class);
            final MultipartFile mf = mock(MultipartFile.class);
            when(mf.getOriginalFilename()).thenReturn("test.json");
            final Path res = service.copyWithUniqueId(mf, "files", 150);
            assertNotNull(res);
            assertNotNull(res.toFile().getName());
            assertTrue(res.toFile().getName().endsWith("test.json"));
        });
    }

    private static final void mockFiles(RunnableException runnable) throws Exception {
        try (MockedStatic<Files> mock = mockStatic(Files.class)) {
            runnable.run();
        }
    }

    private static final WebApplicationContextRunner getWebApplicationContextRunner() {
        final StorageProperties properties = new StorageProperties();
        properties.setLocation(System.getProperty("java.io.tmpdir"));
        return new WebApplicationContextRunner()
                .withBean(StorageService.class, properties);
    }

}
