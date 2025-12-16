package com.hajducakmarek.fixit.utils

import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.Photo
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.CommentWithUser
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.PDFKit.*

actual class PdfExporter {

    private val pageWidth = 595.0 // A4 width in points
    private val pageHeight = 842.0 // A4 height in points
    private val margin = 50.0

    actual suspend fun exportIssueToPdf(
        issue: Issue,
        photos: List<Photo>,
        comments: List<CommentWithUser>,
        creator: User,
        assignedWorker: User?
    ): String {
        val fileName = "issue_${issue.flatNumber.replace("/", "-")}_${NSDate().timeIntervalSince1970.toLong()}.pdf"
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String
        val filePath = "$documentsPath/$fileName"

        // Create PDF context
        val pdfPath = filePath as NSString
        UIGraphicsBeginPDFContextToFile(pdfPath, CGRectMake(0.0, 0.0, pageWidth, pageHeight), null)
        UIGraphicsBeginPDFPage()

        val context = UIGraphicsGetCurrentContext()
        var yPosition = margin + 40.0

        // Title
        val titleAttributes = mapOf(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(24.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        drawText("Issue Report", margin, yPosition, titleAttributes)
        yPosition += 40.0

        // Issue details
        val labelAttributes = mapOf(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(14.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        val valueAttributes = mapOf(
            NSFontAttributeName to UIFont.systemFontOfSize(14.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )

        // Flat Number
        drawText("Flat Number:", margin, yPosition, labelAttributes)
        drawText(issue.flatNumber, margin + 150.0, yPosition, valueAttributes)
        yPosition += 30.0

        // Status
        drawText("Status:", margin, yPosition, labelAttributes)
        drawText(PdfHelper.getStatusText(issue.status), margin + 150.0, yPosition, valueAttributes)
        yPosition += 30.0

        // Priority
        drawText("Priority:", margin, yPosition, labelAttributes)
        drawText(PdfHelper.getPriorityText(issue.priority), margin + 150.0, yPosition, valueAttributes)
        yPosition += 30.0

        // Created By
        drawText("Created By:", margin, yPosition, labelAttributes)
        drawText(creator.name, margin + 150.0, yPosition, valueAttributes)
        yPosition += 30.0

        // Assigned To
        drawText("Assigned To:", margin, yPosition, labelAttributes)
        drawText(assignedWorker?.name ?: "Not assigned", margin + 150.0, yPosition, valueAttributes)
        yPosition += 30.0

        // Created At
        drawText("Created At:", margin, yPosition, labelAttributes)
        drawText(PdfHelper.formatDate(issue.createdAt), margin + 150.0, yPosition, valueAttributes)
        yPosition += 30.0

        // Due Date
        issue.dueDate?.let {
            drawText("Due Date:", margin, yPosition, labelAttributes)
            val dueDateText = PdfHelper.formatDate(it)
            val isOverdue = it < System.currentTimeMillis() && issue.status != com.hajducakmarek.fixit.models.IssueStatus.VERIFIED

            if (isOverdue) {
                val overdueAttributes = mapOf(
                    NSFontAttributeName to UIFont.systemFontOfSize(14.0),
                    NSForegroundColorAttributeName to UIColor.redColor
                )
                drawText(dueDateText, margin + 150.0, yPosition, overdueAttributes)
                drawText("(OVERDUE)", margin + 300.0, yPosition, overdueAttributes)
            } else {
                drawText(dueDateText, margin + 150.0, yPosition, valueAttributes)
            }
            yPosition += 30.0
        }

        // Description
        yPosition += 10.0
        drawText("Description:", margin, yPosition, labelAttributes)
        yPosition += 25.0

        // Wrap description text
        val descriptionLines = wrapText(issue.description, pageWidth - (margin * 2), 14.0)
        descriptionLines.forEach { line ->
            drawText(line, margin, yPosition, valueAttributes)
            yPosition += 20.0
        }

        // Photos count
        if (photos.isNotEmpty()) {
            yPosition += 20.0
            drawText("Photos: ${photos.size}", margin, yPosition, labelAttributes)
            yPosition += 25.0
        }

        // Comments count
        if (comments.isNotEmpty()) {
            yPosition += 10.0
            drawText("Comments: ${comments.size}", margin, yPosition, labelAttributes)
            yPosition += 25.0
        }

        UIGraphicsEndPDFContext()

        return filePath
    }

    actual suspend fun exportAllIssuesToPdf(issues: List<Issue>): String {
        val fileName = "all_issues_${NSDate().timeIntervalSince1970.toLong()}.pdf"
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String
        val filePath = "$documentsPath/$fileName"

        // Create PDF context
        val pdfPath = filePath as NSString
        UIGraphicsBeginPDFContextToFile(pdfPath, CGRectMake(0.0, 0.0, pageWidth, pageHeight), null)
        UIGraphicsBeginPDFPage()

        var yPosition = margin + 40.0

        // Title
        val titleAttributes = mapOf(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(24.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        drawText("All Issues Report", margin, yPosition, titleAttributes)
        yPosition += 30.0

        // Metadata
        val metaAttributes = mapOf(
            NSFontAttributeName to UIFont.systemFontOfSize(12.0),
            NSForegroundColorAttributeName to UIColor.grayColor
        )
        drawText("Total Issues: ${issues.size}", margin, yPosition, metaAttributes)
        yPosition += 20.0
        drawText("Generated: ${PdfHelper.formatDate(System.currentTimeMillis())}", margin, yPosition, metaAttributes)
        yPosition += 40.0

        // Table header
        val headerAttributes = mapOf(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(12.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        drawText("Flat #", margin, yPosition, headerAttributes)
        drawText("Status", margin + 80.0, yPosition, headerAttributes)
        drawText("Priority", margin + 180.0, yPosition, headerAttributes)
        drawText("Due Date", margin + 280.0, yPosition, headerAttributes)
        yPosition += 25.0

        // Draw line
        val context = UIGraphicsGetCurrentContext()
        context?.let {
            CGContextSetStrokeColorWithColor(it, UIColor.blackColor.CGColor)
            CGContextSetLineWidth(it, 2.0)
            CGContextMoveToPoint(it, margin, yPosition)
            CGContextAddLineToPoint(it, pageWidth - margin, yPosition)
            CGContextStrokePath(it)
        }
        yPosition += 15.0

        // Table rows
        val rowAttributes = mapOf(
            NSFontAttributeName to UIFont.systemFontOfSize(10.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )

        issues.take(25).forEach { issue ->  // Limit to 25 issues per page
            if (yPosition > pageHeight - margin) {
                return@forEach // Stop if page is full
            }

            drawText(issue.flatNumber, margin, yPosition, rowAttributes)
            drawText(issue.status.name, margin + 80.0, yPosition, rowAttributes)
            drawText(issue.priority.name, margin + 180.0, yPosition, rowAttributes)
            val dueDateText = issue.dueDate?.let { PdfHelper.formatDate(it) } ?: "None"
            drawText(dueDateText, margin + 280.0, yPosition, rowAttributes)
            yPosition += 20.0
        }

        UIGraphicsEndPDFContext()

        return filePath
    }

    private fun drawText(text: String, x: Double, y: Double, attributes: Map<Any?, *>) {
        val nsText = text as NSString
        val point = CGPointMake(x, y)
        nsText.drawAtPoint(point, withAttributes = attributes)
    }

    private fun wrapText(text: String, maxWidth: Double, fontSize: Double): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""

        val font = UIFont.systemFontOfSize(fontSize)
        val attributes = mapOf(NSFontAttributeName to font)

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val size = (testLine as NSString).sizeWithAttributes(attributes)

            if (size.useContents { this.width } > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}