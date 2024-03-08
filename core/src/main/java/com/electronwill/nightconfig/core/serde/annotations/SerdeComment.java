package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;

/**
 * Defines the comment to write to the configuration for this field.
 * <p>
 * The comment is only added to the configuration we're serializing to if it's a
 * {@link CommentedConfig}.
 * When writing the configuration to a file (with a {@link ConfigWriter}), the
 * comments are only written if that's supported by the configuration format.
 *
 * <h2>Example</h2>
 * <pre><code>
 * class Player {
 *     {@code @SerdeComment}("The username of the Player, as reported by the database.")
 *     String username;
 * }
 * </code></pre>
 *
 * <h2>Example: multiline comment</h2>
 * <pre><code>
 * class MyObject {
 *     {@code @SerdeComment}("First line of comment.")
 *     {@code @SerdeComment}("Second line of comment.")
 *     String someStringField;
 * }
 * </code></pre>
 */
@Repeatable(SerdeCommentsContainer.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeComment {
	String value();
}
