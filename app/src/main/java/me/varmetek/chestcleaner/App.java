package me.varmetek.chestcleaner;

import net.querz.mca.Chunk;
import net.querz.mca.LoadFlags;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) throws IOException {
        File read = args.length == 1 ? new File(args[0]): new File("data.bin");
        ChestStats stats = new ChestStats();

        if (read.isDirectory()) {
            clearChestFromRegionFolder(read, stats, false, 0);
        } else {
            clearChestFromRegionFile(read, stats, false, 0);
        }
    }

    private static void clearChestFromMCA(MCAFile mcaFile, ChestStats stats, boolean dryRun, int debugLevel) {
        for (int i = 0; i < 1024; i += 1) {
            Chunk chunk = mcaFile.getChunk(i);
            if (chunk == null)
                continue;

            ListTag<CompoundTag> tileEntities = (ListTag<CompoundTag>)chunk.getHandle().getListTag("block_entities");

            for (CompoundTag tag : tileEntities) {
                if (tag.containsKey("id") && tag.containsKey("Items")) {
                    stats.containerCount += 1;
                    String id = tag.getString("id");
                    int x = tag.getInt("x");
                    int y = tag.getInt("y");
                    int z = tag.getInt("z");
                    Tag itemTag = tag.get("Items");
                    int itemCount = itemTag.getID() == 9 ? ((ListTag<CompoundTag>)itemTag).size() : ((CompoundTag)itemTag).size();

                    if (debugLevel == 0 && itemCount == 0) {
                        continue;
                    }
                    stats.clearCount += 1;
                    System.out.println("Found container %s at %d, %d %d with %d items".formatted(id, x, y, z, itemCount));
                    if (itemTag.getID() == 9) {
                        tag.put("Items", ListTag.createUnchecked(CompoundTag.class));
                    } else {
                        tag.put("Items", new CompoundTag());
                    }

                }
            }
        }
    }

    private static void clearChestFromRegionFolder(File file, ChestStats stats, boolean dryRun, int debugLevel) throws IOException {
        File[] files = file.listFiles((dir, name) -> name.endsWith(".mca"));
        List<File> errorFiles = new ArrayList<File>();
        for (File regionFile : files) {

            try {
                System.out.println("Looking at " + regionFile.getName());
                MCAFile mcaFile = MCAUtil.read(regionFile, LoadFlags.RAW);
                clearChestFromMCA(mcaFile, stats, dryRun, debugLevel);
                MCAUtil.write(mcaFile, regionFile);
            } catch (Exception e) {
                errorFiles.add(regionFile);
            }
        }

        printChestStats(stats);

        if (!errorFiles.isEmpty()) {
            System.out.println("");
            System.out.println("%d files could not be processed".formatted(errorFiles.size()));
            for (File errorFile : errorFiles) {
                System.out.println("-- " + errorFile.getName());
            }
        }
    }

    private static void clearChestFromRegionFile(File regionFile, ChestStats stats, boolean dryRun, int debugLevel) throws IOException {
        System.out.println("Looking at " + regionFile.getName());
        MCAFile mcaFile = MCAUtil.read(regionFile, LoadFlags.RAW);
        clearChestFromMCA(mcaFile, stats, dryRun, debugLevel);
        MCAUtil.write(mcaFile, regionFile);

        printChestStats(stats);
    }

    private static void printChestStats(ChestStats stats) {
        System.out.println("Cleared %d non-empty containers".formatted(stats.clearCount));
        System.out.println("Found %d total containers".formatted(stats.containerCount));
    }

    private static class ChestStats {
        int clearCount;
        int containerCount;
    }
}
