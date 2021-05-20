/*
 * ART
 *
 * Copyright 2019-2021 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.art.logging.stream;

import io.art.logging.logger.*;
import java.io.*;

public class LoggerStream extends PrintStream {
    private final Logger logger;

    public LoggerStream(Logger logger) {
        super(new ByteArrayOutputStream(), true);
        this.logger = logger;
    }

    @Override
    public void flush() {
        super.flush();
        ByteArrayOutputStream stream = (ByteArrayOutputStream) this.out;
        logger.info(stream.toString());
    }
}
