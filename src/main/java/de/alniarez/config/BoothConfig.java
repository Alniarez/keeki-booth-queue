package de.alniarez.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
        return new BoothConfig(
            LocalTime.parse(props.getProperty("opening_time")),
            LocalTime.parse(props.getProperty("closing_time")),
            Duration.ofMinutes(Long.parseLong(props.getProperty("block_duration_minutes"))),
            Integer.parseInt(props.getProperty("slots_per_block")),
            props.getProperty("username"),
            props.getProperty("password"),
            Boolean.parseBoolean(props.getProperty("dev_mode", "false"))
        );
    }
}
