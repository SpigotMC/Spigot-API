package org.bukkit;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Provides custom timing sections for /timings merged
 */
public class CustomTimingsHandler {

    final public String name;
    public long count = 0;
    public long start = 0;
    public long timingDepth = 0;
    public long totalTime = 0;
    public long curTickTotal = 0;
    public long violations = 0;
    CustomTimingsHandler parent = null;

    final public static ConcurrentLinkedQueue<CustomTimingsHandler> allList = new ConcurrentLinkedQueue<CustomTimingsHandler>();

    public CustomTimingsHandler(String name) {
        this.name = name;
        allList.add(this);
    }
    public CustomTimingsHandler(String name, CustomTimingsHandler parent) {
        this(name);
        this.parent = parent;
    }

    /**
     * Prints the timings and extra data to the printstream
     * @param printStream
     */
    public static void printTimings(PrintStream printStream) {
        printStream.println("Minecraft");
        for (CustomTimingsHandler timings : allList) {
            long time = timings.totalTime;
            long count = timings.count;
            if (count == 0) continue;
            long avg = time / count;

            printStream.println("    " + timings.name + " Time: " + time + " Count: " + count + " Avg: " + avg + " Violations: " + timings.violations);
        }
        printStream.println("# Version " + Bukkit.getVersion());
        int entities = 0;
        int livingEntities = 0;
        for (World world : Bukkit.getWorlds()) {
            entities += world.getEntities().size();
            livingEntities += world.getLivingEntities().size();
        }
        printStream.println("# Entities " + entities);
        printStream.println("# LivingEntities " + livingEntities);
    }

    /**
     * Resets all timings
     */
    public static void reload() {
        if (!Bukkit.getServer().getPluginManager().useTimings()) return;
        for (CustomTimingsHandler timings : allList) {
            timings.reset();
        }
    }

    /**
     * Ticked every tick by CraftBukkit to count the number of times a timer caused TPS loss.
     */
    public static void tick() {
        if (!Bukkit.getServer().getPluginManager().useTimings()) return;
        for (CustomTimingsHandler timings : allList) {
            if (timings.curTickTotal > 50000000) {
                timings.violations += Math.ceil(timings.curTickTotal / 50000000);
            }
            timings.curTickTotal = 0;
        }

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            for (RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
                if (listener instanceof TimedRegisteredListener) {
                    TimedRegisteredListener timings = (TimedRegisteredListener) listener;
                    if (timings.curTickTotal > 50000000) {
                        timings.violations += Math.ceil(timings.curTickTotal / 50000000);
                    }
                    timings.curTickTotal = 0;
                }
            }
        }
    }

    /**
     * Starts timing to track a section of code.
     */
    public void startTiming() {
        if (!Bukkit.getServer().getPluginManager().useTimings()) return;

        if (++timingDepth != 1) {
            return; // Already timing.
        }
        start = System.nanoTime();

        if (parent != null && ++parent.timingDepth == 1) {
            parent.start = start;
        }
    }

    public void stopTiming() {
        if (!Bukkit.getServer().getPluginManager().useTimings()) return;
        if (--timingDepth != 0 || start == 0) {
            return;
        }
        long diff = System.nanoTime() - start;
        totalTime += diff;
        curTickTotal += diff;
        count++;
        start = 0;
        if (parent != null) {
            parent.stopTiming();
        }
    }

    public void reset() {
        count = 0;
        violations = 0;
        curTickTotal = 0;
        totalTime = 0;
    }
}

