package de.alniarez;

import de.alniarez.config.BoothConfig;
import de.alniarez.config.TimeSlot;
import de.alniarez.store.BookingStore;
import de.alniarez.store.Database;
import de.alniarez.store.Slot;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinFreemarker;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Base64;

public class Main {

    static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    static final int SESSION_MAX_AGE = 60 * 60 * 24;

    public static void main(String[] args) throws Exception {
        BoothConfig config = BoothConfig.load(Path.of("booth.properties"));
        Database db = new Database("./data/bookings");
        BookingStore store = new BookingStore(db);
        List<TimeSlot> blocks = config.generateBlocks();

        Map<String, Instant> sessions = new ConcurrentHashMap<>();

        Javalin.create(cfg -> {
            cfg.staticFiles.add("/static");
            cfg.fileRenderer(new JavalinFreemarker());

            cfg.routes.before(ctx -> {
                if (config.devMode()) return;

                // Valid session cookie → allow through
                String token = ctx.cookie("session");
                if (token != null) {
                    Instant expiry = sessions.get(token);
                    if (expiry != null && Instant.now().isBefore(expiry)) return;
                    sessions.remove(token);
                }

                // No valid session → check Basic Auth
                if (!isAuthorized(ctx.header("Authorization"), config.username(), config.password())) {
                    ctx.header("WWW-Authenticate", "Basic realm=\"Keeki Booth\"");
                    ctx.status(401).result("Unauthorized").skipRemainingHandlers();
                    return;
                }

                String newToken = UUID.randomUUID().toString();
                sessions.put(newToken, Instant.now().plus(Duration.ofDays(1)));
                ctx.cookie("session", newToken, SESSION_MAX_AGE);
            });

            cfg.routes.get("/", ctx -> {
                String isoDate = ctx.queryParam("date");
                if (isoDate == null || isoDate.isBlank()) isoDate = LocalDate.now().toString();
                LocalDate localDate = LocalDate.parse(isoDate);
                List<Map<String, Object>> blockData = buildBlockData(blocks, store, isoDate, config.slotsPerBlock());
                ctx.render("/templates/index.ftl", Map.of(
                    "date", localDate.format(DISPLAY_DATE),
                    "isoDate", isoDate,
                    "blocks", blockData
                ));
            });

            cfg.routes.post("/book", ctx -> {
                String name = ctx.formParam("name");
                String time = ctx.formParam("time");
                String date = ctx.formParam("date");

                if (name == null || name.isBlank() || time == null || time.isBlank() || date == null || date.isBlank()) {
                    ctx.status(400).result("{\"error\":\"Missing name, time, or date\"}");
                    return;
                }

                Slot slot = store.book(date, time, name.trim(), config.slotsPerBlock());
                if (slot == null) {
                    ctx.status(409).result("{\"error\":\"Block is full\"}");
                    return;
                }

                ctx.contentType("application/json")
                   .result("{\"code\":\"" + slot.code() + "\"}");
            });

            cfg.routes.get("/admin", ctx -> {
                String date = ctx.queryParam("date");
                if (date == null || date.isBlank()) date = LocalDate.now().toString();
                LocalDate localDate = LocalDate.parse(date);
                List<Map<String, Object>> blockData = buildBlockData(blocks, store, date, config.slotsPerBlock());
                Map<String, Object> model = new HashMap<>();
                model.put("date", date);
                model.put("dateFormatted", localDate.format(DISPLAY_DATE));
                model.put("blocks", blockData);
                ctx.render("/templates/admin.ftl", model);
            });

            cfg.routes.post("/booking/delete", ctx -> {
                String date = ctx.formParam("date");
                String time = ctx.formParam("time");
                String code = ctx.formParam("code");
                store.delete(date, time, code);
                ctx.redirect("/admin?date=" + date);
            });

        }).start(7070);
    }

    private static List<Map<String, Object>> buildBlockData(List<TimeSlot> blocks, BookingStore store, String date, int total) {
        return blocks.stream()
            .map(b -> {
                List<Slot> booked = store.getBookings(date, b.startTime().toString());
                List<Map<String, String>> slots = booked.stream()
                    .map(s -> Map.of("name", s.name(), "code", s.code()))
                    .collect(Collectors.toList());
                int taken = booked.size();
                Map<String, Object> m = new HashMap<>();
                m.put("time", b.startTime().toString());
                m.put("slots", slots);
                m.put("taken", taken);
                m.put("total", total);
                m.put("pct", total > 0 ? taken * 100 / total : 0);
                return m;
            })
            .collect(Collectors.toList());
    }

    private static boolean isAuthorized(String authHeader, String username, String password) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) return false;
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        String[] parts = decoded.split(":", 2);
        return parts.length == 2 && parts[0].equals(username) && parts[1].equals(password);
    }

}
