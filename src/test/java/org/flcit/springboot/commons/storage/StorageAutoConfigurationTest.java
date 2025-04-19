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

package org.flcit.springboot.commons.storage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import org.flcit.commons.core.functional.runnable.RunnableException;
import org.flcit.springboot.commons.storage.configuration.StorageProperties;
import org.flcit.springboot.commons.storage.service.StorageService;
import org.flcit.springboot.commons.test.util.ContextRunnerUtils;
import org.flcit.springboot.commons.test.util.PropertyTestUtils;

class StorageAutoConfigurationTest {

    private static final String PREFIX_PROPERTY = "storage.";

    private static final String PROPERTY_LOCATION = PropertyTestUtils.getValue(PREFIX_PROPERTY, "location", System.getProperty("java.io.tmpdir"));

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CommonsStorageAutoConfiguration.class));

    @Test
    void storageBeanOk() throws Exception {
        mockFiles(() -> 
            ContextRunnerUtils.assertHasSingleBean(this.contextRunner
                .withPropertyValues(PROPERTY_LOCATION),
                StorageProperties.class, StorageService.class)
        );
    }

    @Test
    void storageBeanKo() {
        ContextRunnerUtils.assertDoesNotHaveBean(this.contextRunner, StorageProperties.class, StorageService.class);
    }

    @Test
    void initFoldersOk() throws Exception {
        mockFiles(() -> {
            when(Files.createDirectories(any(Path.class))).thenThrow(IOException.class);
            ContextRunnerUtils.assertHasSingleBean(
                this.contextRunner
                    .withPropertyValues(PROPERTY_LOCATION),
                StorageProperties.class
            );
        });
        mockFiles(() ->
            ContextRunnerUtils.assertHasSingleBean(
                this.contextRunner
                    .withPropertyValues(
                            PROPERTY_LOCATION,
                            PropertyTestUtils.getValue(PREFIX_PROPERTY, "init-folders-with-enum", Folder.class.getName()),
                            PropertyTestUtils.getValue(PREFIX_PROPERTY, "init-folders", "F3")
                     ),
                StorageProperties.class
            )
        );
    }

    private static final void mockFiles(RunnableException runnable) throws Exception {
        try (MockedStatic<Files> mock = mockStatic(Files.class)) {
            runnable.run();
        }
    }

}
