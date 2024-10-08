package be.costrop.excel;

import be.costrop.controller.dto.DeleteFileRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class ExcelFinderImpl implements ExcelFinder {

    private static List<ExcelFileInfo> foundFiles;

    public List<ExcelFileInfo> getSearchResults() {
        if (foundFiles == null)
            foundFiles = performSearch();
        return foundFiles;
    }

    @Override
    public void deleteFiles(List<DeleteFileRequest> deleteFileRequests) {
        log.info("I want to delete files {}", deleteFileRequests);

        // clear the cached files.
        foundFiles = performSearch();
    }

    private List<ExcelFileInfo> performSearch() {
        return Arrays.stream(File.listRoots())
                .map(File::toPath)
                .flatMap(this::walkThroughPath)
                .filter(Files::isRegularFile)
                .map(this::extractExcelExtension)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToExcelFileInfo)
                .peek(e -> log.info("Found file {}", e))
                .toList();
    }

    private Optional<PathWithExcelExtension> extractExcelExtension(Path path) {
        final var extension = FilenameUtils.getExtension(path.toString());
        if (extension.endsWith("xlsx"))
            return Optional.of(new PathWithExcelExtension(path, ExcelVersion.XLSX));
        if (extension.endsWith("xls"))
            return Optional.of(new PathWithExcelExtension(path, ExcelVersion.XLS));

        return Optional.empty();
    }

    private Stream<Path> walkThroughPath(Path path) {
        try {
            final var filesAggregator = new FilesAggregator();
            Files.walkFileTree(path, filesAggregator);
            return filesAggregator.getFiles().stream();
        } catch (IOException e) {
            // handle exception
            log.error("Error while walking file tree for path {}", e.getMessage());
            return Stream.empty();
        }
    }

    private ExcelFileInfo convertToExcelFileInfo(PathWithExcelExtension path) {
        return new ExcelFileInfo(path.path(), extractAuthor(path));
    }

    private String extractAuthor(PathWithExcelExtension path) {
        try {
            final var optionalAuthor = switch (path.excelVersion()) {
                case XLSX -> extractAuthorViaXssf(path.path());
                case XLS -> extractAuthorViaHssf(path.path());
            };
            return optionalAuthor.orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    private Optional<String> extractAuthorViaXssf(Path path) throws Exception {
        try (final OPCPackage pckg = OPCPackage.open(path.toFile())) {
            final PackageProperties props = pckg.getPackageProperties();
            return props.getCreatorProperty();
        }
    }

    private Optional<String> extractAuthorViaHssf(Path path) throws Exception {
        try (final POIFSFileSystem fs = new POIFSFileSystem(path.toFile())) {
            PropertySet props = PropertySetFactory.create(fs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
            if (props instanceof final SummaryInformation sumInf) {
                return Optional.of(sumInf.getAuthor());
            }
        }
        return Optional.empty();
    }

    private record PathWithExcelExtension(Path path, ExcelVersion excelVersion) {
    }
}
