/*
 * Copyright (C) 2007 The Guava Authors
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
import com.google.common.annotations.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Spliterator;
import java.util.Spliterators;

/**
 * 有两个或者多个的不可变集合信息 Implementation of {@link ImmutableSet} with two or more elements.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial")
final class RegularImmutableSet<E> extends ImmutableSet<E> {
    /**
     * 空元素信息
     */
    static final RegularImmutableSet<Object> EMPTY =
        new RegularImmutableSet<>(new Object[0], 0, null, 0);

    /**
     * 保存数据的数组的信息 elements 保存的元素按照添加的顺序插入，且长度大小等于时间的元素的大小
     */
    private final transient Object[] elements;

    /**
     * 散列位置中的相同元素（加上空值） the same elements in hashed positions (plus nulls) table表中的元素是无序的！且大小不一定等于实际元素的大小？？？？
     */
    @VisibleForTesting
    final transient Object[] table;

    /**
     * 'and''和'用int来获取有效的表索引  hashcode & mask = index 'and' with an int to get a valid table index.
     */
    private final transient int mask;

    /**
     * 所有的元素的 hashCode码之和
     */
    private final transient int hashCode;

    /**
     * @param elements 所有的元素（备份的信息） TODO 这里这样使用的意义很在
     * @param hashCode table表中所有的元素的Hash值之和
     * @param table    Hash表中的元素
     * @param mask     hash表的长度减去1
     */
    RegularImmutableSet(Object[] elements, int hashCode, Object[] table, int mask) {
        this.elements = elements;
        this.table = table;
        this.mask = mask;
        this.hashCode = hashCode;
    }

    @Override
    public boolean contains(@Nullable Object target) {
        Object[] table = this.table;
        if (target == null || table == null) {
            return false;
        }
        //根据Hash值去寻找具体的数据信息！可能存在冲突需要继续向下寻找数据
        for (int i = Hashing.smearedHash(target); ; i++) {
            i &= mask;
            Object candidate = table[i];
            if (candidate == null) {
                return false;
            } else if (candidate.equals(target)) {
                return true;
            }
        }
    }

    @Override
    public int size() {
        //当前的元素的大小哦！
        return elements.length;
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return (UnmodifiableIterator<E>)Iterators.forArray(elements);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(elements, SPLITERATOR_CHARACTERISTICS);
    }

    @Override
    Object[] internalArray() {
        return elements;
    }

    @Override
    int internalArrayStart() {
        return 0;
    }

    @Override
    int internalArrayEnd() {
        return elements.length;
    }

    @Override
    int copyIntoArray(Object[] dst, int offset) {
        //内部调用进行复制数据信息
        System.arraycopy(elements, 0, dst, offset, elements.length);
        return offset + elements.length;
    }

    @Override
    ImmutableList<E> createAsList() {
        return (table == null) ? ImmutableList.<E>of() : new RegularImmutableAsList<E>(this, elements);
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    boolean isHashCodeFast() {
        //是否根据Hash Code能够快速的访问呢
        return true;
    }
}
