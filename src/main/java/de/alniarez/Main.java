package de.alniarez;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinFreemarker;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Javalin.create(config -> {
            config.staticFiles.add("/static");
            config.fileRenderer(new JavalinFreemarker());
            config.routes.get("/", ctx -> ctx.render("/templates/index.ftl", Map.of("name", "World")));
        }).start(7070);
    }

}
