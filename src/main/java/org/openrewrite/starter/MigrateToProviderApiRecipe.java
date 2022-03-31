package org.openrewrite.starter;

import org.openrewrite.Recipe;

public class MigrateToProviderApiRecipe extends Recipe {

    public MigrateToProviderApiRecipe() {
        doNext(new CollectTaskPlainPropertiesRecipe());
        doNext(new MigrateTaskPropertiesToProviderApiRecipe());
        doNext(new MigratePropertySetInvocationsRecipe());
    }

    @Override
    public String getDisplayName() {
        return "Migrate Gradle task to the Provider API";
    }
}
