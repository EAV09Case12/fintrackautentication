package com.example.webapi.fintrackautentication.repository;

import com.example.webapi.fintrackautentication.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);
	List<RefreshToken> findByUserId(Long userId);
	void deleteByUserId(Long userId);
}
