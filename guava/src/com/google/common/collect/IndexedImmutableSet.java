/*
 * Copyright (C) 2018 The Guava Authors
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
import com.google.common.annotations.GwtIncompatible;

import java.util.Spliterator;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * indexed （索引）标识可以遍历的处理？
 *
 * @param <E>
 */
@GwtCompatible(emulated = true)
abstract class IndexedImmutableSet<E> extends ImmutableSet<E> {
    /**
     * forEach遍历的时候，通过index 下标进行遍历set 集合数组中的元素的信息，这里主要是通过遍历数组（List）中元素 详情可以参考 ImmutableSet内部的实现！处理方式比较的妥当
     *
     * @param index
     * @return
     */
    abstract E get(int index);

    @Override
    public UnmodifiableIterator<E> iterator() {
        //根据获取的数组信息进行获取迭代器进行遍历
        return asList().iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return CollectSpliterators.indexed(size(), SPLITERATOR_CHARACTERISTICS, this::get);
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        checkNotNull(consumer);
        int n = size();
        for (int i = 0; i < n; i++) {
            //这里是根据数组中的下标来遍历Set集合中的，可能有自己的考虑，牺牲空间换取时间
            consumer.accept(get(i));
        }
    }

    @Override
    @GwtIncompatible
    int copyIntoArray(Object[] dst, int offset) {
        //复制数据的信息
        return asList().copyIntoArray(dst, offset);
    }

    @Override
    ImmutableList<E> createAsList() {
        /**
         * 获取List集合中的数据信息，简单的作为一个代理
         */
        return new ImmutableAsList<E>() {
            @Override
            public E get(int index) {
                return IndexedImmutableSet.this.get(index);
            }

            @Override
            boolean isPartialView() {
                return IndexedImmutableSet.this.isPartialView();
            }

            @Override
            public int size() {
                return IndexedImmutableSet.this.size();
            }

            @Override
            ImmutableCollection<E> delegateCollection() {
                return IndexedImmutableSet.this;
            }
        };
    }
}
