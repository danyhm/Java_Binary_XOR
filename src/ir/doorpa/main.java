package ir.doorpa;

import ir.doorpa.gui.MainForm;

import java.io.File;

/**
 * Starting point of program execution
 * */
//@todo add endianness ?
public class main {
    static private File binaryFile;

    static public File getBinaryFile() {
        return binaryFile;
    }

    static public void setBinaryFile(File binaryFile) {
        main.binaryFile = binaryFile;
    }

    public static void main(String[] args) {
        new MainForm();
    }
}
