/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.starter;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;

import java.util.regex.Pattern;

public class PlainTaskPropertyToProviderApiRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use `Property<T>` instead of plain task property";
    }

    @Override
    public String getDescription() {
        return "Use `Property<T>` instead of plain task property.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getApplicableTest() {
        return super.getApplicableTest();
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            private final String PROPERTY_NAME = "org.gradle.api.provider.Property";
            private final Pattern PROPERTY_PATTERN = Pattern.compile(PROPERTY_NAME);
            private final Pattern INPUT_ANNOTATION_PATTERN = Pattern.compile("org.gradle.api.tasks.Input");

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                return (J.ClassDeclaration) super.visitClassDeclaration(classDecl, executionContext);
            }

            @Override
            public J visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                method = (J.MethodDeclaration) super.visitMethodDeclaration(method, executionContext);
                if (isGetterAndNotAProperty(method)) {
                    maybeAddImport(PROPERTY_NAME);
                    return method
                            .withMethodType(toProperty(method.getMethodType()))
                            .withReturnTypeExpression(toProperty(method.getReturnTypeExpression()));
                }
                return method;
            }

            private @Nullable TypeTree toProperty(@Nullable TypeTree returnTypeExpression) {
                if (returnTypeExpression instanceof J.Identifier) {
                    J.Identifier identifier = ((J.Identifier) returnTypeExpression);
                    return identifier
                            .withSimpleName("Property<" + identifier.getSimpleName() + ">")
                            .withType(JavaType.buildType(PROPERTY_NAME));
                }
                return returnTypeExpression;
            }

            private boolean isGetterAndNotAProperty(J.MethodDeclaration method) {
                return method.getName().getSimpleName().startsWith("get")
                        && method.getLeadingAnnotations().stream().anyMatch(annotation -> annotation.getAnnotationType().getType().isAssignableFrom(INPUT_ANNOTATION_PATTERN))
                        && method.getReturnTypeExpression() != null
                        && method.getReturnTypeExpression().getType() != null
                        && !method.getReturnTypeExpression().getType().isAssignableFrom(PROPERTY_PATTERN);
            }

            private JavaType.@Nullable Method toProperty(@Nullable JavaType.Method type) {
                if (type == null) {
                    return null;
                }
                return type.withReturnType(JavaType.buildType(PROPERTY_NAME));
            }
        };
    }
}
