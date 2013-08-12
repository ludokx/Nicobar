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
package com.netflix.scriptlib.core.plugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This library supports pluggable language compilers. Compiler plugins will be loaded
 * into a separate class loader to provider extra isolation in case there are multiple
 * versions of them in the JVM.
 *
 * This class provides the metadata required to locate a compiler plugin and load it.
 *
 * @author James Kojo
 */
public class ScriptCompilerPluginSpec {
    public static class Builder {
        private final String pluginName;
        private Set<Path> runtimeResources = new LinkedHashSet<Path>();
        private String providerClassName;
        private Map<String, String> pluginMetadata = new LinkedHashMap<String, String>();

        public Builder(String pluginName) {
            this.pluginName = pluginName;
        }

        public Builder withCompilerProviderClassName(String className) {
            providerClassName = className;
            return this;
        }
        /**
         * @param resourcePath Paths to jars and resources needed to create the language runtime module. This
         *  includes the language runtime as well as the jar/path to the provider class project.
         */
        public Builder addRuntimeResource(Path resourcePath) {
            if (resourcePath != null) {
                runtimeResources.add(resourcePath);
            }
            return this;
        }
        public Builder addMetatdata(String name, String value) {
            if (name != null && value != null) {
                pluginMetadata.put(name, value);
            }
            return this;
        }
        public Builder addMetatData(Map<String, String> metadata) {
            if (metadata != null) {
                pluginMetadata.putAll(metadata);
            }
            return this;
        }
        public ScriptCompilerPluginSpec build() {
            return new ScriptCompilerPluginSpec(pluginName,
                new LinkedHashSet<Path>(runtimeResources),
                providerClassName,
                new LinkedHashMap<String, String>(pluginMetadata));
        }
    }
    private final String pluginName;
    private final Set<Path> runtimeResources;
    private final String providerClassName;
    private final Map<String, String> pluginMetadata;

    /**
     * @param pluginName language name. will be used to create a module identifier.
     * @param runtimeResources Paths to jars and resources needed to create the language runtime module. This
     *  includes the language runtime as well as the jar/path to the provider class project.
     * @param providerClassName fully qualified classname of the boostrap class
     */
    protected ScriptCompilerPluginSpec(String pluginName, Set<Path> runtimeResources, String providerClassName, Map<String, String> pluginMetadata) {
        this.pluginName =  Objects.requireNonNull(pluginName, "pluginName");
        this.runtimeResources =  Collections.unmodifiableSet(Objects.requireNonNull(runtimeResources, "runtimeResources"));
        this.providerClassName =  Objects.requireNonNull(providerClassName, "providerClassName");
        this.pluginMetadata = Collections.unmodifiableMap(Objects.requireNonNull(pluginMetadata, "pluginMetadata"));
    }

    public String getPluginName() {
        return pluginName;
    }

    /**
     * Get the language runtime resources (jars or directories.)
     */
    public Set<Path> getRuntimeResources() {
        return runtimeResources;
    }

    /**
     * @return application specific metadata
     */
    public Map<String, String> getPluginMetadata() {
        return pluginMetadata;
    }

    /**
     * @return fully qualified classname for instance of {@link ScriptCompilerPlugin} implementation for this plugin
     */
    public String getCompilerProviderClassName() {
        return providerClassName;
    }
}