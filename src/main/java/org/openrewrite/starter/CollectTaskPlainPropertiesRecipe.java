package org.openrewrite.starter;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;

import static org.openrewrite.starter.gradle.GradleConstants.PROPERTIES_MESSAGE_PREFIX;
import static org.openrewrite.starter.gradle.RecipeUtils.getterToField;
import static org.openrewrite.starter.gradle.RecipeUtils.isGetterForPlainProperty;

public class CollectTaskPlainPropertiesRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Collect task plain properties";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                return (J.ClassDeclaration) super.visitClassDeclaration(classDecl, executionContext);
            }

            @Override
            public J visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                method = (J.MethodDeclaration) super.visitMethodDeclaration(method, executionContext);
                if (isGetterForPlainProperty(method)) {
                    J.ClassDeclaration classDeclaration = getCursor().firstEnclosing(J.ClassDeclaration.class);
                    String classFq = classDeclaration.getType().getFullyQualifiedName();
                    executionContext.putMessageInSet(PROPERTIES_MESSAGE_PREFIX + classFq, getterToField(method));
                }
                return method;
            }
        };
    }
}
