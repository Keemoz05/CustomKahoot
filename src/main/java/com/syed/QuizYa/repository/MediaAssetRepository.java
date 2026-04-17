package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * REPOSITORY: MediaAssetRepository
 *
 * Extends JpaRepository to give us standard CRUD methods for the media_assets table
 * (save, findById, deleteById, etc.) without writing any SQL.
 */
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}
