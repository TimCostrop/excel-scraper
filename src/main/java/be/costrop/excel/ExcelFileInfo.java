package be.costrop.excel;

import java.nio.file.Path;

public record ExcelFileInfo(
        Path fullPath,
        String author
) {
}
