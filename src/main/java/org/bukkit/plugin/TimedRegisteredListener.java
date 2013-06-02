package org.bukkit.plugin;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Extends RegisteredListener to include timing information
 */
public class TimedRegisteredListener extends RegisteredListener {
    private int count;
    private long totalTime;
    // Spigot start
    public long curTickTotal = 0;
    public long violations = 0;
    // Spigot end
    private Event event;
    private boolean multiple = false;

    public TimedRegisteredListener(final Listener pluginListener, final EventExecutor eventExecutor, final EventPriority eventPriority, final Plugin registeredPlugin, final boolean listenCancelled) {
        super(pluginListener, eventExecutor, eventPriority, registeredPlugin, listenCancelled);
    }

    @Override
    public void callEvent(Event event) throws EventException {
        // Spigot start
        if ( !org.bukkit.Bukkit.getServer().getPluginManager().useTimings() )
        {
            super.callEvent( event );
            return;
        }
        // Spigot end
        if (event.isAsynchronous()) {
            super.callEvent(event);
            return;
        }
        count++;
        if (this.event == null) {
            this.event = event;
        }
        else if (!this.event.getClass().equals(event.getClass())) {
            multiple = true;
        }
        long start = System.nanoTime();
        super.callEvent(event);
        // Spigot start
        long diff = System.nanoTime() - start;
        curTickTotal += diff;
        totalTime += diff;
        // Spigot end
    }

    /**
     * Resets the call count and total time for this listener
     */
    public void reset() {
        count = 0;
        totalTime = 0;
        // Spigot start
        curTickTotal = 0;
        violations = 0;
        // Spigot end
    }

    /**
     * Gets the total times this listener has been called
     *
     * @return Times this listener has been called
     */
    public int getCount() {
        return count;
    }

    /**
     * Gets the total time calls to this listener have taken
     *
     * @return Total time for all calls of this listener
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * Gets the first event this listener handled
     *
     * @return An event handled by this RegisteredListener
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Gets whether this listener has handled multiple events
     *
     * @return True if this listener has handled multiple events
     */
    public boolean hasMultiple() {
        return multiple;
    }
}
