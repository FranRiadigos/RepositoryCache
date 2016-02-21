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

import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author Francisco Gonzalez-Armijo
 */
public class Utils {

    public static  Types    typeUtils;
    private static Messager messager;

    public static void initialize(ProcessingEnvironment env) {
        typeUtils = env.getTypeUtils();
        messager = env.getMessager();
    }

    /**
     * Prints a Note message
     *
     * @param message The message Note
     */
    public static void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    /**
     * Prints a Note message
     *
     * @param e       The element which has caused the note. Can be null
     * @param message The message Note
     */
    public static void note(Element e, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, e);
    }

    /**
     * Prints an Error message
     *
     * @param e       The element which has caused the error. Can be null
     * @param message The error message
     */
    public static void error(Element e, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, e);
    }

    /**
     * Prints multiple Error messages
     *
     * @param e        The element which has caused the error. Can be null
     * @param messages The error messages
     */
    public static void errors(Element e, Set<String> messages) {
        for (String msg : messages) {
            messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
        }
    }
}
