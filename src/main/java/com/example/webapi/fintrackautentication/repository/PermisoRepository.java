package com.example.webapi.fintrackautentication.repository;

import com.example.webapi.fintrackautentication.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
}
