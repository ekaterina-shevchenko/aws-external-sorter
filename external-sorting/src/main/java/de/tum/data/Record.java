package de.tum.data;

import lombok.Data;

import java.util.Arrays;

@Data
public class Record {
    public static final int recordSize = 100;
    protected byte[] key;
    protected byte[] value;

    public Record(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public byte[] getByteArray() {
        byte[] joinedArray = Arrays.copyOf(key, key.length + value.length);
        System.arraycopy(value, 0, joinedArray, key.length, value.length);
        return joinedArray;
    }
}
