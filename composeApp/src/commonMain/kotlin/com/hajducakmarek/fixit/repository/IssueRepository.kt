package com.hajducakmarek.fixit.repository

import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.database.FixItDatabase
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.models.Comment
import com.hajducakmarek.fixit.models.CommentWithUser
import com.hajducakmarek.fixit.models.ActivityLog
import com.hajducakmarek.fixit.models.ActivityLogWithUser
import com.hajducakmarek.fixit.models.ActivityType
import com.benasher44.uuid.uuid4

class IssueRepository(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = FixItDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.fixItDatabaseQueries

    suspend fun getAllIssues(): List<Issue> {
        return try {
            dbQuery.selectAllIssues().executeAsList().map { issue ->
                Issue(
                    id = issue.id,
                    photoPath = issue.photoPath,
                    description = issue.description,
                    flatNumber = issue.flatNumber,
                    status = IssueStatus.valueOf(issue.status),
                    createdBy = issue.createdBy,
                    assignedTo = issue.assignedTo,
                    createdAt = issue.createdAt,
                    completedAt = issue.completedAt
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load issues from database", e)
        }
    }

    suspend fun getIssueById(id: String): Issue? {
        return try {
            dbQuery.selectIssueById(id).executeAsOneOrNull()?.let { issue ->
                Issue(
                    id = issue.id,
                    photoPath = issue.photoPath,
                    description = issue.description,
                    flatNumber = issue.flatNumber,
                    status = IssueStatus.valueOf(issue.status),
                    createdBy = issue.createdBy,
                    assignedTo = issue.assignedTo,
                    createdAt = issue.createdAt,
                    completedAt = issue.completedAt
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load issue details", e)
        }
    }

    suspend fun insertIssue(issue: Issue) {
        try {
            dbQuery.insertIssue(
                id = issue.id,
                photoPath = issue.photoPath,
                description = issue.description,
                flatNumber = issue.flatNumber,
                status = issue.status.name,
                createdBy = issue.createdBy,
                assignedTo = issue.assignedTo,
                createdAt = issue.createdAt,
                completedAt = issue.completedAt
            )
            logIssueCreation(issue.id, issue.createdBy)
        } catch (e: Exception) {
            throw Exception("Failed to create issue", e)
        }
    }

    suspend fun updateIssueStatus(issueId: String, newStatus: IssueStatus, userId: String) {
        try {
            // Get old status first
            val oldIssue = getIssueById(issueId)
            val oldStatus = oldIssue?.status ?: IssueStatus.OPEN

            dbQuery.updateIssueStatus(
                status = newStatus.name,
                id = issueId
            )

            logStatusChange(issueId, userId, oldStatus, newStatus)
        } catch (e: Exception) {
            throw Exception("Failed to update issue status", e)
        }
    }

    suspend fun updateIssueAssignment(issueId: String, newWorkerId: String?, userId: String) {
        try {
            // Get old assignment first
            val oldIssue = getIssueById(issueId)
            val oldWorkerId = oldIssue?.assignedTo

            dbQuery.updateIssueAssignment(
                assignedTo = newWorkerId,
                id = issueId
            )

            // Log assignment change
            logAssignmentChange(issueId, userId, oldWorkerId, newWorkerId)
        } catch (e: Exception) {
            throw Exception("Failed to assign worker", e)
        }
    }

    // User operations
    suspend fun seedUsers() {
        try {
            val existingUsers = dbQuery.selectAllUsers().executeAsList()
            if (existingUsers.isEmpty()) {
                dbQuery.insertUser("manager-1", "John Smith", UserRole.MANAGER.name)
                dbQuery.insertUser("worker-1", "Mike Johnson", UserRole.WORKER.name)
                dbQuery.insertUser("worker-2", "Sarah Williams", UserRole.WORKER.name)
            }
        } catch (e: Exception) {
            throw Exception("Failed to seed users", e)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            dbQuery.selectAllUsers().executeAsList().map { user ->
                User(
                    id = user.id,
                    name = user.name,
                    role = UserRole.valueOf(user.role)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load users", e)
        }
    }

    suspend fun getUserById(id: String): User? {
        return try {
            dbQuery.selectUserById(id).executeAsOneOrNull()?.let { user ->
                User(
                    id = user.id,
                    name = user.name,
                    role = UserRole.valueOf(user.role)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load user details", e)
        }
    }

    suspend fun insertUser(user: User) {
        try {
            dbQuery.insertUser(
                id = user.id,
                name = user.name,
                role = user.role.name
            )
        } catch (e: Exception) {
            throw Exception("Failed to add user", e)
        }
    }

    suspend fun getWorkers(): List<User> {
        return try {
            dbQuery.selectUsersByRole(UserRole.WORKER.name).executeAsList().map { user ->
                User(
                    id = user.id,
                    name = user.name,
                    role = UserRole.valueOf(user.role)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to load workers", e)
        }
    }

    // Comment operations
    suspend fun getCommentsByIssue(issueId: String): List<CommentWithUser> {
        return try {
            dbQuery.selectCommentsByIssue(issueId).executeAsList().map { commentData ->
                val comment = Comment(
                    id = commentData.id,
                    issueId = commentData.issueId,
                    userId = commentData.userId,
                    text = commentData.text,
                    createdAt = commentData.createdAt
                )
                val user = getUserById(commentData.userId) ?: User(
                    id = commentData.userId,
                    name = "Unknown User",
                    role = UserRole.WORKER
                )
                CommentWithUser(comment, user)
            }
        } catch (e: Exception) {
            throw Exception("Failed to load comments", e)
        }
    }

    suspend fun insertComment(comment: Comment) {
        try {
            dbQuery.insertComment(
                id = comment.id,
                issueId = comment.issueId,
                userId = comment.userId,
                text = comment.text,
                createdAt = comment.createdAt
            )
            logCommentActivity(comment.issueId, comment.userId, comment.id, false)
        } catch (e: Exception) {
            throw Exception("Failed to add comment", e)
        }
    }

    suspend fun deleteComment(commentId: String, userId: String, issueId: String) {
        try {
            dbQuery.deleteComment(commentId)
            logCommentActivity(issueId, userId, commentId, true)
        } catch (e: Exception) {
            throw Exception("Failed to delete comment", e)
        }
    }

    suspend fun getActivitiesByIssue(issueId: String): List<ActivityLogWithUser> {
        return try {
            dbQuery.selectActivitiesByIssue(issueId).executeAsList().map { activityData ->
                val activity = ActivityLog(
                    id = activityData.id,
                    issueId = activityData.issueId,
                    userId = activityData.userId,
                    activityType = ActivityType.valueOf(activityData.activityType),
                    oldValue = activityData.oldValue,
                    newValue = activityData.newValue,
                    createdAt = activityData.createdAt
                )
                val user = getUserById(activityData.userId) ?: User(
                    id = activityData.userId,
                    name = "Unknown User",
                    role = UserRole.WORKER
                )
                ActivityLogWithUser(activity, user)
            }
        } catch (e: Exception) {
            throw Exception("Failed to load activity log", e)
        }
    }

    suspend fun insertActivity(activity: ActivityLog) {
        try {
            dbQuery.insertActivity(
                id = activity.id,
                issueId = activity.issueId,
                userId = activity.userId,
                activityType = activity.activityType.name,
                oldValue = activity.oldValue,
                newValue = activity.newValue,
                createdAt = activity.createdAt
            )
        } catch (e: Exception) {
            throw Exception("Failed to log activity", e)
        }
    }

    suspend fun logIssueCreation(issueId: String, userId: String) {
        val activity = ActivityLog(
            id = "activity-${uuid4()}",
            issueId = issueId,
            userId = userId,
            activityType = ActivityType.CREATED,
            oldValue = null,
            newValue = null,
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        insertActivity(activity)
    }

    suspend fun logStatusChange(issueId: String, userId: String, oldStatus: IssueStatus, newStatus: IssueStatus) {
        val activity = ActivityLog(
            id = "activity-${uuid4()}",
            issueId = issueId,
            userId = userId,
            activityType = ActivityType.STATUS_CHANGED,
            oldValue = oldStatus.name,
            newValue = newStatus.name,
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        insertActivity(activity)
    }

    suspend fun logAssignmentChange(issueId: String, userId: String, oldWorkerId: String?, newWorkerId: String?) {
        val activityType = when {
            oldWorkerId == null && newWorkerId != null -> ActivityType.ASSIGNED
            oldWorkerId != null && newWorkerId == null -> ActivityType.UNASSIGNED
            else -> ActivityType.ASSIGNED
        }

        val activity = ActivityLog(
            id = "activity-${uuid4()}",
            issueId = issueId,
            userId = userId,
            activityType = activityType,
            oldValue = oldWorkerId,
            newValue = newWorkerId,
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        insertActivity(activity)
    }

    suspend fun logCommentActivity(issueId: String, userId: String, commentId: String, isDeleted: Boolean) {
        val activity = ActivityLog(
            id = "activity-${uuid4()}",
            issueId = issueId,
            userId = userId,
            activityType = if (isDeleted) ActivityType.COMMENT_DELETED else ActivityType.COMMENT_ADDED,
            oldValue = if (isDeleted) commentId else null,
            newValue = if (!isDeleted) commentId else null,
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        insertActivity(activity)
    }
}