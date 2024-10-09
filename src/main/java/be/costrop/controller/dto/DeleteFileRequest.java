package be.costrop.controller.dto;

public record DeleteFileRequest(
        String path,
        Boolean delete
) {

}
