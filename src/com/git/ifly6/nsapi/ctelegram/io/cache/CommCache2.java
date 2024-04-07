/*
 * Copyright (c) 2024 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.nsapi.ctelegram.io.cache;

import com.git.ifly6.gson.CommuniqueGson;
import com.git.ifly6.nsapi.NSPopulatable;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.git.ifly6.CommuniqueApplication.APP_SUPPORT;

public class CommCache2<T extends NSPopulatable<?>> {

    public static final Logger LOGGER = Logger.getLogger(CommCache2.class.getName());
    public static final Duration DEFAULT_EXPIRATION_DURATION = Duration.ofDays(2);

    public final Class<T> THE_CLASS;
    public final Path PERSIST_PATH;
    public final Duration EXPIRE_IN;
    public final Map<String, T> CACHE;

    public CommCache2(Class<T> theClass, Path path, Duration expirationDuration) {
        THE_CLASS = theClass;
        PERSIST_PATH = Objects.requireNonNull(path);
        EXPIRE_IN = (expirationDuration == null) ? DEFAULT_EXPIRATION_DURATION : expirationDuration;

        Map<String, T> cache;
        try {
            cache = loadCache();

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Failed to load cache from file", e);
            cache = new HashMap<>();
        }
        CACHE = cache;
    }

    private Map<String, T> loadCache() throws IOException, JsonSyntaxException {
        Gson gson = CommuniqueGson.createNew();
        return gson.fromJson(Files.newBufferedReader(PERSIST_PATH), this.getClass()).CACHE;
    }

    public static <U extends NSPopulatable<?>> CommCache2<U> getInstance(Class<U> type) {
        return new CommCache2<U>(
                type, APP_SUPPORT.resolve("cache").resolve(type.getSimpleName() + ".json"),
                null);
    }

    private T cacheNew(String i) {
        try {
            Method method = THE_CLASS.getDeclaredMethod("newInstance", String.class);
            return (T) method.invoke(null, i);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public T lookup(String i, Duration d) {
        if (CACHE.containsKey(i)) {
            T cached = CACHE.get(i);
            if (cached.timestamp().isBefore(Instant.now().minus(d))) { return cacheNew(i); }
            return cached;
        }
        return cacheNew(i);
    }

    public T lookupNow(String i) {
        return lookup(i, Duration.ofSeconds(70));
    }

}
