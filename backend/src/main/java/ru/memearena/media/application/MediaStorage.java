package ru.memearena.media.application;
import java.io.*;
public interface MediaStorage { void put(String key, InputStream data, long size, String contentType); InputStream get(String key); void delete(String key); }
