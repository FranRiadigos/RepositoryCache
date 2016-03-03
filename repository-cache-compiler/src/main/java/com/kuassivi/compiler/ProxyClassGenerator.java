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

import com.kuassivi.annotation.RepositoryCacheManager;
import com.kuassivi.annotation.RepositoryProxyCache;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author Francisco Gonzalez-Armijo
 */
public class ProxyClassGenerator {

    /**
     * Will be added to the name of the generated proxy class
     */
    private static final String CLASS_SUFFIX = "ProxyCache";

    /**
     * The full qualified name of the Class that will be processed
     */
    private String qualifiedClassName;

    /**
     * The simple name of the Class that will be processed
     */
    private String simpleClassName;

    /**
     * The name of the Proxy Class that will be generated
     */
    private String generatedClassName;

    /**
     * The package name of the current class
     */
    private String packageName;

    /**
     * Element Utils
     */
    private Elements elementUtils;

    /**
     * Maps all annotated methods
     */
    private Map<String, AnnotatedMethod> methodsMap = new LinkedHashMap<String, AnnotatedMethod>();

    /**
     * Supply the Qualified ClassName for the Proxy
     *
     * @param elementUtils Element utils object
     * @param qualifiedClassName ClassName of the Annotated Class
     */
    public ProxyClassGenerator(Elements elementUtils, String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
        this.elementUtils = elementUtils;
        TypeElement classElement = this.elementUtils.getTypeElement(qualifiedClassName);
        this.simpleClassName = classElement.getSimpleName().toString();
        this.generatedClassName = this.simpleClassName + CLASS_SUFFIX;
        PackageElement pkg = this.elementUtils.getPackageOf(classElement);
        this.packageName = pkg.isUnnamed()
                           ? null
                           : pkg.getQualifiedName().toString();
    }

    /**
     * Adds the annotated method into the methods Map
     *
     * @param methodToInsert Method annotated
     * @throws ProcessingException Method already exist, please use the named value
     */
    public void add(AnnotatedMethod methodToInsert) throws ProcessingException {

        String methodName = methodToInsert.getQualifiedMethodName();

        // Checks for overloaded methods
        if (methodsMap.containsKey(methodName)) {
            // Method already exist in Map
            throw new ProcessingException(methodToInsert.getElement(),
                                          "Conflict: The method \"%1$s\" already exists in %2$s"
                                          + CLASS_SUFFIX + "."
                                          + "class"
                                          + "\nThe checked method in conflict is %1$s( %3$s )."
                                          + "\nWe cannot process overloaded methods, so please add the "
                                          + "\"named\" attribute in the annotated method like "
                                          + "RepositoryCache(named = \"%1$s_2\").",
                                          methodName,
                                          this.simpleClassName,
                                          methodToInsert.getExecutableType().getParameterTypes()
                                                        .toString());
        }

        methodsMap.put(methodName, methodToInsert);
    }

    /**
     * Generates the proxy cache java file
     *
     * @param filer Filer
     * @throws IOException Generator file exception
     */
    public void generateCode(Filer filer) throws IOException {

        TypeSpec.Builder classBuilder =
                TypeSpec.classBuilder(generatedClassName)
                        .addJavadoc("Auto-generated Class by RepositoryCache library Processor")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(RepositoryProxyCache.class);

        // Add Fields
        classBuilder
                .addField(
                        FieldSpec
                                .builder(RepositoryCacheManager.class, "repositoryCacheManager")
                                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                .build())
                .addField(
                        FieldSpec
                                .builder(File.class, "cacheDir")
                                .addModifiers(Modifier.PRIVATE)
                                .build())
                .addField(
                        FieldSpec
                                .builder(String.class, "fileName")
                                .addModifiers(Modifier.PRIVATE)
                                .build())
                .addField(
                        FieldSpec
                                .builder(String.class, "cacheKey")
                                .addModifiers(Modifier.PRIVATE)
                                .build())
                .addField(
                        FieldSpec
                                .builder(TypeName.LONG, "cacheTime")
                                .addModifiers(Modifier.PRIVATE)
                                .build());

        // Add Constructor
        MethodSpec.Builder constructor =
                MethodSpec.constructorBuilder()
                          .addModifiers(Modifier.PRIVATE)
                          .addParameter(File.class, "cacheDir")
                          .addParameter(String.class, "fileName")
                          .addParameter(TypeName.LONG, "cacheTime")
                          .addStatement("this.repositoryCacheManager = "
                                        + "RepositoryCacheManager.getInstance()")
                          .addStatement("this.cacheDir = cacheDir")
                          .addStatement("this.fileName = fileName")
                          .addStatement("this.cacheTime = cacheTime");
        classBuilder.addMethod(constructor.build());

        for (AnnotatedMethod annotatedMethod : methodsMap.values()) {

            MethodSpec.Builder method = MethodSpec
                    .methodBuilder(annotatedMethod.getQualifiedMethodName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(File.class, "cacheDir")
                    .returns(ClassName.get(packageName, generatedClassName));

            String fileName = simpleClassName + "_" + annotatedMethod.getFullMethodName();
            fileName = RepositoryCacheManager.hashMD5(fileName);

            method.addStatement("return new $L(cacheDir, $S, $L)",
                                generatedClassName,
                                fileName,
                                annotatedMethod.getAnnotation().value());
            classBuilder.addMethod(method.build());
        }

        // Add proxy methods
        addProxyMethods(classBuilder);

        // Write file
        TypeSpec typeSpec = classBuilder.build();
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }

    private void addProxyMethods(TypeSpec.Builder classBuilder) {
        MethodSpec.Builder method;

        method = MethodSpec.methodBuilder("persist")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(TypeName.VOID);
        method.addStatement("this.repositoryCacheManager.persist(this)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("persist")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addParameter(String.class, "content")
                           .addAnnotation(Override.class)
                           .returns(TypeName.VOID);
        method.addStatement("this.repositoryCacheManager.persist(this, content)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("evict")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(TypeName.VOID);
        method.addStatement("this.repositoryCacheManager.evict(this)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("select")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addParameter(Object.class, "cacheKey")
                           .addAnnotation(Override.class)
                           .returns(TypeName.VOID);
        method.addStatement("this.cacheKey = String.valueOf(cacheKey)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("getContent")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(String.class);
        method.addStatement("return repositoryCacheManager.getContent(this)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("getCacheDir")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(File.class);
        method.addStatement("return this.cacheDir");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("getCacheTime")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(TypeName.LONG);
        method.addStatement("return this.cacheTime");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("getFileName")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(String.class);
        method.addStatement("return RepositoryCacheManager.hashMD5(this.fileName + this.cacheKey)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("isCached")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(TypeName.BOOLEAN);
        method.addStatement("return repositoryCacheManager.isCached(this)");
        classBuilder.addMethod(method.build());

        method = MethodSpec.methodBuilder("isExpired")
                           .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                           .addAnnotation(Override.class)
                           .returns(TypeName.BOOLEAN);
        method.addStatement("return repositoryCacheManager.isExpired(this)");
        classBuilder.addMethod(method.build());
    }
}
