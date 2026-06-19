package ru.memearena.meme.api; import jakarta.validation.constraints.*; public record RejectMemeRequest(@NotBlank @Size(min=3,max=500) String reason){}
