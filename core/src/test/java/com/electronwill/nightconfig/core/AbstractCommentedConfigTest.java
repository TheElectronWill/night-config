package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.concurrent.StampedConfig;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author TheElectronWill
 */
class AbstractCommentedConfigTest {

	private static Stream<Arguments> commentCopyOperations() {
		Function<CommentedConfig, CommentedConfig> copy =
				config -> CommentedConfig.copy(config, LinkedHashMap::new);
		Function<CommentedConfig, CommentedConfig> copyWithFormat =
				config -> CommentedConfig.copy(config, LinkedHashMap::new,
						InMemoryCommentedFormat.defaultInstance());
		Function<CommentedConfig, CommentedConfig> clone =
				config -> ((AbstractCommentedConfig)config).clone();
		return Stream.of(
				Arguments.of("copy with map creator", copy),
				Arguments.of("copy with map creator and format", copyWithFormat),
				Arguments.of("clone", clone));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("commentCopyOperations")
	public void testCopyComments(String name, Function<CommentedConfig, CommentedConfig> copy) {
		CommentedConfig config = CommentedConfig.of(LinkedHashMap::new,
				InMemoryCommentedFormat.defaultInstance());
		config.set("key", "value");
		config.setComment("key", "comment");
		config.setComment("commentOnly", "comment without a value");

		CommentedConfig copied = copy.apply(config);
		assertEquals("comment", copied.getComment("key"));
		assertEquals("comment without a value", copied.getComment("commentOnly"));

		copied.setComment("key", "changed copy");
		assertEquals("comment", config.getComment("key"));
		config.setComment("commentOnly", "changed original");
		assertEquals("comment without a value", copied.getComment("commentOnly"));
	}

	@Test
	public void testCopyCommentsWithoutCommentMap() {
		CommentedConfig config = new StampedConfig();
		config.set("key", "value");
		config.setComment("key", "comment");
		config.set("uncommented", "value without a comment");

		CommentedConfig copied = CommentedConfig.copy(config, ConcurrentHashMap::new);
		assertEquals("comment", copied.getComment("key"));
		assertFalse(copied.containsComment("uncommented"));

		copied.setComment("key", "changed copy");
		assertEquals("comment", config.getComment("key"));
	}

	@Test
	public void testClearComments() {
		CommentedConfig config = CommentedConfig.inMemory();
		config.set("a", "a");
		config.setComment("a", "commentA");

		CommentedConfig sub = CommentedConfig.inMemory();
		sub.set("b", "b");
		sub.setComment("b", "commentB");
		config.set("sub", sub);
		config.setComment("sub", "commentSub");

		assertEquals(config.getComment("a"), "commentA");
		assertEquals(config.getComment("sub.b"), "commentB");
		assertEquals(config.getComment("sub"), "commentSub");
		assertEquals(sub.getComment("b"), "commentB");

		for (CommentedConfig.Entry entry : config.entrySet()) {
			assertNotNull(entry.getComment());
		}

		config.clearComments();

		assertNull(config.getComment("a"));
		assertNull(config.getComment("sub.b"));
		assertNull(config.getComment("sub"));
		assertNull(sub.getComment("b"));

		for (CommentedConfig.Entry entry : config.entrySet()) {
			assertNull(entry.getComment());
			assertNotNull(entry.getValue());
		}
	}

}