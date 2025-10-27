package com.indexer;

import io.javalin.Javalin;
import java.util.Map;

public class IndexApp {

    private static final IndexService indexService = new IndexService();

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json")
                .start(7002);

        System.out.println("[INDEXER] Service started on http://localhost:7002");

        app.get("/status", ctx ->
                ctx.json(Map.of("service", "indexer", "status", "running"))
        );

        app.get("/index/status/{id}", ctx -> {
            int bookId = Integer.parseInt(ctx.pathParam("id"));
            boolean indexed = indexService.isIndexed(bookId);
            ctx.json(Map.of(
                    "book_id", bookId,
                    "status", indexed ? "indexed" : "not_indexed"
            ));
        });


        app.post("/index/update/{id}", ctx -> {
            int bookId = Integer.parseInt(ctx.pathParam("id"));
            System.out.println("[INDEXER] Building index for book " + bookId);

            Map<String, Object> result = indexService.buildIndex(bookId);
            ctx.json(result);
        });
    }
}

