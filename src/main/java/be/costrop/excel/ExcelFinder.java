package be.costrop.excel;

import be.costrop.controller.dto.DeleteFileRequest;

import java.util.List;

public interface ExcelFinder {
    List<ExcelFileInfo> getSearchResults();

    void deleteFiles(List<DeleteFileRequest> deleteFileRequests);
}
