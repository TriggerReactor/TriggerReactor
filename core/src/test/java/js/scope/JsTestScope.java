package js.scope;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Instances live while the plugin is enabled
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface JsTestScope {
}
