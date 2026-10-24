package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.model.Philosopher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhilosopherJpaRepository extends JpaRepository<Philosopher, UUID> {
    @Query("""
            SELECT DISTINCT f FROM Philosopher f
            LEFT JOIN FETCH f.period LEFT JOIN FETCH f.schools
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    List<Philosopher> findAllWithRelationships();

    @Query("""
            SELECT DISTINCT f FROM Philosopher f
            LEFT JOIN FETCH f.period LEFT JOIN FETCH f.schools WHERE f.id = :id
            """)
    Optional<Philosopher> findWithRelationshipsById(@Param("id") UUID id);

    boolean existsByNameIgnoreCaseAndPeriod_NameIgnoreCase(String name, String period);

    boolean existsByNameIgnoreCaseAndPeriod_NameIgnoreCaseAndIdNot(String name, String period, UUID id);
}
