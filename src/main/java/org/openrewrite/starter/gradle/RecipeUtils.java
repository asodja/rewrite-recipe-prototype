package org.openrewrite.starter.gradle;

import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.openrewrite.starter.gradle.GradleConstants.INPUT_ANNOTATION_PATTERN;
import static org.openrewrite.starter.gradle.GradleConstants.PROPERTY_PATTERN;

public class RecipeUtils {

    public static boolean isSetter(J.MethodDeclaration method) {
        // TODO check also types
        return method.getName().getSimpleName().startsWith("set");
    }

    public static boolean isSetter(J.MethodInvocation method) {
        // TODO check also types
        return method.getName().getSimpleName().startsWith("set") && method.getArguments().size() == 1;
    }

    public static boolean isVariableForPlainProperty(J.VariableDeclarations variableDeclarations) {
        return variableDeclarations.getType() != null && !variableDeclarations.getType().isAssignableFrom(PROPERTY_PATTERN);
    }

    public static boolean isGetterForPlainProperty(J.MethodDeclaration method) {
        return (method.getName().getSimpleName().startsWith("get") || method.getName().getSimpleName().startsWith("is"))
                && method.getLeadingAnnotations().stream().anyMatch(annotation -> annotation.getAnnotationType().getType().isAssignableFrom(INPUT_ANNOTATION_PATTERN))
                && method.getReturnTypeExpression() != null
                && method.getReturnTypeExpression().getType() != null
                && !method.getReturnTypeExpression().getType().isAssignableFrom(PROPERTY_PATTERN);
    }

    public static String getterToField(J.MethodDeclaration method) {
        return getterToField(method.getSimpleName());
    }

    public static String getterToField(String getterName) {
        String property = getterName.replace("get", "").replace("is", "");
        return property.substring(0, 1).toLowerCase() + property.substring(1);
    }

    public static String setterToField(J.MethodDeclaration method) {
        String property = method.getSimpleName().replace("set", "");
        return property.substring(0, 1).toLowerCase() + property.substring(1);
    }

    public static String setterToGetter(J.MethodInvocation method) {
        return method.getSimpleName().replace("set", "get");
    }

    public static J.Modifier newModifier(J.Modifier.Type type) {
        return new J.Modifier(
                Tree.randomId(),
                Space.build(" ", emptyList()),
                Markers.EMPTY,
                type,
                emptyList()
        );
    }

    public static String getPrimitiveBoxedType(JavaType.Primitive primitive) {
        switch (primitive.getKeyword()) {
            case "boolean":
                return Boolean.class.getName();
            case "byte":
                return Byte.class.getName();
            case "char":
                return Character.class.getName();
            case "double":
                return Double.class.getName();
            case "float":
                return Float.class.getName();
            case "int":
                return Integer.class.getName();
            case "long":
                return Long.class.getName();
            case "short":
                return Short.class.getName();
            case "String":
                return String.class.getName();
            case "void":
                return Void.class.getName();
            default:
                throw new UnsupportedOperationException("Can't wrap primitive type: " + primitive.getKeyword());
        }
    }
}
