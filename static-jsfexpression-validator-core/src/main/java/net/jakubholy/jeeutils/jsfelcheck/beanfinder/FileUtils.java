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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class FileUtils {

    private FileUtils() {/* hide */}

    /**
     * Convert list of Files into list of InputResources.
     * @param files (required) must be non-null, non-empty
     * @return resources for the files
     * @throws  IllegalArgumentException If null/empty list, if a file cannot be read or a stream opened for it
     */
    private static Collection<InputResource> filesToResources(final Collection<File> files) {
        final Collection<InputResource> inputResources = new ArrayList<InputResource>(files.size());

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("filesToStream: Collection<File> cannot be null/empty, is: " + files);
        }

        for (File inputFile : files) {
            if (!inputFile.canRead()) {
                throw new IllegalArgumentException("The supplied input file "
                        + "cannot be opened for reading: " + inputFile + " (absolute path: "
                        + inputFile.getAbsolutePath() + ")");
            }

            inputResources.add(new InputResource(inputFile));
        }

        return inputResources;
    }

    /**
     * Convert list of Files into list of InputResources, this variation accepts null.
     * @param files (optional) must non-empty or null
     * @return resources for the files or null if the files are null
     * @throws  IllegalArgumentException If empty list, if a file cannot be read or a stream opened for it
     */
    public static Collection<InputResource> filesToResourcesNullSafe(final Collection<File> files) {
	    if (files == null) return null;
	    return filesToResources(files);
    }

	public static Collection<InputResource> streamsToResourcesNullSafe(final Collection<InputStream> streams) {
		if (streams == null) return null;

		final Collection<InputResource> inputResources = new ArrayList<InputResource>(streams.size());
		for (InputStream stream : streams) {
			inputResources.add(new InputResource(stream));
		}
		return inputResources;
	}
}
