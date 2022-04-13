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
import org.openrewrite.groovy.GroovyParser
import org.openrewrite.groovy.GroovyRecipeTest

class GroovyMigrateToProviderApiToRecipeTest: GroovyRecipeTest {
    override val parser: GroovyParser
        get() = GroovyParser.builder()
            .logCompilationWarningsAndErrors(true)
            .classpath("gradle-api")
            .build()

    override val recipe: Recipe
        get() = MigrateToProviderApiRecipe()

    private val testTaskDeclaration = """
        class TestTask {
            private final Property<String> property = null

            @Input
            Property<String> getProperty() {
                return property
            }
        }
    """

    @Test
    fun `replace input plain type with the Provider API`() = assertChanged(
        before = """
// TODO FIX IMPORTS
// import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

class TestTask {
    private String property

    @Input
    String getProperty() {
        return property
    }

    void setProperty(String value) {
        this.property = value
    }
}
        """,
        after = """
// TODO FIX IMPORTS
// import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

class TestTask {
    private final Property<String> property

    @Input
    Property<String> getProperty() {
        return property
    }
}
        """
    )

    @Test
    fun `replace setProperty with property$set invocations`() = assertChanged(
        before = """
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

$testTaskDeclaration

class TestPlugin {
    void apply() {
        TestTask task = new TestTask()
        task.setProperty("Demo value")
    }
}
        """,
        after = """
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

$testTaskDeclaration

class TestPlugin {
    void apply() {
        TestTask task = new TestTask()
        task.property.set("Demo value")
    }
}
        """
    )

    @Test
    fun `replace setProperty with property$set invocations for action`() = assertChanged(
        before = """
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.Action

$testTaskDeclaration

class TestPlugin {

    def <T> T register(String name, Class<T> type, Action<? super T> configurationAction) {
        return null
    }

    void apply() {
        register("testTask", TestTask) { TestTask it ->
            it.setProperty("Demo value")
        }
    }
}
        """,
        after = """
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.Action

$testTaskDeclaration

class TestPlugin {

    def <T> T register(String name, Class<T> type, Action<? super T> configurationAction) {
        return null
    }

    void apply() {
        register("testTask", TestTask) { TestTask it ->
            it.property.set("Demo value")
        }
    }
}
        """
    )

    @Test
    fun `replace setProperty with property$set invocations for closure delegate`() = assertChanged(
        before = """
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.Action

$testTaskDeclaration

class TestPlugin {

    def <T extends TestTask> T register(String name, Class<T> type, @DelegatesTo(value = TestTask.class, strategy = Closure.DELEGATE_FIRST) Closure configurationAction) {
        return null
    }

    void apply() {
        register("testTask", TestTask) {
            it.setProperty("Demo value")
        }
    }
}
        """,
        after = """
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.Action

$testTaskDeclaration

class TestPlugin {

    def <T extends TestTask> T register(String name, Class<T> type, @DelegatesTo(value = TestTask.class, strategy = Closure.DELEGATE_FIRST) Closure configurationAction) {
        return null
    }

    void apply() {
        register("testTask", TestTask) {
            it.property.set("Demo value")
        }
    }
}
        """
    )
}
