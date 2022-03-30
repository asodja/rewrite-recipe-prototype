package org.openrewrite.starter.gradle;

import java.util.regex.Pattern;

public class GradleConstants {

    public static final String PROPERTIES_MESSAGE_PREFIX = "plain-properties:";
    public static final String PROPERTY_NAME = "org.gradle.api.provider.Property";
    public static final Pattern PROPERTY_PATTERN = Pattern.compile(PROPERTY_NAME);
    public static final Pattern INPUT_ANNOTATION_PATTERN = Pattern.compile("org.gradle.api.tasks.Input");

}
