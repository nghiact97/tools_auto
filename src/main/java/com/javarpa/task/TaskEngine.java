package com.javarpa.task;

import java.util.function.Consumer;

/**
 * Engine that executes a TaskConfig, supporting start/stop/status callbacks.
 */
public class TaskEngine {

    public enum Status { IDLE, RUNNING, PAUSED, STOPPED, FINISHED, ERROR }

    private volatile Status status = Status.IDLE;
    private volatile boolean paused = false;
    private Thread runThread;
    private int currentLoop = 0;
    private int currentStep = 0;
    private long startTime;

    // Callbacks
    private Runnable onStart;
    private Runnable onFinish;
    private Consumer<Exception> onError;
    private Consumer<Status> onStatusChange;
    private Runnable onStepComplete;

    /**
     * Run a TaskConfig asynchronously.
     */
    public void run(TaskConfig config) {
        if (status == Status.RUNNING) return;

        status = Status.RUNNING;
        currentLoop = 0;
        currentStep = 0;
        startTime = System.currentTimeMillis();
        notifyStatus();

        if (onStart != null) onStart.run();

        runThread = new Thread(() -> {
            try {
                for (int i = 0; i < config.getRepeat(); i++) {
                    if (status == Status.STOPPED) break;
                    currentLoop = i + 1;

                    for (TaskConfig.TaskStep step : config.getSteps()) {
                        if (status == Status.STOPPED) break;

                        // Wait while paused
                        while (paused && status != Status.STOPPED) {
                            Thread.sleep(100);
                        }

                        step.execute();
                        currentStep++;
                        if (onStepComplete != null) onStepComplete.run();
                        Thread.sleep(config.getDelayBetweenSteps());
                    }

                    if (i < config.getRepeat() - 1) {
                        Thread.sleep(config.getDelayBetweenLoops());
                    }
                }

                if (status != Status.STOPPED) {
                    status = Status.FINISHED;
                    notifyStatus();
                    if (onFinish != null) onFinish.run();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status = Status.STOPPED;
                notifyStatus();
            } catch (Exception e) {
                status = Status.ERROR;
                notifyStatus();
                if (onError != null) onError.accept(e);
            }
        });
        runThread.setDaemon(true);
        runThread.start();
    }

    /** Stop execution. */
    public void stop() {
        status = Status.STOPPED;
        paused = false;
        if (runThread != null) runThread.interrupt();
        notifyStatus();
    }

    /** Pause/resume execution. */
    public void togglePause() {
        if (status == Status.RUNNING) {
            paused = !paused;
            status = paused ? Status.PAUSED : Status.RUNNING;
            notifyStatus();
        }
    }

    // Getters
    public Status getStatus() { return status; }
    public int getCurrentLoop() { return currentLoop; }
    public int getCurrentStep() { return currentStep; }
    public long getElapsedMs() { return status == Status.IDLE ? 0 : System.currentTimeMillis() - startTime; }

    // Callbacks
    public TaskEngine onStart(Runnable r) { this.onStart = r; return this; }
    public TaskEngine onFinish(Runnable r) { this.onFinish = r; return this; }
    public TaskEngine onError(Consumer<Exception> c) { this.onError = c; return this; }
    public TaskEngine onStatusChange(Consumer<Status> c) { this.onStatusChange = c; return this; }
    public TaskEngine onStepComplete(Runnable r) { this.onStepComplete = r; return this; }

    private void notifyStatus() {
        if (onStatusChange != null) onStatusChange.accept(status);
    }
}
