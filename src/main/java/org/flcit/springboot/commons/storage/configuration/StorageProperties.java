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

package org.flcit.springboot.commons.storage.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.flcit.commons.core.util.EnumUtils;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@ConfigurationProperties("storage")
public class StorageProperties implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(StorageProperties.class);

    private String location;
    private String[] initFolders;
    private Class<Enum<?>> initFoldersWithEnum;

    /**
     * @return
     */
    public Path getLocation() {
        return Paths.get(location);
    }

    /**
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @param initFolders
     */
    public void setInitFolders(String[] initFolders) {
        this.initFolders = initFolders;
    }

    /**
     * @param initFoldersWithEnum
     */
    public void setInitFoldersWithEnum(Class<Enum<?>> initFoldersWithEnum) {
        this.initFoldersWithEnum = initFoldersWithEnum;
    }

    private String[] getFoldersToInit() {
        if (initFoldersWithEnum != null) {
            return StringUtils.concatenateStringArrays(initFolders, EnumUtils.toStringArray(EnumUtils.values(initFoldersWithEnum)));
        } else {
            return initFolders;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Path root = getLocation();
        final String[] folders = getFoldersToInit();
        try {
            Files.createDirectories(root);
            if (!ObjectUtils.isEmpty(folders)) {
                for (String dir :folders) {
                    Files.createDirectories(root.resolve(dir));
                }
            }
        } catch (IOException e) {
            LOG.error("THE STORAGE DIRECTORY IS NOT ACCESSIBLE IN READ / WRITE !!!!", e);
        }
    }

}
