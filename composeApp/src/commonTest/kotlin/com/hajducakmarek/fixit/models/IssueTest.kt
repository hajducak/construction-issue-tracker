package com.hajducakmarek.fixit.models

import kotlin.test.*

class IssueTest {

    @Test
    fun `issue creation with required fields`() {
        // Given
        val issue = Issue(
            id = "test-1",
            photoPath = "/path/to/photo.jpg",
            description = "Test issue",
            flatNumber = "A-101",
            status = IssueStatus.OPEN,
            createdBy = "manager-1",
            assignedTo = null,
            createdAt = 1234567890L
        )

        // Then
        assertEquals("test-1", issue.id)
        assertEquals("Test issue", issue.description)
        assertEquals(IssueStatus.OPEN, issue.status)
        assertNull(issue.assignedTo)
    }

    @Test
    fun `issue copy changes only specified fields`() {
        // Given
        val original = Issue(
            id = "test-1",
            photoPath = "/photo.jpg",
            description = "Original",
            flatNumber = "A-101",
            status = IssueStatus.OPEN,
            createdBy = "manager-1",
            assignedTo = null,
            createdAt = 1234567890L
        )

        // When
        val updated = original.copy(status = IssueStatus.FIXED)

        // Then
        assertEquals(IssueStatus.FIXED, updated.status)
        assertEquals("Original", updated.description) // Unchanged
        assertEquals("test-1", updated.id) // Unchanged
    }

    @Test
    fun `issue status enum has all expected values`() {
        val allStatuses = IssueStatus.entries

        assertEquals(4, allStatuses.size)
        assertTrue(allStatuses.contains(IssueStatus.OPEN))
        assertTrue(allStatuses.contains(IssueStatus.IN_PROGRESS))
        assertTrue(allStatuses.contains(IssueStatus.FIXED))
        assertTrue(allStatuses.contains(IssueStatus.VERIFIED))
    }
}