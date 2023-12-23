package com.aphisiit.com.signingpdf.controller

import com.aphisiit.com.signingpdf.service.PdfService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("pdf")
class PdfController {
    
    @Autowired
    lateinit var pdfService: PdfService
    
    @PostMapping("sign")
    fun sign(@RequestBody file: MultipartFile, httpServletRequest: HttpServletRequest) : ResponseEntity<Resource> {
        val brs = ByteArrayResource(pdfService.signPdf(file.inputStream))
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(brs.contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("sign-${file.originalFilename}").build().toString())
            .body(brs)
    }
    
    @PostMapping("unsign")
    fun unsign(@RequestBody file: MultipartFile, httpServletRequest: HttpServletRequest) : ResponseEntity<Resource> {
        val brs = ByteArrayResource(pdfService.unsignPdf(file.inputStream))
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(brs.contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("unsign-${file.originalFilename}").build().toString())
            .body(brs)
    }
    
}