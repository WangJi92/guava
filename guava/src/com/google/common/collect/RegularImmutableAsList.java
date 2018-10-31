/*
 * Copyright (C) 2012 The Guava Authors
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

import java.util.function.Consumer;

/**
 * {@link ImmutableAsList}实现，专门用于委托集合已由{@code ImmutableList}或数组支持
 * <p>
 * Regular  如何理解这个单词 adj. 有规律的;规则，整齐的;不变的;合格的 n.正规军;主力（或正式）队员;常客 adv. 定期地;经常地
 *
 * @author Louis Wasserman
 */
@GwtCompatible(emulated = true)
@SuppressWarnings("serial")
class RegularImmutableAsList<E> extends ImmutableAsList<E> {
    /**
     * 这个是代理的集合 比如Set 里面的HashTable的数据可能不方便变量查看！
     */
    private final ImmutableCollection<E> delegate;

    /**
     * 这个就是保存了当前集合中的数据的信息！方便遍历和查看List中的数据信息
     */
    private final ImmutableList<? extends E> delegateList;

    RegularImmutableAsList(ImmutableCollection<E> delegate, ImmutableList<? extends E> delegateList) {
        this.delegate = delegate;
        this.delegateList = delegateList;
    }

    RegularImmutableAsList(ImmutableCollection<E> delegate, Object[] array) {
        this(delegate, ImmutableList.<E>asImmutableList(array));
    }

    @Override
    ImmutableCollection<E> delegateCollection() {
        return delegate;
    }

    ImmutableList<? extends E> delegateList() {
        return delegateList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UnmodifiableListIterator<E> listIterator(int index) {
        return (UnmodifiableListIterator<E>)delegateList.listIterator(index);
    }

    @GwtIncompatible
    @Override
    public void forEach(Consumer<? super E> action) {
        delegateList.forEach(action);
    }

    @GwtIncompatible
    @Override
    int copyIntoArray(Object[] dst, int offset) {
        return delegateList.copyIntoArray(dst, offset);
    }

    @Override
    Object[] internalArray() {
        return delegateList.internalArray();
    }

    @Override
    int internalArrayStart() {
        return delegateList.internalArrayStart();
    }

    @Override
    int internalArrayEnd() {
        return delegateList.internalArrayEnd();
    }

    @Override
    public E get(int index) {
        return delegateList.get(index);
    }
}
