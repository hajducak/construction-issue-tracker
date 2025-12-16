package com.hajducakmarek.fixit.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.Photo
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.CommentWithUser
import java.io.File
import java.io.FileOutputStream

actual class PdfExporter(private val context: Context) {

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 50

    actual suspend fun exportIssueToPdf(
        issue: Issue,
        photos: List<Photo>,
        comments: List<CommentWithUser>,
        creator: User,
        assignedWorker: User?
    ): String {
        val fileName = "issue_${issue.flatNumber.replace("/", "-")}_${System.currentTimeMillis()}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val file = File(downloadsDir, fileName)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var yPosition = margin + 40

        // Title
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        canvas.drawText("Issue Report", margin.toFloat(), yPosition.toFloat(), titlePaint)
        yPosition += 40

        // Issue details
        val labelPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        val valuePaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.BLACK
        }

        // Flat Number
        canvas.drawText("Flat Number:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        canvas.drawText(issue.flatNumber, (margin + 150).toFloat(), yPosition.toFloat(), valuePaint)
        yPosition += 30

        // Status
        canvas.drawText("Status:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        canvas.drawText(PdfHelper.getStatusText(issue.status), (margin + 150).toFloat(), yPosition.toFloat(), valuePaint)
        yPosition += 30

        // Priority
        canvas.drawText("Priority:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        canvas.drawText(PdfHelper.getPriorityText(issue.priority), (margin + 150).toFloat(), yPosition.toFloat(), valuePaint)
        yPosition += 30

        // Created By
        canvas.drawText("Created By:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        canvas.drawText(creator.name, (margin + 150).toFloat(), yPosition.toFloat(), valuePaint)
        yPosition += 30

        // Assigned To
        canvas.drawText("Assigned To:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        canvas.drawText(assignedWorker?.name ?: "Not assigned", (margin + 150).toFloat(), yPosition.toFloat(), valuePaint)
        yPosition += 30

        // Created At
        canvas.drawText("Created At:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        canvas.drawText(PdfHelper.formatDate(issue.createdAt), (margin + 150).toFloat(), yPosition.toFloat(), valuePaint)
        yPosition += 30

        // Due Date
        issue.dueDate?.let {
            canvas.drawText("Due Date:", margin.toFloat(), yPosition.toFloat(), labelPaint)
            val dueDateText = PdfHelper.formatDate(it)
            val isOverdue = it < System.currentTimeMillis() && issue.status != com.hajducakmarek.fixit.models.IssueStatus.VERIFIED
            val dueDatePaint = if (isOverdue) {
                Paint().apply {
                    textSize = 14f
                    color = android.graphics.Color.RED
                }
            } else {
                valuePaint
            }
            canvas.drawText(dueDateText, (margin + 150).toFloat(), yPosition.toFloat(), dueDatePaint)
            if (isOverdue) {
                canvas.drawText("(OVERDUE)", (margin + 300).toFloat(), yPosition.toFloat(), dueDatePaint)
            }
            yPosition += 30
        }

        // Description
        yPosition += 10
        canvas.drawText("Description:", margin.toFloat(), yPosition.toFloat(), labelPaint)
        yPosition += 25

        // Wrap description text
        val descriptionLines = wrapText(issue.description, pageWidth - (margin * 2), valuePaint)
        descriptionLines.forEach { line ->
            canvas.drawText(line, margin.toFloat(), yPosition.toFloat(), valuePaint)
            yPosition += 20
        }

        // Photos count
        if (photos.isNotEmpty()) {
            yPosition += 20
            canvas.drawText("Photos: ${photos.size}", margin.toFloat(), yPosition.toFloat(), labelPaint)
            yPosition += 25
        }

        // Comments count
        if (comments.isNotEmpty()) {
            yPosition += 10
            canvas.drawText("Comments: ${comments.size}", margin.toFloat(), yPosition.toFloat(), labelPaint)
            yPosition += 25
        }

        pdfDocument.finishPage(page)

        // Save to file
        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return file.absolutePath
    }

    actual suspend fun exportAllIssuesToPdf(issues: List<Issue>): String {
        val fileName = "all_issues_${System.currentTimeMillis()}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val file = File(downloadsDir, fileName)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var yPosition = margin + 40

        // Title
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        canvas.drawText("All Issues Report", margin.toFloat(), yPosition.toFloat(), titlePaint)
        yPosition += 30

        val labelPaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.GRAY
        }
        canvas.drawText("Total Issues: ${issues.size}", margin.toFloat(), yPosition.toFloat(), labelPaint)
        yPosition += 20
        canvas.drawText("Generated: ${PdfHelper.formatDate(System.currentTimeMillis())}", margin.toFloat(), yPosition.toFloat(), labelPaint)
        yPosition += 40

        // Table header
        val headerPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        canvas.drawText("Flat #", margin.toFloat(), yPosition.toFloat(), headerPaint)
        canvas.drawText("Status", (margin + 80).toFloat(), yPosition.toFloat(), headerPaint)
        canvas.drawText("Priority", (margin + 180).toFloat(), yPosition.toFloat(), headerPaint)
        canvas.drawText("Due Date", (margin + 280).toFloat(), yPosition.toFloat(), headerPaint)
        yPosition += 25

        // Draw line
        canvas.drawLine(
            margin.toFloat(),
            yPosition.toFloat(),
            (pageWidth - margin).toFloat(),
            yPosition.toFloat(),
            Paint().apply { strokeWidth = 2f }
        )
        yPosition += 15

        // Table rows
        val rowPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.BLACK
        }

        issues.take(25).forEach { issue ->  // Limit to 25 issues per page
            if (yPosition > pageHeight - margin) {
                return@forEach // Stop if page is full
            }

            canvas.drawText(issue.flatNumber, margin.toFloat(), yPosition.toFloat(), rowPaint)
            canvas.drawText(issue.status.name, (margin + 80).toFloat(), yPosition.toFloat(), rowPaint)
            canvas.drawText(issue.priority.name, (margin + 180).toFloat(), yPosition.toFloat(), rowPaint)
            val dueDateText = issue.dueDate?.let { PdfHelper.formatDate(it) } ?: "None"
            canvas.drawText(dueDateText, (margin + 280).toFloat(), yPosition.toFloat(), rowPaint)
            yPosition += 20
        }

        pdfDocument.finishPage(page)

        // Save to file
        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return file.absolutePath
    }

    private fun wrapText(text: String, maxWidth: Int, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)

            if (width > maxWidth && currentLine.isNotEmpty()) {
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