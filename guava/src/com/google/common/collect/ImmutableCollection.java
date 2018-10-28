/*
 * Copyright (C) 2008 The Guava Authors
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
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 不可变集合！
 * class Foo {
 *   private static final ImmutableSet<String> RESERVED_CODES =
 *       ImmutableSet.of("AZ", "CQ", "ZX");
 *
 *   private final ImmutableSet<String> codes;
 *
 *   public Foo(Iterable<String> codes) {
 *     this.codes = ImmutableSet.copyOf(codes);
 *     checkArgument(Collections.disjoint(this.codes, RESERVED_CODES));
 *   }
 * }
 * }</pre>
 *
 * <h3>See also</h3>
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/ImmutableCollectionsExplained"> immutable collections</a>.
 *
 * @since 2.0
 */
@GwtCompatible(emulated = true)
public abstract class ImmutableCollection<E> extends AbstractCollection<E> implements Serializable {

    static final int SPLITERATOR_CHARACTERISTICS =
        Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;

    ImmutableCollection() {}

    /**
     * 返回集合中不可修改的迭代器
     *
     * @return
     */
    @Override
    public abstract UnmodifiableIterator<E> iterator();

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, SPLITERATOR_CHARACTERISTICS);
    }

    private static final Object[] EMPTY_ARRAY = {};

    @Override
    public final Object[] toArray() {
        return toArray(EMPTY_ARRAY);
    }

    @CanIgnoreReturnValue
    @Override
    public final <T> T[] toArray(T[] other) {
        //确保参数不为空，使用断言判断不为空，否则抛出异常处理
        checkNotNull(other);
        int size = size();

        if (other.length < size) {
            Object[] internal = internalArray();
            if (internal != null) {
                //复制部分信息处理哦！使用JDK的Arrays.copyRange...进行数据的复制哦
                return Platform.copy(internal, internalArrayStart(), internalArrayEnd(), other);
            }
            other = ObjectArrays.newArray(other, size);
        } else if (other.length > size) {
            other[size] = null;
        }
        copyIntoArray(other, 0);
        return other;
    }

    /**
     * 如果此集合由插入顺序中的元素数组支持，则返回
     *
     * @return
     */
    @Nullable
    Object[] internalArray() {
        return null;
    }

    /**
     * 如果该集合以插入顺序由其元素的数组支持，则返回该集合的元素开始的偏移量
     */
    int internalArrayStart() {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果该集合以插入顺序由其元素的数组支持，则返回该集合的元素结束的偏移量。
     */
    int internalArrayEnd() {
        throw new UnsupportedOperationException();
    }

    /**
     * 是否存在元素哦
     * @param object
     * @return
     */
    @Override
    public abstract boolean contains(@Nullable Object object);

    /**
     *保证抛出异常并使集合未修改，因为是不可变集合！所以不能让其修改哦
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保证抛出异常并使集合未修改，因为是不可变集合！所以不能让其修改哦
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保证抛出异常并使集合未修改，因为是不可变集合！所以不能让其修改哦
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final boolean addAll(Collection<? extends E> newElements) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保证抛出异常并使集合未修改，因为是不可变集合！所以不能让其修改哦
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final boolean removeAll(Collection<?> oldElements) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保证抛出异常并使集合未修改。
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保证抛出异常并使集合未修改。
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final boolean retainAll(Collection<?> elementsToKeep) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保证抛出异常并使集合未修改。
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回一个{@code ImmutableList} ，包含与该集合相同的元素
     * Returns an {@code ImmutableList} containing the same elements, in the same order, as this collection.
     *
     * <P>性能说明：</b>在大多数情况下，这种方法可以快速返回而不必实际复制任何东西。执行复制的确切情况是未定义的，可能会发生变化。
     * 根据具体的情况进行创建不同类型的不可变集合的实现！感觉这样的处理思路更加友好哦！
     *
     * @since 2.0
     */
    public ImmutableList<E> asList() {
        switch (size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                return ImmutableList.of(iterator().next());
            default:
                return new RegularImmutableAsList<E>(this, toArray());
        }
    }

    /**
     * 如果此不可变集合的实现包含对用户创建对象的引用，则返回{@code true}
     * 这些对象无法通过该集合的方法访问。这通常用于确定{@code copyOf}实现是否应该做出明确的副本以避免内存泄漏。
     *
     * 是部分视图
     * Returns {@code true} if this immutable collection's implementation contains references to user-created objects
     * that aren't accessible via this collection's methods. This is generally used to determine whether {@code copyOf}
     * implementations should make an explicit copy to avoid memory leaks.
     */
    abstract boolean isPartialView();

    /**
     * 将此不可变集合的内容复制到指定的偏移量的指定数组中
     *
     * 这里的this就是这个集合哦~可以通过集合获取到具体的数据的信息
     */
    @CanIgnoreReturnValue
    int copyIntoArray(Object[] dst, int offset) {
        for (E e : this) {
            dst[offset++] = e;
        }
        return offset;
    }

    /**
     * 默认情况下，我们序列化为不可变的，最简单的方法。
     * @return
     */
    Object writeReplace() {
        return new ImmutableList.SerializedForm(toArray());
    }

    /**
     * 对于不可变集合的抽象的 builders 抽象的基类，典型的建筑者模式的使用哦
     * @since 10.0
     */
    public abstract static class Builder<E> {
        /**
         * 默认初始化数据的大小
         */
        static final int DEFAULT_INITIAL_CAPACITY = 4;

        /**
         * 扩容处理
         * @param oldCapacity 旧容量
         * @param minCapacity 最小容量
         * @return
         */
        static int expandedCapacity(int oldCapacity, int minCapacity) {
            if (minCapacity < 0) {
                throw new AssertionError("cannot store more than MAX_VALUE elements wang");
            }

            /**
             * 小心溢出
             * 2+(2>>1)+1 =4
             * 3+(3>>1)+1 =5
             */
            int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
            if (newCapacity < minCapacity) {
                newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
            }
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE;
            }
            return newCapacity;
        }

        Builder() {}

        /**
         *向正在生成的数据中添加一个元素！切这个元素不能为空哦~
         *
         * <p>Note that each builder class covariantly returns its own type from this method.
         *
         * @param element the element to add
         * @return this {@code Builder} instance
         * @throws NullPointerException if {@code element} is null
         */
        @CanIgnoreReturnValue
        public abstract Builder<E> add(E element);

        /**
         * 添加一系统的元素，不能为空哦！
         * @param elements
         * @return
         */
        @CanIgnoreReturnValue
        public Builder<E> add(E... elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        /**
         * @param elements the elements to add
         * @return this {@code Builder} instance
         * @throws NullPointerException if {@code elements} is null or contains a null element
         *
         *
         * 实现这个接口（Iterable）允许对象成为“每个循环”语句的目标。
         */
        @CanIgnoreReturnValue
        public Builder<E> addAll(Iterable<? extends E> elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        /**
         * 各种类型的元素都是可以通过这个来添加的哦
         * @param elements
         * @return
         */
        @CanIgnoreReturnValue
        public Builder<E> addAll(Iterator<? extends E> elements) {
            while (elements.hasNext()) {
                add(elements.next());
            }
            return this;
        }

        /**
         * 返回通过建筑者模式创建的元素的值的信息！且当前的元素是不可变化的！
         *
         * 注意，每一个Builder类都从这个方法中适当地返回{@代码*imutabelCopy}的适当类型。
         */
        public abstract ImmutableCollection<E> build();
    }
}
