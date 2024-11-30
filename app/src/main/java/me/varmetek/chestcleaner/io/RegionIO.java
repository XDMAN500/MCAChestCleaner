package me.varmetek.chestcleaner.io;

import me.varmetek.chestcleaner.model.Region;

import java.io.*;

public class RegionIO {

    public void save(OutputStream stream) {

    }

    public Region load(RandomAccessFile stream) {
        int[] chunkOffsets = new int[1024];
        int[] timestamp = new int[1024];
        int[] chunkSectionCount = new int[1024];
        Region region = new Region();
        BufferedInputStream buffered;

        try {
            //Read chunk offsets
            for (int i = 0; i < 1024; i += 1) {
                int chunkLoc = stream.readInt();
                int offset = (chunkLoc >> 8) & 0xFFFFFF;
                int sectionCount = chunkLoc & 0xFF;

                chunkOffsets[i] = offset;
                chunkSectionCount[i] = sectionCount;
                System.out.println("Chunk Found %d %08X %08X Coff: %d,%08X SecOff: %d,%08X".formatted(i, i * 4, chunkLoc, offset, offset, sectionCount, sectionCount));

            }

            //Read chunk timestamps
            for (int i = 0; i < 1024; i += 1) {

            }
            } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return region;


    }
}
