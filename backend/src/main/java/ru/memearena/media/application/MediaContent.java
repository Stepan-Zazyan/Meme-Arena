package ru.memearena.media.application; import java.io.InputStream; public record MediaContent(InputStream stream,String contentType,long sizeBytes,String etag){}
