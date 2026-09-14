package com.codingbattle.dsa;

import java.util.EmptyStackException;

/**
 * Custom Generic LIFO Stack data structure used for Code Editor Undo and Redo operations.
 * Demonstrates:
 * - Stack dynamic array resizing
 * - push O(1) amortized
 * - pop O(1)
 * - peek O(1)
 */
public class EditorUndoStack<T> {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_CAPACITY = 32;

    public EditorUndoStack() {
        this.elements = new Object[DEFAULT_CAPACITY];
    }

    public void push(T item) {
        ensureCapacity();
        elements[size++] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = (T) elements[--size];
        elements[size] = null; // Prevent memory leak
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return (T) elements[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    private void ensureCapacity() {
        if (size >= elements.length) {
            int newCap = elements.length * 2;
            Object[] newArr = new Object[newCap];
            System.arraycopy(elements, 0, newArr, 0, size);
            elements = newArr;
        }
    }
}
