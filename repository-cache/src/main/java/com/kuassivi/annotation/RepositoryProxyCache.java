/*******************************************************************************
 * Copyright (c) 2016 Francisco Gonzalez-Armijo Riádigos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.kuassivi.annotation;

import java.io.File;

/**
 * @author Francisco Gonzalez-Armijo
 */
public interface RepositoryProxyCache {

    void save();

    void clear();

    void setContent(String content);

    String getContent();

    File getCacheDir();

    int getCacheTime();

    String getFileName();

    String getMethodName();

    boolean isCached();

    boolean isExpired();

    void log(String message);
}
