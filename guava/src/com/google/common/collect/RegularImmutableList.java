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
import com.google.common.annotations.VisibleForTesting;

import java.util.Spliterator;
import java.util.Spliterators;

/**
 * 由简单数组支持实现了这个类{@link ImmutableList}，并没有进行对外的暴露数据
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial")
class RegularImmutableList<E> extends ImmutableList<E> {
    /**
     * 一个元素都没有哦
     */
    static final ImmutableList<Object> EMPTY = new RegularImmutableList<>(new Object[0]);

    /**
     * 不可变集合中的元素的信息
     */
    @VisibleForTesting
    final transient Object[] array;

    RegularImmutableList(Object[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    /**
     * 是否为子视图
     * @return
     */
    @Override
    boolean isPartialView() {
        return false;
    }

    /**
     * 内部的元素的信息，这里只是对于{@link ImmutableList}暴露！其他的人并没有能够看到这个数据！
     * 这个Class为私有的Class 只能当前包内使用哦！！！
     * @return
     */
    @Override
    Object[] internalArray() {
        return array;
    }

    @Override
    int internalArrayStart() {
        return 0;
    }

    @Override
    int internalArrayEnd() {
        return array.length;
    }

    @Override
    int copyIntoArray(Object[] dst, int dstOff) {
        System.arraycopy(array, 0, dst, dstOff, array.length);
        return dstOff + array.length;
    }

    /**
     * 假投E是安全的，因为创作方法只允许e
     * @param index
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E)array[index];
    }

    @SuppressWarnings("unchecked")
    @Override
    public UnmodifiableListIterator<E> listIterator(int index) {
        //通过共用的方法去处理这个迭代器的数据的信息，就是简单的封装了迭代器的实现哦！
        return (UnmodifiableListIterator<E>)Iterators.forArray(array, 0, array.length, index);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(array, SPLITERATOR_CHARACTERISTICS);
    }

}
