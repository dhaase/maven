package org.apache.maven.model.io.xpp3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.model.Dependency;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.ModelBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Is Parsing the Artifact.
 */
public final class HandleExchangedDependencies {

    private static final String EXCHANGE = "exchange";

    public static void parseDependency(ModelBase modelBase, InputLocation location, String attrName, String artifact) {
        if ((modelBase != null) && (attrName.endsWith(EXCHANGE))) {
            Dependency dependency = parseDependency(location, artifact);
            List<Dependency> dependencyList = modelBase.getDependencies();
            if (!isContaining(dependencyList, dependency)) {
                modelBase.addDependency(dependency);
            }
        }
    }

    private static Dependency parseDependency(InputLocation location, String artifact) {
        // groupId, artifactId, version, classifier, type, scope
        Dependency dependency = new Dependency();
        dependency.setClassifier(EXCHANGE);
        dependency.setLocation(EXCHANGE, location );
        dependency.setOptional(true);
        int idx1 = artifact.indexOf(':');
        dependency.setGroupId(artifact.substring(0, idx1));
        int idx2 = artifact.indexOf(':', idx1 + 1);
        if (idx2 > 0) {
            dependency.setArtifactId(artifact.substring(idx1 + 1, idx2));
        } else {
            dependency.setArtifactId(artifact.substring(idx1 + 1));
            return dependency;
        }
        idx1 = artifact.indexOf(':', idx2 + 1);
        if (idx1 > 0) {
            final String version = artifact.substring(idx2 + 1, idx1);
            if (!"*".equals(version)) {
                dependency.setVersion(version);
            }
            idx2 = artifact.indexOf(':', idx1 + 1);
            if (idx2 > 0) {
                final String classifier = artifact.substring(idx1 + 1, idx2);
                if (!"*".equals(classifier)) {
                    dependency.setClassifier(classifier);
                }
                idx1 = artifact.indexOf(':', idx2 + 1);
                if (idx2 > 0) {
                    final String type = artifact.substring(idx2 + 1, idx1);
                    if (!"*".equals(classifier)) {
                        dependency.setType(type);
                    }
                    if ((idx1 + 1) < artifact.length()) {
                        dependency.setScope(artifact.substring(idx1 + 1));
                    }
                }
            }
        }
        return dependency;
    }

    private static boolean isContaining(List<Dependency> dependencyList, Dependency newDependency) {
        for (Dependency dependency : dependencyList) {
            if (dependency.getArtifactId().equals(newDependency.getArtifactId())) {
                if (dependency.getGroupId().equals(newDependency.getGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void removeDuplicates(ModelBase modelBase) {
        List<Dependency> exchangeList = new ArrayList<>();
        List<Dependency> dependencyList = modelBase.getDependencies();
        List<Dependency> dependencyListCopy = new ArrayList<>(modelBase.getDependencies());

        for (Dependency dependency : dependencyListCopy) {
            if (EXCHANGE.equals(dependency.getClassifier())) {
                if (!isContaining(exchangeList, dependency)) {
                    exchangeList.add(dependency);
                }
                dependencyList.remove(dependency);
            }
        }

        for (Dependency dependency : exchangeList) {
            if (!isContaining(dependencyList, dependency)) {
                dependency.setClassifier(null);
                System.out.println("exchange-dependency: " + dependency);
                modelBase.addDependency(dependency);
            }
        }

        if (!exchangeList.isEmpty()) {
            System.out.println("exchangeList: " + exchangeList);
        }
    }


    private HandleExchangedDependencies() {
    }

    public static void main(String... args) {
        Dependency dependency1 = parseDependency(null, "groupId1:artifactId1:version1:classifier1:type1:runtime1");
        System.out.println(dependency1.getVersion());
        System.out.println(dependency1.getClassifier());
        System.out.println(dependency1.getType());
        System.out.println(dependency1.getScope());
        Dependency dependency2 = parseDependency(null, "groupId1:artifactId1");
        System.out.println(dependency2);
        Dependency dependency3 = parseDependency(null, "groupId1:artifactId1:*:*:*:runtime1");
        System.out.println(dependency3);
        System.out.println(dependency3.getVersion());
        System.out.println(dependency3.getClassifier());
        System.out.println(dependency3.getType());
        System.out.println(dependency3.getScope());
    }
}
