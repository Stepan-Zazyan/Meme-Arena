package ru.memearena.user.infrastructure;
import org.springframework.data.jpa.repository.*; import ru.memearena.user.domain.UserProfile; import java.util.*;
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>, JpaSpecificationExecutor<UserProfile> { boolean existsByNicknameIgnoreCase(String nickname); Optional<UserProfile> findByNicknameIgnoreCase(String nickname); }
