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

package org.flcit.springboot.commons.storage.filter;

import java.io.File;
import java.io.FileFilter;

import org.flcit.commons.core.file.util.FileUtils;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@SuppressWarnings("java:S6548")
public class DirectoryFileFilter implements FileFilter {

    private static DirectoryFileFilter instance;

    private DirectoryFileFilter() { }

    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory()
                && !FileUtils.isTempFile(pathname.toPath());
    }

    /**
     * @return
     */
    public static DirectoryFileFilter getInstance() {
        if (instance == null) {
            instance = new DirectoryFileFilter();
        }
        return instance;
    }

}
