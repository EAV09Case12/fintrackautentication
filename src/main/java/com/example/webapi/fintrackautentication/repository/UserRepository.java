package com.example.webapi.fintrackautentication.repository;

import com.example.webapi.fintrackautentication.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

	@Query("select u from User u left join fetch u.usuarioRoles ur left join fetch ur.rol where u.email = :email")
	Optional<User> findByEmailWithRoles(@Param("email") String email);
}
