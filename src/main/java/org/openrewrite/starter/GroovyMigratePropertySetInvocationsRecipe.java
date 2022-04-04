package org.openrewrite.starter;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.groovy.GroovyVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;
import org.openrewrite.starter.gradle.GradleConstants;
import org.openrewrite.starter.gradle.RecipeUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

public class GroovyMigratePropertySetInvocationsRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Rewrite setter invocations for properties that were migrated to provider API";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GroovyVisitor<ExecutionContext>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                method = (J.MethodInvocation) super.visitMethodInvocation(method, executionContext);
                Optional<JavaType.Method> getterOptional = findPropertyGetterForSetter(method);
                if (getterOptional.isPresent()) {
                    // Create method call like `task.getProperty()`
                    J.MethodInvocation getPropertyCall = new J.MethodInvocation(
                            Tree.randomId(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            JRightPadded.build(method.getSelect()),
                            null,
                            new J.Identifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, getterOptional.get().getName(), null, null),
                            JContainer.empty(),
                            getterOptional.get()
                    );
                    // Find a `set` method of the Property<T> interface. We cheat a bit, and we find first `set` method, and then we modify method parameter type.
                    JavaType.Method propertySetterMethod = ((JavaType.FullyQualified) getterOptional.get().getReturnType()).getMethods().stream()
                            .filter(m -> m.getName().equals("set") && m.getParameterTypes().size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Could not find setter for property " + getterOptional.get().getName()))
                            .withParameterTypes(Collections.singletonList(method.getArguments().get(0).getType()));
                    // Modify `setProperty` method to be like `task.getProperty().set(value)`
                    return method
                            .withId(Tree.randomId())
                            .withSelect(getPropertyCall)
                            .withName(new J.Identifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, propertySetterMethod.getName(), null, null))
                            .withMethodType(propertySetterMethod);
                }
                return method;
            }

            private Optional<JavaType.Method> findPropertyGetterForSetter(J.MethodInvocation method) {
                if (RecipeUtils.isSetter(method)
                        && method.getSelect() instanceof J.Identifier
                        && method.getSelect().getType() instanceof JavaType.FullyQualified) {
                    JavaType.FullyQualified type = (JavaType.FullyQualified) method.getSelect().getType();
                    String getterName = RecipeUtils.setterToGetter(method);
                    Pattern parameterType = maybeBoxPrimitive(method.getArguments().get(0).getType());
                    return type.getMethods().stream()
                            .filter(m -> isPropertyGetterMatchingSetter(m, getterName, parameterType))
                            .findFirst();

                }
                return Optional.empty();
            }

            private boolean isPropertyGetterMatchingSetter(JavaType.Method method, String expectedGetterName, Pattern parameterType) {
                return method.getName().equals(expectedGetterName)
                        && method.getReturnType() instanceof JavaType.Parameterized
                        && method.getReturnType().isAssignableFrom(GradleConstants.PROPERTY_PATTERN)
                        && ((JavaType.Parameterized) method.getReturnType()).getTypeParameters().get(0).isAssignableFrom(parameterType);
            }

            private Pattern maybeBoxPrimitive(JavaType maybePrimitive) {
                if (maybePrimitive instanceof JavaType.Primitive) {
                    String keyword = ((JavaType.Primitive) maybePrimitive).getKeyword();
                    switch (keyword) {
                        case "boolean":
                            return Pattern.compile("java.lang.Boolean");
                        case "byte":
                            return Pattern.compile("java.lang.Byte");
                        case "char":
                            return Pattern.compile("java.lang.Char");
                        case "double":
                            return Pattern.compile("java.lang.Double");
                        case "float":
                            return Pattern.compile("java.lang.Float");
                        case "int":
                            return Pattern.compile("java.lang.Integer");
                        case "long":
                            return Pattern.compile("java.lang.Long");
                        case "short":
                            return Pattern.compile("java.lang.Short");
                        case "String":
                            return Pattern.compile("java.lang.String");
                        default:
                            throw new UnsupportedOperationException("Can't wrap primitive type: " + keyword);
                    }
                }
                if (maybePrimitive instanceof JavaType.FullyQualified) {
                    return Pattern.compile(((JavaType.FullyQualified) maybePrimitive).getFullyQualifiedName());
                }
                throw new UnsupportedOperationException("Not supported transforming type to pattern: " + maybePrimitive.getClass());
            }
        };
    }
}
