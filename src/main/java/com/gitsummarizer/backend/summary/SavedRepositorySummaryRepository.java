package com.gitsummarizer.backend.summary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedRepositorySummaryRepository
        extends JpaRepository<SavedRepositorySummary, Long> {

    List<SavedRepositorySummary> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}