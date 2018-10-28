/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkPositionIndex;

/**
 * {@link ListIterator}的一些实现！
 * <p>
 * This class provides a skeletal implementation of the  interface across a fixed number of elements that may be
 * retrieved by position. It does not support {@link #remove}, {@link #set}, or {@link #add}.
 *
 * @author Jared Levy
 */
@GwtCompatible
abstract class AbstractIndexedListIterator<E> extends UnmodifiableListIterator<E> {
    /**
     * 大小
     */
    private final int size;

    /**
     * 位置信息
     */
    private int position;

    /**
     * 获取到数据中的指定的元素信息，由子类去实现这个操作
     * Returns the element with the specified index. This method is called by {@link #next()}.
     */
    protected abstract E get(int index);

    /**
     *
     * @throws IllegalArgumentException if {@code size} is negative
     */
    protected AbstractIndexedListIterator(int size) {
        this(size, 0);
    }

    /**
     * 下标数据检测
     * @throws IndexOutOfBoundsException if {@code position} is negative or is greater than {@code
     *     size}
     * @throws IllegalArgumentException if {@code size} is negative
     */
    protected AbstractIndexedListIterator(int size, int position) {
        checkPositionIndex(position, size);
        this.size = size;
        this.position = position;
    }

    /**
     * 是否还有下一个数据的信息
     * @return
     */
    @Override
    public final boolean hasNext() {
        return position < size;
    }

    /**
     * 是否存在下一个数据哦~~~~
     * @return
     */
    @Override
    public final E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return get(position++);
    }

    @Override
    public final int nextIndex() {
        return position;
    }

    @Override
    public final boolean hasPrevious() {
        return position > 0;
    }

    @Override
    public final E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return get(--position);
    }

    @Override
    public final int previousIndex() {
        return position - 1;
    }
}
