package io.github.wysohn.triggerreactor.core.manager.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark a field that should be injected or updated at runtime.
 * Unless the field in the Trigger class is marked with this annotation, no such
 * field should exist as any mutable state in a Trigger must be stored in the
 * TriggerInfo instead {@link io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TriggerRuntimeDependency {
}
