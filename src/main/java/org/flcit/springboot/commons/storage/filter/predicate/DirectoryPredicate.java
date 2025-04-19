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

package org.flcit.springboot.commons.storage.filter.predicate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import org.flcit.commons.core.file.util.FileUtils;

/**
 * @param <T>
 * @since 
 * @author Florian Lestic
 */
public class DirectoryPredicate<T extends Path> implements Predicate<T> {

    private static Predicate<Path> instance;

    private DirectoryPredicate() { }

    @Override
    public boolean test(T t) {
        return Files.isDirectory(t)
                && !FileUtils.isTempFile(t);
    }

    /**
     * @return
     */
    public static Predicate<Path> getInstance() {
        if (instance == null) {
            instance = new DirectoryPredicate<>();
        }
        return instance;
    }

}
