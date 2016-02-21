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

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Fluent validator object that checks for valid elements and returns a {@link Set} of error Strings
 * or an empty {@link Set}.
 *
 * @author Francisco Gonzalez-Armijo
 */
final class AnnotationValidator {

    private static AnnotationValidator instance;
    private        Element             annotatedElement;
    private        Class<?>            clazz;
    private        Set<String>         errors;

    private AnnotationValidator(Element annotatedElement,
                                Class<?> clazz) {
        this.errors = new HashSet<String>();
        this.annotatedElement = annotatedElement;
        this.clazz = clazz;
    }

    static AnnotationValidator with(Element annotatedElement,
                                    Class<?> clazz) {
        return (instance == null)
               ? instance = new AnnotationValidator(annotatedElement, clazz)
               : instance;
    }

    /**
     * Checks whether is an accessible {@link ElementType}. Accessible means: public, default or
     * native.
     *
     * @return AnnotationValidator
     */
    AnnotationValidator isAccessible(ElementKind elementType) {
        if (ElementKind.METHOD.equals(elementType)
            && !annotatedElement.getModifiers().contains(PUBLIC)) {
            addError("The method %s is not accessible. "
                     + "It must have public, default or native modifier.",
                     annotatedElement.getSimpleName().toString());
        }
        return this;
    }

    /**
     * Checks whether is an abstract {@link ElementType}.
     *
     * @return AnnotationValidator
     */
    AnnotationValidator isAbstractAllowed(ElementKind elementType, boolean assertion) {
        if (ElementKind.METHOD.equals(elementType)
            && annotatedElement.getModifiers().contains(ABSTRACT) == assertion) {
            addError(
                    "The method %s is abstract. "
                    + (assertion
                       ? "Only abstract methods can be annotated with @%s"
                       : "You can't annotate abstract methods with @%s"),
                    annotatedElement.getSimpleName().toString(),
                    clazz.getSimpleName());
        }
        return this;
    }

    /**
     * Checks whether is not an abstract {@link ElementType}.
     *
     * @return AnnotationValidator
     */
    AnnotationValidator isInterfaceAllowed(ElementKind elementType, boolean assertion) {
        if (ElementKind.METHOD.equals(elementType)
            && (annotatedElement.getEnclosingElement()
                                .getKind() == ElementKind.INTERFACE) != assertion) {
            addError(
                    "The class %s is an Interface. "
                    + (assertion
                       ? "Only Interface classes can have methods annotated with @%s"
                       : "You can't annotate Interface methods with @%s"),
                    annotatedElement.getEnclosingElement().getSimpleName().toString(),
                    clazz.getSimpleName());
        }
        return this;
    }

    void addError(String msg, Object... args) {
        errors.add(String.format(msg, args));
    }

    public Set<String> getErrors() {
        return errors;
    }
}
