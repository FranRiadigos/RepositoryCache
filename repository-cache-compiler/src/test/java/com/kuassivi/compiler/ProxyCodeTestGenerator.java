/*******************************************************************************
 * Copyright (c) 2016 Francisco Gonzalez-Armijo Riádigos
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.kuassivi.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProxyCodeTestGenerator {

    private static String[] proxyHeader = new String[]{
            "package test;",

            "import com.kuassivi.annotation.RepositoryCacheManager;",
            "import com.kuassivi.annotation.RepositoryProxyCache;",
            "import java.io.File;",
            "import java.lang.Object;",
            "import java.lang.Override;",
            "import java.lang.String;",

            "/**",
            " * Auto-generated Class by RepositoryCache library Processor */",
            "public final class TestProxyCache implements RepositoryProxyCache {",
            "   private final RepositoryCacheManager repositoryCacheManager;",

            "   private File cacheDir;",

            "   private String fileName;",

            "   private String cacheKey;",

            "   private long cacheTime;",

            "   private TestProxyCache(File cacheDir, String fileName, long cacheTime) {",
            "       this.repositoryCacheManager = RepositoryCacheManager.getInstance();",
            "       this.cacheDir = cacheDir;",
            "       this.fileName = fileName;",
            "       this.cacheTime = cacheTime;",
            "   }"
    };

    private static String[] proxyMethods = new String[]{
            "   @Override",
            "   public final void persist() {",
            "       this.repositoryCacheManager.persist(this);",
            "   }",

            "   @Override",
            "   public final void persist(String content) {",
            "       this.repositoryCacheManager.persist(this, content);",
            "   }",

            "   @Override",
            "   public final void evict() {",
            "       this.repositoryCacheManager.evict(this);",
            "   }",

            "   @Override",
            "   public final void select(Object cacheKey) {",
            "       this.cacheKey = String.valueOf(cacheKey);",
            "   }",

            "   @Override",
            "   public final String getContent() {",
            "       return repositoryCacheManager.getContent(this);",
            "   }",

            "   @Override",
            "   public final File getCacheDir() {",
            "       return this.cacheDir;",
            "   }",

            "   @Override",
            "   public final long getCacheTime() {",
            "       return this.cacheTime;",
            "   }",

            "   @Override",
            "   public final String getFileName() {",
            "       return RepositoryCacheManager.hashMD5(this.fileName + this.cacheKey);",
            "   }",

            "   @Override",
            "   public final boolean isCached() {",
            "       return repositoryCacheManager.isCached(this);",
            "   }",

            "   @Override",
            "   public final boolean isExpired() {",
            "       return repositoryCacheManager.isExpired(this);",
            "   }",
            "}"
    };

    public static Iterable<String> joinItems(String... pieces) {
        List<String> content = new ArrayList<String>(proxyHeader.length + proxyMethods.length);
        List<String> header = Arrays.asList(proxyHeader);
        List<String> methods = Arrays.asList(proxyMethods);
        content.addAll(header);
        Collections.addAll(content, pieces);
        content.addAll(methods);
        return content;
    }
}
