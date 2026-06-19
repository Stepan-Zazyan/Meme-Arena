package ru.memearena.media.api;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.memearena.media.application.MediaService;
import ru.memearena.ratelimit.RateLimiterService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Validated
@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService service;
    private final RateLimiterService limits;

    public MediaController(MediaService service, RateLimiterService limits) {
        this.service = service;
        this.limits = limits;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(@Deprecated @RequestParam(required=false) UUID userId, @RequestPart MultipartFile file) {
        userId = ru.memearena.security.CurrentUser.required().userId();
        limits.check("upload-hour", userId.toString(), 10, 3600);
        limits.check("upload-day", userId.toString(), 30, 86400);
        var result = service.upload(userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(MediaUploadResponse.of(result.asset(), result.duplicateDetected()));
    }

    @GetMapping("/{mediaId}/content")
    public ResponseEntity<InputStreamResource> content(
            @PathVariable UUID mediaId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    ) {
        var content = service.content(mediaId);
        if (content.etag().equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(content.etag()).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.sizeBytes())
                .eTag(content.etag())
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .body(new InputStreamResource(content.stream()));
    }
}
