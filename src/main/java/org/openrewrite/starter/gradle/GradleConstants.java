package org.openrewrite.starter.gradle;

import org.openrewrite.java.tree.JavaType;

import java.util.regex.Pattern;

public class GradleConstants {

    public static final String PROPERTIES_MESSAGE_PREFIX = "plain-properties:";
    public static final String PROPERTY_FQ = "org.gradle.api.provider.Property";
    public static final JavaType PROPERTY_TYPE = JavaType.buildType(PROPERTY_FQ);
    public static final Pattern PROPERTY_PATTERN = Pattern.compile(PROPERTY_FQ);
    public static final Pattern INPUT_ANNOTATION_PATTERN = Pattern.compile("org.gradle.api.tasks.Input");

}
