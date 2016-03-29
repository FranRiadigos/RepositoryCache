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

package com.kuassivi.examples.repository_cache.dummy;

import com.kuassivi.annotation.RepositoryProxyCache;

import android.content.Context;

import java.io.File;

/**
 * Samples how the RepositoryCache annotation works.
 */
public class MyRepositoryImpl implements MyRepository {

    /**
     * Cloud dataSource strategy (i.e rest service)
     */
    private MyCloudData myCloudData;

    /**
     * Local dataSource strategy (i.e database)
     */
    private MyLocalData myLocalData;

    /**
     * Cache directory of your System
     */
    private File cacheDir;

    /**
     * Initialize dependencies
     *
     * @param context The application Context
     */
    public MyRepositoryImpl(Context context) {
        // Android Cache Dir:
        // These files will be ones that get deleted first when the device runs low on storage,
        // such as below 1 MB.
        this.cacheDir = context.getCacheDir();
        this.myCloudData = new MyCloudData();
        this.myLocalData = new MyLocalData();
    }

    /**
     * Demonstrates how to distinguish between local or cloud data source with the cache strategy.
     * You can store a cache indefinitely if you don't provide any time to expire.
     *
     * @return any retrieved data
     */
    @Override
    public Object[] getAllDataOnce() {

        // Gets an instance of the cached method
        RepositoryProxyCache cache = MyRepositoryProxyCache.getAllDataOnce(this.cacheDir);

        // No need to select any particular cache ID

        // It means the method is cache and it is not expired.
        if (!cache.isExpired()) {

            // retrieves the data from local store
            return this.myLocalData.getAllDataOnce();
        } else {

            // retrieves the data from cloud service
            Object[] object = this.myCloudData.getAllDataOnce();

            // persists the method into the cache
            // (it will save the current key assigned if set)
            if(!cache.isCached()) {
                cache.persist();
            }

            /*
             * Tip:
             *
             * If you are not planning to have a database, you can still using the cache
             * to store your content.
             *
             * Use: "cache.persist(json_string)" to store any string content.
             *
             * Then use: "cache.getContent()" to retrieve cached content.
             */

            return object;
        }
    }

    /**
     * Demonstrates how to distinguish between local or cloud data source with the cache strategy.
     * The RepositoryCache allows you to store different caches depending on a particular ID.
     *
     * @param id any Id
     * @return any retrieved data
     */
    @Override
    public Object getDataById(int id) {

        // Gets an instance of the cached method
        RepositoryProxyCache cache = MyRepositoryProxyCache.getDataById(this.cacheDir);

        // The cache will check for this id if set
        cache.select( id );

        // It means the method is cache and it is not expired.
        if (!cache.isExpired()) {

            // retrieves data from local store
            return this.myLocalData.getDataById(id);
        } else {

            // retrieves data from cloud service
            Object object = this.myCloudData.getDataById(id);

            // persists the method into the cache
            // (it will save the current key assigned if set)
            if(!cache.isCached()) {
                cache.persist();
            }

            /*
             * Tip:
             *
             * If you are not planning to have a database, you can still using the cache
             * to store your content.
             *
             * Use: "cache.persist(json_string)" to store any string content.
             *
             * Then use: "cache.getContent()" to retrieve cached content.
             */

            return object;
        }
    }
}
