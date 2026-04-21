package com.example.webapi.fintrackautentication.repository;

import com.example.webapi.fintrackautentication.domain.AuditoriaAtenticacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<AuditoriaAtenticacion, Long> {
}
