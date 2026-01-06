package com.msrecette.univ.soa.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CacheController {

    private final CacheManager cacheManager;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        log.info("GET /api/cache/stats - Récupération des statistiques du cache");

        Map<String, Object> stats = new HashMap<>();

        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, String> cacheInfo = new HashMap<>();
                    cacheInfo.put("name", cacheName);
                    cacheInfo.put("type", cache.getClass().getSimpleName());
                    stats.put(cacheName, cacheInfo);
                }
            });
        }

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        log.info("DELETE /api/cache - Effacement de tous les caches");

        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Cache vidé: {}", cacheName);
                }
            });
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "Tous les caches ont été vidés avec succès");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        log.info("DELETE /api/cache/{} - Effacement du cache", cacheName);

        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache {} vidé avec succès", cacheName);

            Map<String, String> response = new HashMap<>();
            response.put("status", "Cache " + cacheName + " vidé avec succès");
            return ResponseEntity.ok(response);
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", "Cache " + cacheName + " non trouvé");
        return ResponseEntity.badRequest().body(response);
    }
}

