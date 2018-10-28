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
import com.google.common.annotations.GwtIncompatible;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * 感觉这个类什么都没干！这个不是public的只能当前包使用哦
 * {@link ImmutableCollection#asList}中返回的数据的值，委托检测这个后备的集合的数据的信息
 *
 * @author Jared Levy
 * @author Louis Wasserman
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial")
abstract class ImmutableAsList<E> extends ImmutableList<E> {
    /**
     * 需要进行代理处理的不可变集合的数据的信息
     *
     * @return
     */
    abstract ImmutableCollection<E> delegateCollection();

    @Override
    public boolean contains(Object target) {
        return delegateCollection().contains(target);
    }

    @Override
    public int size() {
        return delegateCollection().size();
    }

    @Override
    public boolean isEmpty() {
        return delegateCollection().isEmpty();
    }

    @Override
    boolean isPartialView() {
        return delegateCollection().isPartialView();
    }

    /**
     * 序列化形式，导致与原始列表相同的性能
     */
    @GwtIncompatible
    static class SerializedForm implements Serializable {
        /**
         * 元素的引用
         */
        final ImmutableCollection<?> collection;

        SerializedForm(ImmutableCollection<?> collection) {
            this.collection = collection;
        }

        Object readResolve() {
            return collection.asList();
        }

        private static final long serialVersionUID = 0;
    }

    @GwtIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @GwtIncompatible
    @Override
    Object writeReplace() {
        return new SerializedForm(delegateCollection());
    }
}
