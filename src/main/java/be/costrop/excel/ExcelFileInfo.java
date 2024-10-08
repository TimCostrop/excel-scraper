package be.costrop.excel;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Optional;

public record ExcelFileInfo(
        Path fullPath,
        String author
) {

    public String name() {
        return fullPath.getFileName().toString();
    }

    public String author() {
        return Optional.ofNullable(author)
                .filter(StringUtils::isNotBlank)
                .orElse("No author found");
    }

    public String id() {
        return fullPath.toString().replaceAll("[/\\\\]", "_");
    }
}
