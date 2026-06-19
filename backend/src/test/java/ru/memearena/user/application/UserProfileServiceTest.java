package ru.memearena.user.application;

import org.junit.jupiter.api.Test;
import ru.memearena.error.ApiException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserProfileServiceTest {
    @Test
    void nicknameRegexAcceptsAllowedCharacters() {
        assertDoesNotThrow(() -> UserProfileService.normalize("Тест_User-123"));
        assertDoesNotThrow(() -> UserProfileService.normalize("Latin_123-name"));
    }

    @Test
    void nicknameRegexRejectsSpacesAndSpecialCharacters() {
        assertThrows(ApiException.class, () -> UserProfileService.normalize("bad name"));
        assertThrows(ApiException.class, () -> UserProfileService.normalize("bad$name"));
    }
}
