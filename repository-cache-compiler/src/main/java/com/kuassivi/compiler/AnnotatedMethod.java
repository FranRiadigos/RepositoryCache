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

package com.kuassivi.compiler;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Francisco Gonzalez-Armijo
 * @param <T> Annotation class
 */
public class AnnotatedMethod<T extends Annotation> {

    private Element        element;
    private T              annotation;
    private ExecutableType executableType;

    public AnnotatedMethod(Element element, Class<T> clazz) {
        this.element = element;
        this.annotation = element.getAnnotation(clazz);
        this.executableType = ((ExecutableType) element.asType());
    }

    public String getSimpleMethodName() {
        return this.element.getSimpleName().toString();
    }

    public String getQualifiedClassName() {
        return this.element.getEnclosingElement().asType().toString();
    }

    public String getQualifiedMethodName() {
        List<? extends TypeMirror> elements = getExecutableType().getParameterTypes();
        String parameters = "";
        for (TypeMirror element : elements) {
            parameters += "-" + element.toString();
        }
        return getSimpleMethodName() + parameters;
    }

    public T getAnnotation() {
        return this.annotation;
    }

    public Element getElement() {
        return this.element;
    }

    public ExecutableType getExecutableType() {
        return executableType;
    }
}
