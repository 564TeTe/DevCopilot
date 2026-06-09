package com.devcopilot.infrastructure.storage;

import com.devcopilot.application.port.TextExtractor;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

@Component
public class TikaTextExtractor implements TextExtractor {

    @Override
    public String extract(Path file) {
        try (InputStream inputStream = Files.newInputStream(file)) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, new Metadata(), new ParseContext());
            String text = handler.toString();
            if (text == null || text.isBlank()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "文档没有可解析文本");
            }
            return text;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文档解析失败: " + ex.getMessage());
        }
    }
}
