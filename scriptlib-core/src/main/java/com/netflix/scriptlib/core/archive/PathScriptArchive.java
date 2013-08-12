/*
 *
 *  Copyright 2013 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.scriptlib.core.archive;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Script archive backed by a files in a {@link Path}. Includes all files under the given rootPath.
 *
 * @author James Kojo
 */
public class PathScriptArchive implements ScriptArchive {

    /**
     * Used to Construct a {@link PathScriptArchive}
     */
    public static class Builder {
        private final String name;
        private final int version;
        private final Path rootDirPath;

        private final Map<String, String> archiveMetadata = new LinkedHashMap<String, String>();
        private final List<String> dependencies = new LinkedList<String>();
        public Builder(String name, int version, Path rootDirPath) {
            this.name = name;
            this.version = version;
            this.rootDirPath = rootDirPath;
        }
        public Builder addMetadata(Map<String, String> metadata) {
            if (metadata != null) {
                archiveMetadata.putAll(metadata);
            }
            return this;
        }
        public Builder addMetadata(String property, String value) {
            if (property != null && value != null) {
                archiveMetadata.put(property, value);
            }
            return this;
        }
        public Builder addDependency(String dependencyName) {
            if (dependencyName != null) {
                dependencies.add(dependencyName);
            }
            return this;
        }
        public PathScriptArchive build() throws IOException {
           return new PathScriptArchive(name, version, rootDirPath,
               new HashMap<String, String>(archiveMetadata),
               new ArrayList<String>(dependencies));
        }
    }

    private final String archiveName;
    private final int archiveVersion;
    private final Set<String> entryNames;
    private final Path rootDirPath;
    private final URL rootUrl;
    private final Map<String, String> archiveMetadata;
    private final List<String> dependencies;

    PathScriptArchive(String archiveName, int archiveVersion, Path rootDirPath, Map<String, String> applicationMetaData, List<String> dependencies) throws IOException {
        this.archiveName = Objects.requireNonNull(archiveName, "archiveName");
        this.archiveVersion = archiveVersion;
        this.rootDirPath = Objects.requireNonNull(rootDirPath, "rootPath");
        if (!this.rootDirPath.isAbsolute()) throw new IllegalArgumentException("rootPath must be absolute.");

        this.archiveMetadata = Objects.requireNonNull(applicationMetaData, "applicationMetaData");
        this.dependencies = Objects.requireNonNull(dependencies, "dependencies");

        // initialize the index
        final Set<String> indexBuilder = new HashSet<String>();
        Files.walkFileTree(this.rootDirPath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                indexBuilder.add(PathScriptArchive.this.rootDirPath.relativize(file).toString());
                return FileVisitResult.CONTINUE;
            };
        });
        entryNames = Collections.unmodifiableSet(indexBuilder);
        rootUrl = this.rootDirPath.toUri().toURL();
    }

    @Override
    public String getArchiveName() {
        return archiveName;
    }
    @Override
    public int getArchiveVersion() {
        return archiveVersion;
    }

    @Override
    public URL getRootUrl() {
        return rootUrl;
    }

    @Override
    public Set<String> getArchiveEntryNames() {
        return entryNames;
    }

    @Override
    @Nullable
    public URL getEntry(String entryName) throws IOException {
        if (!entryNames.contains(entryName)) {
            return null;
        }
        return rootDirPath.resolve(entryName).toUri().toURL();
    }

    @Override
    public Map<String, String> getArchiveMetadata() {
        return archiveMetadata;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }
}