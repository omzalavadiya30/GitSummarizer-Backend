package com.gitsummarizer.backend.summary;

import com.gitsummarizer.backend.ai.RepositorySummary;
import com.gitsummarizer.backend.auth.ApiAuthenticationFilter;
import com.gitsummarizer.backend.github.GitHubRepositoryData;
import com.gitsummarizer.backend.user.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final SavedRepositorySummaryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public HistoryController(SavedRepositorySummaryRepository historyRepository, ObjectMapper objectMapper) {
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<HistoryEntry> list(@RequestAttribute(ApiAuthenticationFilter.USER_ATTRIBUTE) AppUser user) {
        return historyRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(entry -> entry.getRepositoryData() != null)
                .map(entry -> toDto(entry))
                .toList();
    }

    @PatchMapping("/{id}")
    @Transactional
    public HistoryEntry updateSaved(
            @PathVariable long id,
            @RequestBody SavedState request,
            @RequestAttribute(ApiAuthenticationFilter.USER_ATTRIBUTE) AppUser user) {
        SavedRepositorySummary entry = findOwned(id, user);
        entry.setSaved(request.saved());
        return toDto(historyRepository.save(entry));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(
            @PathVariable long id,
            @RequestAttribute(ApiAuthenticationFilter.USER_ATTRIBUTE) AppUser user) {
        findOwned(id, user);
        historyRepository.deleteByIdAndUser_Id(id, user.getId());
    }

    @DeleteMapping
    @Transactional
    public void clear(
            @RequestParam(defaultValue = "true") boolean keepSaved,
            @RequestAttribute(ApiAuthenticationFilter.USER_ATTRIBUTE) AppUser user) {
        if (keepSaved) historyRepository.deleteAllByUser_IdAndSavedFalse(user.getId());
        else historyRepository.deleteAllByUser_Id(user.getId());
    }

    private SavedRepositorySummary findOwned(long id, AppUser user) {
        return historyRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "History item was not found."));
    }

    private HistoryEntry toDto(SavedRepositorySummary entry) {
        try {
            GitHubRepositoryData repository = objectMapper.readValue(entry.getRepositoryData(), GitHubRepositoryData.class);
            RepositorySummary summary = objectMapper.readValue(entry.getSummaryText(), RepositorySummary.class);
            return new HistoryEntry(
                    String.valueOf(entry.getId()),
                    entry.getRepositoryUrl(),
                    repository.fullName(),
                    repository.description(),
                    repository.primaryLanguage(),
                    repository.stars(),
                    summary,
                    entry.isSaved(),
                    entry.getCreatedAt());
        } catch (Exception exception) {
            throw new IllegalStateException("A saved history entry could not be read.", exception);
        }
    }

    public record SavedState(boolean saved) {
    }
}
