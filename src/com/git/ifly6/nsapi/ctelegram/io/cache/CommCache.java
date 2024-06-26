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

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.NSTimeStamped;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Creates a high-level caching framework for objects which can be timestamped. Also implicitly requires, due to
 * {@link #createNewObject(String)} that the key (a string) be mappable 1:1 to the object.
 * @param <T> is time-stamped object, ie implements {@link NSTimeStamped}.
 * @since version 13
 */
public abstract class CommCache<T extends NSTimeStamped> {

    private static final Logger LOGGER = Logger.getLogger(CommCache.class.getName());
    public static final Duration DEFAULT_EXPIRATION_DURATION = Duration.ofMinutes(30);

    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private final Duration maximumAge;

    /**
     * Runnable to be executed at the end of every {@link #lookupObject(String)}. This is a transient variable and is
     * not passed in serialisation.
     */
    private transient Runnable finaliser = null;

    /** Creates empty cache with cache expiration in 10 minutes. */
    public CommCache() { this(DEFAULT_EXPIRATION_DURATION); }

    /**
     * Creates empty cache with custom cache expiration duration.
     * @param maximumAge duration
     */
    public CommCache(Duration maximumAge) {
        this.maximumAge = maximumAge;
        purge(); // not called when instantiated by Gson!
    }

    /** Purges items older than a certain age. This is not called when instantiated by Gson. */
    public void purge() { purge(this.maximumAge); }

    /**
     * Purges items older than a certain age
     * @param age required for purge
     */
    public void purge(Duration age) {
        Instant cutoff = Instant.now().minus(age);
        cache.entrySet().removeIf(entry -> entry.getValue().timestamp() == null
                || entry.getValue().timestamp().isBefore(cutoff));
    }

    /**
     * Supplies a new {@link NSTimeStamped} instance to the cache. Without a supplier, the cache cannot load data.
     * @param s is name of the object
     * @return the new instance
     */
    protected abstract T createNewObject(String s);

    /**
     * Gets object; if not present, caches object.
     * @param s pointing to object
     * @return cached object
     */
    private T getOrCacheObject(String s) {
        if (!cache.containsKey(s)) cacheObject(s);
        return cache.get(s);
    }

    /**
     * Adds an object to the cache.
     * @param s to add to cache
     */
    private void cacheObject(String s) {
        T object = createNewObject(s);
        T oldObject = cache.put(s, object);
        if (object != oldObject) // if they have different REFERENCES, they must be different objects
            LOGGER.fine(String.format("overwrote cache for element <%s>", s));
    }

    /** @returns {@code true} if {@link #finaliser} is not null */
    public boolean hasFinaliser() {
        return finaliser != null;
    }

    /**
     * Sets a finaliser to execute at the end of {@link #lookupObject(String)}. Entirely optional.
     * @param finaliser is the {@link Runnable} to execute
     */
    public void setFinaliser(Runnable finaliser) {
        this.finaliser = finaliser;
    }

    /**
     * Gets information for an object. If it does not exist, adds that object to the cache. If the cached information is
     * older than {@link #maximumAge}, it updates the cache.
     * @param s is the ref name of the object to get data for
     * @return {@link NSTimeStamped} with data no older than {@link #maximumAge}
     */
    public T lookupObject(String s) {
        return lookupObject(s, maximumAge);
    }

    /**
     * Gets information for an object. If it does not exist, adds that object to the cache. If the cached information is
     * older than requested, it updates the cache.
     * @param s         is the ref name of the object to get data for
     * @param orElseAge calls for a new object if the cached version is older than this age; capped by
     *                  {@link #maximumAge}
     * @return {@link NSTimeStamped} with cached data as up-to-date as requested
     */
    public T lookupObject(String s, Duration orElseAge) {
        s = ApiUtils.ref(s); // normalise inputs
        orElseAge = (orElseAge.compareTo(maximumAge)) > 0 ? maximumAge : orElseAge;

        T n = getOrCacheObject(s);
        Instant cutoff = Instant.now().minus(orElseAge);
        if (n.timestamp().isAfter(cutoff))
            cacheObject(s); // update cache

        if (hasFinaliser()) finaliser.run();
        return getOrCacheObject(s);
    }

}
