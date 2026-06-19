package ru.memearena.ratelimit; public interface RateLimiterService { void check(String name,String key,int limit,long windowSeconds); }
