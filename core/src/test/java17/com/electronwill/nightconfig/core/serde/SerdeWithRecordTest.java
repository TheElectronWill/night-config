package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;

/**
 * Serialization/deserialization of records, a new feature of Java 16.
 */
public class SerdeWithRecordTest {
    static record Point3d(int x, int y, int z) {
    }

    static record Name(String username, String nickname) {
    }

    static class Player {
        int id = 123;
        Point3d blockPosition = new Point3d(-56, 25, 789);
        Name name = new Name("ElectronWill", "Will");

        @Override
        public String toString() {
            return "Player [id=" + id + ", blockPosition=" + blockPosition + ", name=" + name + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Player))
                return false;
            Player other = (Player) obj;
            return id == other.id && Objects.equals(blockPosition, other.blockPosition)
                    && Objects.equals(name, other.name);
        }

        static final Config SERIALIZED;
        static {
            SERIALIZED = Config.inMemory();
            SERIALIZED.set("id", 123);
            SERIALIZED.set("blockPosition.x", -56);
            SERIALIZED.set("blockPosition.y", 25);
            SERIALIZED.set("blockPosition.z", 789);
            SERIALIZED.set("name.username", "ElectronWill");
            SERIALIZED.set("name.nickname", "Will");
        }
    }

    static class Engine {
        List<Player> playersById = Arrays.asList(new Player(), null);
        Map<String, Player> playersByUsername = Map.of("ElectronWill", new Player());

        @Override
        public String toString() {
            return "Engine [playersById=" + playersById + ", playersByUsername=" + playersByUsername
                    + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Engine))
                return false;
            Engine other = (Engine) obj;
            return Objects.equals(playersById, other.playersById)
                    && Objects.equals(playersByUsername, other.playersByUsername);
        }

        static final Config SERIALIZED;
        static {
            SERIALIZED = Config.inMemory();
            SERIALIZED.set("playersById", Arrays.asList(Player.SERIALIZED, null));
            var sub = Config.inMemory();
            sub.set("ElectronWill", Player.SERIALIZED);
            SERIALIZED.set("playersByUsername", sub);
        }
    }

    @Test
    public void testRecordsDirect() {
        var conf = Config.inMemory();
        conf.set("x", 123);
        conf.set("y", 456);
        conf.set("z", 789);
        var deserialized = ObjectDeserializer.builder().build().deserializeToRecord(conf, Point3d.class);
        assertEquals(new Point3d(123, 456, 789), deserialized);

        var serialized = ObjectSerializer.builder().build().serialize(new Point3d(123, 456, 789), Config::inMemory);
        assertEquals(conf, serialized);
    }

    @Test
    public void testFieldsRecords() {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(Player.SERIALIZED, Player::new);
        assertEquals(new Player(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Player(), Config::inMemory);
        assertEquals(Player.SERIALIZED, serialized);

        var serialized2 = ser.serialize(new Player(), CommentedConfig::inMemory);
        assertEquals(Player.SERIALIZED, serialized2);
    }

    @Test
    public void testNestedRecords() {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(Engine.SERIALIZED, Engine::new);
        assertEquals(new Engine(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Engine(), Config::inMemory);
        assertEquals(Engine.SERIALIZED, serialized);

        var serialized2 = ser.serialize(new Engine(), CommentedConfig::inMemory);
        assertEquals(Engine.SERIALIZED, serialized2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRecordsError() {
        // oops, y is missing!
        var wrong = Config.inMemory();
        wrong.set("x", 15);
        wrong.set("z", -2);

        // it should fail to deserialize
        var de = ObjectDeserializer.builder().build();
        assertThrows(DeserializationException.class, () -> {
            de.deserializeToRecord(wrong, Point3d.class);
        });

        // String is not a record but we can cheat with generics to make the call compile
        assertThrows(IllegalArgumentException.class, () -> {
            de.deserializeToRecord(wrong, (Class<? extends Record>)(Class<?>)String.class);
        });
    }
}
