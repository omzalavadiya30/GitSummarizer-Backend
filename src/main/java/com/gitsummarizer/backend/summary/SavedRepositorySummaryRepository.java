package com.gitsummarizer.backend.summary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedRepositorySummaryRepository
        extends JpaRepository<SavedRepositorySummary, Long> {

    List<SavedRepositorySummary> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<SavedRepositorySummary> findByIdAndUser_Id(Long id, Long userId);

    void deleteByIdAndUser_Id(Long id, Long userId);

    void deleteAllByUser_IdAndSavedFalse(Long userId);

    void deleteAllByUser_Id(Long userId);
}
