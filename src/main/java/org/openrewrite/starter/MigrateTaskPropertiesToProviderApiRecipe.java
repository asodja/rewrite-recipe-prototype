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
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.starter.gradle.RecipeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openrewrite.starter.gradle.GradleConstants.PROPERTIES_MESSAGE_PREFIX;
import static org.openrewrite.starter.gradle.GradleConstants.PROPERTY_NAME;
import static org.openrewrite.starter.gradle.RecipeUtils.isVariableForPlainProperty;
import static org.openrewrite.starter.gradle.RecipeUtils.newModifier;

public class MigrateTaskPropertiesToProviderApiRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Rewrite field, getter and setter for every plain property to a Property API";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                classDecl = (J.ClassDeclaration) super.visitClassDeclaration(classDecl, executionContext);
                // Remove setters
                List<Statement> statements = classDecl.getBody().getStatements()
                        .stream()
                        .filter(statement -> !isSetterForPlainProperty(statement, executionContext))
                        .map(statement -> transformPropertyVariableDeclaration(statement, executionContext))
                        .collect(Collectors.toList());
                return classDecl.withBody(classDecl.getBody().withStatements(statements));
            }

            private boolean isSetterForPlainProperty(Statement statement, ExecutionContext executionContext) {
                if (!(statement instanceof J.MethodDeclaration)) {
                    return false;
                }
                J.MethodDeclaration method = (J.MethodDeclaration) statement;
                String field = RecipeUtils.setterToField(method);
                return RecipeUtils.isSetter(method) && executionContext.getMessage(getPropertiesMessageKey(), Collections.<String>emptySet()).contains(field);
            }

            private Statement transformPropertyVariableDeclaration(Statement statement, ExecutionContext executionContext) {
                if (!(statement instanceof J.VariableDeclarations)) {
                    return statement;
                }
                J.VariableDeclarations variableDeclaration = (J.VariableDeclarations) statement;
                J.VariableDeclarations.NamedVariable variable = variableDeclaration.getVariables().get(0);
                if (isVariableForPlainProperty(variableDeclaration)
                        && executionContext.getMessage(getPropertiesMessageKey(), Collections.<String>emptySet()).contains(variable.getSimpleName())) {
                    maybeAddImport(PROPERTY_NAME);
                    // TODO add intialization:
                    // JLeftPadded.withElement(Space.build(" ", Collections.emptyList())), J.MethodInvocation..)
                    variable = variable
                            .withType(JavaType.buildType(PROPERTY_NAME));
                    return variableDeclaration
                            .withType(JavaType.buildType(PROPERTY_NAME))
                            .withModifiers(maybeAddFinal(variableDeclaration.getModifiers()))
                            .withTypeExpression(toProperty(variableDeclaration.getTypeExpression()))
                            .withVariables(Collections.singletonList(variable));
                }
                return statement;
            }

            private List<J.Modifier> maybeAddFinal(List<J.Modifier> modifiers) {
                if (J.Modifier.hasModifier(modifiers, J.Modifier.Type.Final)) {
                    return modifiers;
                }
                modifiers = new ArrayList<>(modifiers);
                modifiers.add(newModifier(J.Modifier.Type.Final));
                return modifiers;
            }

            @Override
            public J visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                method = (J.MethodDeclaration) super.visitMethodDeclaration(method, executionContext);
                if (isGetterForPlainProperty(method, executionContext)) {
                    maybeAddImport(PROPERTY_NAME);
                    return method
                            .withMethodType(toProperty(method.getMethodType()))
                            .withReturnTypeExpression(toProperty(method.getReturnTypeExpression()));
                }
                return method;
            }

            private boolean isGetterForPlainProperty(J.MethodDeclaration method, ExecutionContext executionContext) {
                String field = RecipeUtils.getterToField(method);
                return RecipeUtils.isGetterForPlainProperty(method)
                        && executionContext.getMessage(getPropertiesMessageKey(), Collections.<String>emptySet()).contains(field);
            }

            private String getPropertiesMessageKey() {
                J.ClassDeclaration classDeclaration = getCursor().firstEnclosing(J.ClassDeclaration.class);
                String className = classDeclaration.getType().getFullyQualifiedName();
                return PROPERTIES_MESSAGE_PREFIX + className;
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

            private JavaType.@Nullable Method toProperty(@Nullable JavaType.Method type) {
                if (type == null) {
                    return null;
                }
                return type.withReturnType(JavaType.buildType(PROPERTY_NAME));
            }
        };
    }
}
