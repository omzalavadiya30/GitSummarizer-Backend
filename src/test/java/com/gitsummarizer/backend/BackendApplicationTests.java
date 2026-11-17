package com.gitsummarizer.backend;

import com.gitsummarizer.backend.auth.PasswordHasher;
import com.gitsummarizer.backend.github.GitHubRepositoryReference;
import com.gitsummarizer.backend.github.InvalidGitHubRepositoryUrlException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackendApplicationTests {

    @Test
    void parsesRepositoryUrlsAndGitSuffixes() {
        GitHubRepositoryReference reference =
                GitHubRepositoryReference.parse("https://github.com/owner/project.git");

        assertEquals("owner", reference.owner());
        assertEquals("project", reference.repository());
    }

    @Test
    void rejectsNonRepositoryPaths() {
        assertThrows(InvalidGitHubRepositoryUrlException.class,
                () -> GitHubRepositoryReference.parse("https://github.com/owner/project/tree/main"));
    }

    @Test
    void verifiesSaltedPasswordHashes() {
        PasswordHasher hasher = new PasswordHasher();
        String hash = hasher.hash("SmokeTest123");

        assertTrue(hasher.matches("SmokeTest123", hash));
        assertFalse(hasher.matches("NotThePassword123", hash));
        assertNotEquals(hash, hasher.hash("SmokeTest123"));
    }
}
