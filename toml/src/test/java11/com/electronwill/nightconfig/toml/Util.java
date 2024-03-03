package com.electronwill.nightconfig.toml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.List;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.sharedtests.TestEnum;

public class Util {
    static void checkExample(CommentedConfig config) {
        assertEquals("TOML Example", config.get("title"));
        assertInstanceOf(CommentedConfig.class, config.get("owner"));
        assertEquals("Tom Preston-Werner", config.get("owner.name"));

        Temporal date = config.get("owner.dob");
        assertEquals(1979, date.get(ChronoField.YEAR));
        assertEquals(05, date.get(ChronoField.MONTH_OF_YEAR));
        assertEquals(27, date.get(ChronoField.DAY_OF_MONTH));
        assertEquals(07, date.get(ChronoField.HOUR_OF_DAY));
        assertEquals(32, date.get(ChronoField.MINUTE_OF_HOUR));
        assertEquals(00, date.get(ChronoField.SECOND_OF_MINUTE));
        assertEquals(-8 * 3600, date.get(ChronoField.OFFSET_SECONDS));

        assertEquals("192.168.1.1", config.get("database.server"));
        assertEquals(List.of(8000, 8001, 8002), config.get("database.ports"));
        assertEquals(5000, config.<Integer>get("database.connection_max"));
        assertTrue(config.<Boolean>get("database.enabled"));

        assertEquals(" Indentation (tabs and/or spaces) is allowed but not required",
                config.getComment("servers.alpha"));
        assertEquals("10.0.0.1", config.get("servers.alpha.ip"));
        assertEquals("eqdc10", config.get("servers.alpha.dc"));

        assertEquals("10.0.0.2", config.get("servers.beta.ip"));
        assertEquals("eqdc10", config.get("servers.beta.dc"));

        assertEquals(List.of(List.of("gamma", "delta"), List.of(1, 2)), config.get("clients.data"));
        assertEquals(List.of("alpha", "omega"), config.get("clients.hosts"));
        // assertEquals(" Line breaks are OK when inside arrays", config.getComment("clients.hosts"));
    }

    static void populateTest(CommentedConfig config) {
        Config subConfig = config.createSubConfig();
        subConfig.set("dateTime",
            ZonedDateTime.of(2024, 05, 02, 13, 17, 38, 777, ZoneOffset.ofHours(+1)));
        subConfig.set("sub", config.createSubConfig());
        subConfig.set("string", "test");

        List<Config> arrayOfTables = List.of(subConfig, subConfig, subConfig);

        config.set("bool_array", List.of(true, false, true, false));
        config.set("string", "\"value\"");
        config.set("double", 3.1415926535);
        config.set("integer", 2);
        config.set("long", 123456789L);
        config.set("enum", TestEnum.A);
        config.set("config", subConfig);
        config.set("table_array2", arrayOfTables);
        config.set("table_array", arrayOfTables);
    }

    static final String EXPECTED_SERIALIZED = "bool_array = [\n" + //
            "	true,\n" + //
            "	false,\n" + //
            "	true,\n" + //
            "	false\n" + //
            "]\n" + //
            "string = \"\\\"value\\\"\"\n" + //
            "double = 3.1415926535\n" + //
            "integer = 2\n" + //
            "long = 123456789\n" + //
            "enum = \"A\"\n" + //
            "\n" + //
            "[config]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "\n" + //
            "[[table_array2]]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "[[table_array2]]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "[[table_array2]]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "\n" + //
            "[[table_array]]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "[[table_array]]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "[[table_array]]\n" + //
            "	dateTime = 2024-05-02T13:17:38.000000777+01:00\n" + //
            "	sub = {}\n" + //
            "	string = \"test\"\n" + //
            "";
}
