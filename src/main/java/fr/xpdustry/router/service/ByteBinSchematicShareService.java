/*
 * Router, a plugin for sharing schematics.
 *
 * Copyright (C) 2022 Xpdustry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.xpdustry.router.service;

import com.alibaba.fastjson2.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

public final class ByteBinSchematicShareService implements SchematicShareService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String address;

    public ByteBinSchematicShareService(final String address) {
        this.address = address;
    }

    @Override
    public CompletableFuture<URI> upload(final Schematic schematic) {
        final String base64;
        try (final var out = new ByteArrayOutputStream()) {
            Schematics.write(schematic, out);
            base64 = Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (final IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return client.sendAsync(
                        HttpRequest.newBuilder()
                                .uri(URI.create(address + "/post"))
                                .header("Content-Type", "text/plain")
                                .POST(BodyPublishers.ofString(base64))
                                .build(),
                        BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    final var id = JSONObject.parse(response.body()).getString("key");
                    return URI.create(address + "/" + id);
                });
    }
}
