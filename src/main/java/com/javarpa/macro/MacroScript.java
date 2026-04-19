package com.javarpa.macro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a macro script containing a sequence of MacroActions.
 * Can be saved and loaded as JSON (.macro file).
 */
public class MacroScript {

    private String name;
    private String description;
    private String createdAt;
    private List<MacroAction> actions;
    private int repeatCount = 1;
    private long speedMultiplierPercent = 100; // 100% = normal speed

    public MacroScript() {
        this.actions = new ArrayList<>();
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.name = "Untitled Macro";
        this.description = "";
    }

    public MacroScript(String name) {
        this();
        this.name = name;
    }

    /** Add an action to the script. */
    public void addAction(MacroAction action) {
        actions.add(action);
    }

    /** Clear all actions. */
    public void clear() {
        actions.clear();
    }

    /**
     * Save this macro script to a .macro JSON file.
     */
    public void saveToFile(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(this, writer);
        }
    }

    /**
     * Load a MacroScript from a .macro JSON file.
     */
    public static MacroScript loadFromFile(File file) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, MacroScript.class);
        }
    }

    // === Getters / Setters ===
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedAt() { return createdAt; }
    public List<MacroAction> getActions() { return actions; }
    public void setActions(List<MacroAction> actions) { this.actions = actions; }
    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }
    public long getSpeedMultiplierPercent() { return speedMultiplierPercent; }
    public void setSpeedMultiplierPercent(long speedMultiplierPercent) { this.speedMultiplierPercent = speedMultiplierPercent; }

    public int getActionCount() { return actions != null ? actions.size() : 0; }
}
