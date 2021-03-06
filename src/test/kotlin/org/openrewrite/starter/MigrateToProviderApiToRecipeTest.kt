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
package org.openrewrite.starter

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaRecipeTest

class MigrateToProviderApiToRecipeTest: JavaRecipeTest {
    override val parser: JavaParser
        get() = JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("gradle-api")
            .build()

    override val recipe: Recipe
        get() = MigrateToProviderApiRecipe()

    @Test
    fun `replace input plain type with the Provider API`() = assertChanged(
        before = """
import org.gradle.api.tasks.Input;

class TestTask {
    private String property;

    @Input
    public String getProperty() {
        return property;
    }

    public void setProperty(String value) {
        this.property = value;
    }
}
        """,
        after = """
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

class TestTask {
    private final Property<String> property;

    @Input
    public Property<String> getProperty() {
        return property;
    }
}
        """
    )

    @Test
    fun `replace setProperty with property$set invocations`() = assertChanged(
        dependsOn = arrayOf(
            """
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

class TestTask {
    private final Property<String> property = null;

    @Input
    public Property<String> getProperty() {
        return property;
    }
}
    """
        ),
        before = """
class TestPlugin {
    public void apply() {
        TestTask task = new TestTask();
        task.setProperty("Demo value");
    }
}
        """,
        after = """
class TestPlugin {
    public void apply() {
        TestTask task = new TestTask();
        task.getProperty().set("Demo value");
    }
}
        """
    )
}
