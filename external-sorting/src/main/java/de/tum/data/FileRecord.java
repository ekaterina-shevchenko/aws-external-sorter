package de.tum.data;

import lombok.Getter;

import java.io.FileInputStream;

@Getter
public class FileRecord extends Record {
    private FileInputStream fileInputStream;

    public FileRecord(byte[] key, byte[] value, FileInputStream input) {
        super(key, value);
        this.fileInputStream = input;
    }
}
