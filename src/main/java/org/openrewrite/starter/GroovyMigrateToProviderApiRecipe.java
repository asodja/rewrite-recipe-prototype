package org.openrewrite.starter;

import org.openrewrite.Recipe;

public class GroovyMigrateToProviderApiRecipe extends Recipe {

    public GroovyMigrateToProviderApiRecipe() {
        doNext(new GroovyMigratePropertySetInvocationsRecipe());
    }

    @Override
    public String getDisplayName() {
        return "Migrate Gradle task to the Provider API";
    }
}
