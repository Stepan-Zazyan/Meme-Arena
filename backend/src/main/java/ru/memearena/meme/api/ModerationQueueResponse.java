package ru.memearena.meme.api; import java.util.*; public record ModerationQueueResponse(List<ModerationMemeItemResponse> items,int page,int size,long totalElements,int totalPages){}
