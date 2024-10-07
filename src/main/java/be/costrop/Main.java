package be.costrop;

import be.costrop.excel.ExcelFinder;

public class Main {

    public static void main(String[] args) {
        final var excelsFoundOnDevice = new ExcelFinder().search();

        excelsFoundOnDevice.forEach(excelFileInfo -> {
            System.out.println("Found excel " + excelFileInfo);
        });
    }
}