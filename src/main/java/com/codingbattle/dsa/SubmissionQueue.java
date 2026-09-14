package com.codingbattle.dsa;

import com.codingbattle.model.Submission;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Thread-safe FIFO Submission Processing Queue.
 * Demonstrates:
 * - FIFO Queue processing
 * - Thread synchronization
 * - Decoupling UI requests from heavy compilation/execution tasks
 */
public class SubmissionQueue {
    private final Queue<SubmissionTask> queue;
    private final Object lock = new Object();

    public static class SubmissionTask {
        private final Submission submission;
        private final Runnable task;

        public SubmissionTask(Submission submission, Runnable task) {
            this.submission = submission;
            this.task = task;
        }

        public Submission getSubmission() {
            return submission;
        }

        public void execute() {
            if (task != null) {
                task.run();
            }
        }
    }

    public SubmissionQueue() {
        this.queue = new LinkedList<>();
    }

    public void enqueue(Submission submission, Runnable task) {
        synchronized (lock) {
            queue.offer(new SubmissionTask(submission, task));
        }
    }

    public SubmissionTask dequeue() {
        synchronized (lock) {
            return queue.poll();
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return queue.isEmpty();
        }
    }

    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }
}
