/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jakubholy.jeeutils.jsfelcheck.beanfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class FileUtils {

    private FileUtils() {/* hide */}

    /**
     * Convert list of Files into list of InputStreams.
     * @param files (required) must be non-null, non-empty
     * @return streams for the files
     * @throws  IllegalArgumentException If null/empty list, if a file cannot be read or a stream opened for it
     */
    public static Collection<InputStream> filesToStream(final Collection<File> files) {
        final Collection<InputStream> inputStream = new ArrayList<InputStream>(files.size());

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("filesToStream: Collection<File> cannot be null/empty, is: " + files);
        }

        for (File inputFile : files) {
            if (!inputFile.canRead()) {
                throw new IllegalArgumentException("The supplied input file "
                        + "cannot be opened for reading: " + inputFile + "(absolute path: "
                        + inputFile.getAbsolutePath() + ")");
            }

            try {
                inputStream.add(new NamedInputStream(inputFile));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Failed to create an input stream for the file "
                		+ inputFile, e);
            }
        }

        return inputStream;
    }
}
