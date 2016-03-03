/*******************************************************************************
 * Copyright (c) 2016 Francisco Gonzalez-Armijo Riádigos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.kuassivi.annotation;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Francisco Gonzalez-Armijo
 */
public final class RepositoryCacheManager {

    private static final String DEFAULT_FILE_NAME = "rpc_"; // repository_proxy_cache_
    private static final String CONTENT_FILE_NAME = "rpc_content_"; // repository_proxy_cache_content

    private static RepositoryCacheManager instance;

    private FileManager fileManager;

    private RepositoryCacheManager() {
        fileManager = new FileManager();
    }

    public static RepositoryCacheManager getInstance() {
        return instance == null
               ? instance = new RepositoryCacheManager()
               : instance;
    }

    public static String hashMD5(String str) {
        MessageDigest md;
        StringBuffer sb = new StringBuffer();
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte byteData[] = md.digest();
            //convert the byte to hex format
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Returns a hash code based on the contents of the given array. If the array contains other
     * arrays as its elements, the hash code is based on their identities not their contents. So it
     * is acceptable to invoke this method on an array that contains itself as an element, either
     * directly or indirectly.
     * 
     * For any two arrays {@code a} and {@code b}, if {@code Arrays.equals(a, b)} returns {@code
     * true}, it means that the return value of {@code Arrays.hashCode(a)} equals {@code
     * Arrays.hashCode(b)}.
     * 
     * The value returned by this method is the same value as the method
     * Arrays.asList(array).hashCode(). If the array is {@code null}, the return value is 0.
     *
     * @param objects the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(Object... objects) {
        if (objects == null) {
            return 0;
        }
        int hashCode = 1;
        for (Object element : objects) {
            int elementHashCode;

            if (element == null) {
                elementHashCode = 0;
            } else {
                elementHashCode = (element).hashCode();
            }
            hashCode = 31 * hashCode + elementHashCode;
        }
        return hashCode;
    }

    /**
     * Warning: This is an I/O operation and this method is going to be performed in a different
     * Thread.
     *
     * @param proxyCache the ProxyCache object
     */
    public void persist(RepositoryProxyCache proxyCache) {
        persist(proxyCache, null);
    }

    /**
     * Warning: This is an I/O operation and this method is going to be performed in a different
     * Thread.
     *
     * @param proxyCache the ProxyCache object
     * @param content any content to be stored in the cache
     */
    public void persist(RepositoryProxyCache proxyCache, String content) {
        File cacheFile = buildFile(proxyCache.getCacheDir(),
                                   proxyCache.getFileName(),
                                   DEFAULT_FILE_NAME);
        new Thread(new CacheWriter(fileManager, cacheFile, content)).start();
    }

    /**
     * Warning: This is an I/O operation and this method is going to be performed in a different
     * Thread.
     *
     * @param proxyCache the ProxyCache object
     */
    public void evict(RepositoryProxyCache proxyCache) {
        File cacheFile = buildFile(proxyCache.getCacheDir(),
                                   proxyCache.getFileName(),
                                   DEFAULT_FILE_NAME);
        new Thread(new CacheClear(fileManager, cacheFile)).start();
    }

    /**
     * Warning: This is an I/O operation and this method is going to be performed in a different
     * Thread.
     *
     * @param directory the File directory to clear on disk.
     */
    public static void evictAll(File directory) {
        new Thread(new CacheClear(new FileManager(), directory)).start();
    }

    /**
     * Checks whether the method is cached.
     *
     * @param proxyCache the ProxyCache object
     * @return true if is cached, false otherwise
     */
    public boolean isCached(RepositoryProxyCache proxyCache) {
        File cacheFile = buildFile(proxyCache.getCacheDir(),
                                   proxyCache.getFileName(),
                                   DEFAULT_FILE_NAME);
        return isCached(cacheFile);
    }

    private boolean isCached(File file) {
        return fileManager.exists(file);
    }

    /**
     * Warning: This is an I/O operation and this method must to be performed in a different
     * Thread.
     *
     * @param proxyCache the ProxyCache object
     * @return true if expired, or false otherwise.
     */
    public boolean isExpired(RepositoryProxyCache proxyCache) {
        long methodCacheTime = proxyCache.getCacheTime();
        boolean unlimitedCache = methodCacheTime <= 0;
        File cacheFile = buildFile(proxyCache.getCacheDir(),
                                   proxyCache.getFileName(),
                                   DEFAULT_FILE_NAME);
        if (isCached(cacheFile)) {
            if (unlimitedCache) {
                return false;
            }
            long lastModifiedTime = fileManager.getLastModifiedTime(cacheFile);
            boolean expired = System.currentTimeMillis()
                              > (lastModifiedTime + methodCacheTime);
            if (expired) {
                evict(proxyCache);
            }
            return expired;
        }
        return true;
    }

    /**
     * Warning: This is an I/O operation and this method must to be performed in a different
     * Thread.
     *
     * @param proxyCache the ProxyCache object
     * @return A valid content file.
     */
    public String getContent(RepositoryProxyCache proxyCache) {
        File contentFile = buildFile(proxyCache.getCacheDir(),
                                     proxyCache.getFileName(),
                                     CONTENT_FILE_NAME);
        return fileManager.readFileContent(contentFile);
    }

    /**
     * Builds the cached file method into the disk cache.
     *
     * @param file     The Context File
     * @param fileName The string to store the cache method
     * @param type     The type of the file [ DEFAULT or CONTENT ]
     * @return A valid file.
     */
    private File buildFile(File file, String fileName, String type) {
        //noinspection StringBufferReplaceableByString
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(file.getPath());
        fileNameBuilder.append(File.separator);
        fileNameBuilder.append(type);
        fileNameBuilder.append(fileName);

        return new File(fileNameBuilder.toString());
    }

    /**
     * {@link Runnable} class for writing to disk.
     */
    private static class CacheWriter implements Runnable {

        private final FileManager fileManager;
        private final File        fileToWrite;
        private final String      fileContent;

        CacheWriter(FileManager fileManager, File fileToWrite, String fileContent) {
            this.fileManager = fileManager;
            this.fileToWrite = fileToWrite;
            this.fileContent = fileContent;
        }

        @Override
        public void run() {
            if (!fileManager.exists(fileToWrite)
                || !fileManager.contains(fileToWrite, fileContent)) {
                this.fileManager.writeToFile(fileToWrite, fileContent);
            }
        }
    }

    /**
     * {@link Runnable} class for clearing files on disk.
     */
    private static class CacheClear implements Runnable {

        private final FileManager fileManager;
        private final File        fileToClear;

        CacheClear(FileManager fileManager, File fileToClear) {
            this.fileManager = fileManager;
            this.fileToClear = fileToClear;
        }

        @Override
        public void run() {
            this.fileManager.clearFile(fileToClear);
        }
    }
}
