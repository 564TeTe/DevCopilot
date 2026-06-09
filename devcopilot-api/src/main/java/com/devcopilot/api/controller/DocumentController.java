package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.dto.DocumentUploadResult;
import com.devcopilot.application.service.DocumentService;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.model.KnowledgeDocument;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController extends CurrentUserSupport {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DocumentUploadResult> upload(@RequestParam Long knowledgeBaseId, @RequestParam MultipartFile file) {
        try {
            return ApiResponse.ok(documentService.upload(
                    currentUserId(),
                    knowledgeBaseId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            ));
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取上传文件失败");
        }
    }

    @GetMapping
    public ApiResponse<List<KnowledgeDocument>> list(@RequestParam Long knowledgeBaseId) {
        return ApiResponse.ok(documentService.listByKnowledgeBase(currentUserId(), knowledgeBaseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDocument> get(@PathVariable Long id) {
        return ApiResponse.ok(documentService.getOwned(currentUserId(), id));
    }
}
