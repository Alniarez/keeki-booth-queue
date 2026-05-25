package de.alniarez.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public record BoothConfig(LocalTime openingTime, LocalTime closingTime, Duration blockDuration, int slotsPerBlock, String username, String password, boolean devMode) {

    public List<TimeSlot> generateBlocks() {
        List<TimeSlot> blocks = new ArrayList<>();
        LocalTime start = openingTime;
        while (!start.plus(blockDuration).isAfter(closingTime)) {
            blocks.add(new TimeSlot(start, start.plus(blockDuration)));
            start = start.plus(blockDuration);
        }
        return blocks;
    }

    public static BoothConfig load(Path path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        }
        Map<String, String> env = System.getenv();
        return new BoothConfig(
            LocalTime.parse(env.getOrDefault("BOOTH_OPENING_TIME", props.getProperty("opening_time"))),
            LocalTime.parse(env.getOrDefault("BOOTH_CLOSING_TIME", props.getProperty("closing_time"))),
            Duration.ofMinutes(Long.parseLong(env.getOrDefault("BOOTH_BLOCK_DURATION", props.getProperty("block_duration_minutes")))),
            Integer.parseInt(env.getOrDefault("BOOTH_SLOTS_PER_BLOCK", props.getProperty("slots_per_block"))),
            env.getOrDefault("BOOTH_USERNAME", props.getProperty("username")),
            env.getOrDefault("BOOTH_PASSWORD", props.getProperty("password")),
            Boolean.parseBoolean(env.getOrDefault("BOOTH_DEV_MODE", props.getProperty("dev_mode", "false")))
        );
    }
}
