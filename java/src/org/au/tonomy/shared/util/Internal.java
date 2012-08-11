package org.au.tonomy.shared.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * A marker annotation that identifies an element as being internal
 * so it shouldn't be called except under special circumstances, even
 * though it might be public for testing or other reasons.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Internal {

}
