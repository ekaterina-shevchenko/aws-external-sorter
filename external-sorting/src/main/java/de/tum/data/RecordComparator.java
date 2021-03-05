package de.tum.data;

import java.util.Comparator;

public class RecordComparator implements Comparator<Record> {
    @Override
    public int compare(Record o1, Record o2) {
        byte[] key1 = o1.getKey();
        byte[] key2 = o2.getKey();
        for(int i = 0; i < 10; i++ ) {
            if (key1[i] > key2[i]) {
                return 1;
            }
            if (key1[i] < key2[i]) {
                return -1;
            }
        }
        return 0;
    }
}
