package com.example.webapi.fintrackautentication.repository;

import com.example.webapi.fintrackautentication.domain.UserRol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRolRepository extends JpaRepository<UserRol, Long> {
	java.util.List<UserRol> findByUserId(Long userId);
	boolean existsByUserIdAndRolId(Long userId, Long rolId);
}
