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

package org.flcit.springboot.commons.storage.exception;

import java.nio.file.Path;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.flcit.commons.core.exception.BasicRuntimeException;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RejetException extends BasicRuntimeException {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings({ "java:S1948", "java:S1165" })
    private Path targetDirectory;

    public RejetException(String message) {
        super(message);
    }

    public RejetException(Throwable cause) {
        super(cause);
    }

    public RejetException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return
     */
    public Path getTargetDirectory() {
        return targetDirectory;
    }

    /**
     * @param targetDirectory
     */
    public void setTargetDirectory(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}
