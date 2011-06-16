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

package net.jakubholy.jeeutils.jsfelcheck.sourcefinder;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

public class FilesystemViewFinder implements ViewFileFinder {

    private final String[] allowedExtensions = new String[] {"jsp", "jspf"};

    private Collection<File> searchRoots;

    public FilesystemViewFinder(final Collection<File> searchRoots) {
        this.searchRoots = new LinkedList<File>(searchRoots);
        // TODO verify !null & valid directories
    }

    @Override
    public Collection<File> findViewFiles() {

        final Collection<File> allFiles = new LinkedList<File>();

        for (File  searchRoot: searchRoots) {
            @SuppressWarnings("unchecked")
            Collection<File> viewFilesUnderRoot = FileUtils.listFiles(searchRoot, allowedExtensions, true);
            allFiles.addAll(viewFilesUnderRoot);
        }

        return allFiles;
    }





}
