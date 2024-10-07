package be.costrop.excel;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public class ExcelFinder {
    public List<ExcelFileInfo> search() {
        return Arrays.stream(File.listRoots())
                .map(File::toPath)
                .flatMap(this::walkThroughPath)
                .filter(Files::isRegularFile)
                .map(this::extractExcelExtension)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToExcelFileInfo)
                .peek(System.out::println)
                .toList();
    }

    private record PathWithExcelExtension(Path path, ExcelVersion excelVersion) {
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
            System.err.println("Error while walking file tree for path " + e.getMessage());
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
            return optionalAuthor.orElseThrow(NoSuchElementException::new);
        } catch (Exception e) {
            System.err.printf("Could not find author for %s%n", path.toString());
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
}
