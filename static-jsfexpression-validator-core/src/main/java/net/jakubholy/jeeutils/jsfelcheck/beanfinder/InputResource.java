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
import java.io.IOException;
import java.io.InputStream;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

/**
 * Abstraction over an InputStream and a File.
 */
public class InputResource {

	private final InputStream stream;
	private final File file;

	public InputResource(InputStream stream) {
		this(null, stream);
	}

	public InputResource(File file) {
		this(
			assertNotNull(file, "file", File.class)
			, toStream(file)
		);
	}

	private InputResource(File file, InputStream stream) {
		this.file = file;
		this.stream = assertNotNull(stream, "stream", InputStream.class);

		try {
			stream.available();
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read from the input stream", e);
		}
	}


	/**
	 * Returns the stream for this resource; never null.
	 */
	public InputStream getStream() {
		return stream;
	}

	/**
	 * If this resource was created for a file then return it otherwise
	 * return null (in which case you should use {@link #getStream()}).
	 */
	public File getFileIfAvailable() {
		return file;
	}

	private static NamedInputStream toStream(File file) throws IllegalArgumentException {
		try {
			return new NamedInputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Failed to create resource for the file "
                		+ file.getAbsolutePath(), e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InputResource that = (InputResource) o;

		if (file == null) {
			if (stream != null ? !stream.equals(that.stream) : that.stream != null) return false;
		} else if (!file.equals(that.file)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		if (file == null) {
			return stream != null ? stream.hashCode() : 0;
		} else {
			return file.hashCode();
		}
	}
}
