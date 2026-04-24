package com.example.authadmin.repository;

import com.example.authadmin.entity.GeocodeCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeocodeCacheRepository extends JpaRepository<GeocodeCache, Integer> {
    Optional<GeocodeCache> findByQueryKey(String queryKey);
}
