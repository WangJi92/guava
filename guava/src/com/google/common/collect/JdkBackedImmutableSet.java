/*
 * Copyright (C) 2018 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

/**
 * 由JDK HashSet支持的ImmutableSet实现，用于防御明显的散列泛滥。此实现从未在GWT客户端使用，但必须存在才能使序列化工作。
 *
 * @author Louis Wasserman
 */
@GwtCompatible(serializable = true)
final class JdkBackedImmutableSet<E> extends IndexedImmutableSet<E> {
    /**
     * 代理JDK中的Set集合，这个方便通过Hash值查找具体的元素
     */
    private final Set<?> delegate;
    /**
     * 保存Set集合中的数据List的信息！方便进行获取List进行迭代器的便利处理！ 以及获取Set集合的数据的大小哦！这样操作起来非常的方便！
     * <p>
     * 这个方便获取数据的大小 and 进行forEach遍历，迭代器遍历的处理
     */
    private final ImmutableList<E> delegateList;

    @Override
    E get(int index) {
        //这里就是迭代器遍历的时候，方便进行这里的处理
        return delegateList.get(index);
    }

    JdkBackedImmutableSet(Set<?> delegate, ImmutableList<E> delegateList) {
        this.delegate = delegate;
        this.delegateList = delegateList;
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    public int size() {
        return delegateList.size();
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return delegate.contains(object);
    }
}
