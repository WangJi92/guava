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

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.CollectPreconditions.checkNonnegative;
import static com.google.common.collect.ObjectArrays.checkElementsNotNull;
import static com.google.common.collect.RegularImmutableList.EMPTY;

/**
 * 作为一个List 它的内容永远不会改变，还有许多其他重要的属性{@link ImmutableCollection}.
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/ImmutableCollectionsExplained"> immutable collections</a>.
 *
 * @author Kevin Bourrillion
 * @see ImmutableMap
 * @see ImmutableSet
 * @since 2.0
 */
public abstract class ImmutableList<E> extends ImmutableCollection<E>
    implements List<E>, RandomAccess {

    /**
     * 干啥用来着哦
     *
     * @param <E>
     * @return
     */
    @Beta
    public static <E> Collector<E, ?, ImmutableList<E>> toImmutableList() {
        return CollectCollectors.toImmutableList();
    }

    /**
     * 返回空的不可变列表。该列表的行为和执行与{@link Collections#EMPTY_LIST}相当，并且主要为了代码的一致性和可维护性而更可取。
     */
    @SuppressWarnings("unchecked")
    public static <E> ImmutableList<E> of() {
        return (ImmutableList<E>)EMPTY;
    }

    /**
     * 和这个方法也是一样的！区别在于 这个方法不能接受一个空的元素 {@link Collections#singleton} 由子类去是实现这个重要的方法！
     *
     * @throws NullPointerException if {@code element} is null
     */
    public static <E> ImmutableList<E> of(E element) {
        return new SingletonImmutableList<E>(element);
    }

    /**
     * 按顺序返回包含给定元素的不可变列表
     *
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2) {
        return construct(e1, e2);
    }

    //region 使用这一的参数的方法！其实非常的实用，比使用builder模式更加的简单的方便！一行代码就搞定了！用起来十分的舒服的哦

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3) {
        return construct(e1, e2, e3);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4) {
        return construct(e1, e2, e3, e4);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5) {
        return construct(e1, e2, e3, e4, e5);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
        return construct(e1, e2, e3, e4, e5, e6);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return construct(e1, e2, e3, e4, e5, e6, e7);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return construct(e1, e2, e3, e4, e5, e6, e7, e8);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return construct(e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(
        E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return construct(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }

    /**
     * @throws NullPointerException if any element is null
     */
    public static <E> ImmutableList<E> of(
        E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10, E e11) {
        return construct(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
    }
    //endregion

    /**
     * 按照数学的去处理哦~看起来还是十分的强大得哦！ 其他的长度不要超过{@code Integer.MAX_VALUE - 12}.
     *
     * @throws NullPointerException if any element is null
     * @since 3.0 (source-compatible since 2.0)
     */
    @SafeVarargs // For Eclipse. For internal javac we have disabled this pointless type of warning.
    public static <E> ImmutableList<E> of(
        E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10, E e11, E e12, E... others) {

        //检测参数是否异常！十分的严谨
        checkArgument(
            others.length <= Integer.MAX_VALUE - 12,
            "the total number of elements must fit in an int");
        Object[] array = new Object[12 + others.length];
        array[0] = e1;
        array[1] = e2;
        array[2] = e3;
        array[3] = e4;
        array[4] = e5;
        array[5] = e6;
        array[6] = e7;
        array[7] = e8;
        array[8] = e9;
        array[9] = e10;
        array[10] = e11;
        array[11] = e12;
        System.arraycopy(others, 0, array, 12, others.length);
        return construct(array);
    }

    /**
     * 按顺序返回包含给定元素的不可变列表
     *
     * @throws NullPointerException if any of {@code elements} is null
     */
    public static <E> ImmutableList<E> copyOf(Iterable<? extends E> elements) {
        //检测参数非空
        checkNotNull(elements);
        return (elements instanceof Collection) ? copyOf((Collection<? extends E>)elements) : copyOf(
            elements.iterator());
    }

    /**
     * 如果当前的元素本身就是继承啦ImmutableCollection集合，这样的处理更加的简单！不需要再次使用曾经创建的过程啦！
     *
     * @throws NullPointerException if any of {@code elements} is null
     */
    public static <E> ImmutableList<E> copyOf(Collection<? extends E> elements) {
        if (elements instanceof ImmutableCollection) {
            @SuppressWarnings("unchecked")
            //按顺序返回包含给定元素的不可变列表。
                ImmutableList<E> list = ((ImmutableCollection<E>)elements).asList();
            return list.isPartialView() ? ImmutableList.<E>asImmutableList(list.toArray()) : list;
        }
        return construct(elements.toArray());
    }

    /**
     * 复制迭代器的值（我们对0个或1个元素的特殊情况，但进一步疯狂）
     *
     * @throws NullPointerException if any of {@code elements} is null
     */
    public static <E> ImmutableList<E> copyOf(Iterator<? extends E> elements) {
        //对于只有0 个或者一个元素！的处理十分的友好！应该采取什么样的策略呢！
        if (!elements.hasNext()) {
            return of();
        }
        //只有一个元素的处理
        E first = elements.next();
        if (!elements.hasNext()) {
            return of(first);
        } else {
            //多个元素的处理，使用builder去处理
            return new ImmutableList.Builder<E>().add(first).addAll(elements).build();
        }
    }

    /**
     * 按顺序返回包含给定元素的不可变列表。
     *
     * @throws NullPointerException if any of {@code elements} is null
     * @since 3.0
     */
    public static <E> ImmutableList<E> copyOf(E[] elements) {
        switch (elements.length) {
            case 0:
                return of();
            case 1:
                return of(elements[0]);
            default:
                //这里使用了 元素的复制clone
                return construct(elements.clone());
        }
    }

    /**
     * 返回包含给定元素的不可变列表，按自然顺序排序，所使用的排序算法是稳定的，所以比较为相等的元素将按照它们在输入中出现的顺序保持。 如果您的数据没有重复use {@code
     * ImmutableSortedSet.copyOf(elements)} 或者您希望重复元素 if you want a {@code List} you can use its {@code asList()} view.
     * <p>
     * 对于Java8的使用者，如果你想转为{@link java.util.stream.Stream} 去排序，可以使用 {@code stream.sorted().collect(toImmutableList())}
     *
     * @throws NullPointerException if any element in the input is null
     * @since 21.0
     */
    public static <E extends Comparable<? super E>> ImmutableList<E> sortedCopyOf(
        Iterable<? extends E> elements) {
        //将迭代的元素转换为数组对象，然后进行排序，之后进行创建不可变集合！
        Comparable<?>[] array = Iterables.toArray(elements, new Comparable<?>[0]);
        checkElementsNotNull((Object[])array);
        Arrays.sort(array);
        return asImmutableList(array);
    }

    /**
     * 看上面这个方法就知道啦~这里仅仅添加了一个比较器
     *
     * @throws NullPointerException if any element in the input is null
     * @since 21.0
     */
    public static <E> ImmutableList<E> sortedCopyOf(
        Comparator<? super E> comparator, Iterable<? extends E> elements) {
        checkNotNull(comparator);
        @SuppressWarnings("unchecked")
        E[] array = (E[])Iterables.toArray(elements);
        checkElementsNotNull(array);
        Arrays.sort(array, comparator);
        return asImmutableList(array);
    }

    /**
     * 将数组视为不可变列表。检查空值；不复制。
     */
    private static <E> ImmutableList<E> construct(Object... elements) {
        return asImmutableList(checkElementsNotNull(elements));
    }

    /**
     * 将数组视为不可变列表。不检查空值；不复制。（在执行这个方法之前！其他的方法已经将一部分的事情都已经处理好啦）
     *
     * <p>The array must be internally created.
     */
    static <E> ImmutableList<E> asImmutableList(Object[] elements) {
        return asImmutableList(elements, elements.length);
    }

    /**
     * 将数组视为不可变列表。如果指定的范围不覆盖完整的数组，则复制。不检查空值。
     */
    static <E> ImmutableList<E> asImmutableList(Object[] elements, int length) {
        switch (length) {
            //对于不同的长度的处理，选择不同的子类去是实现这样的逻辑
            case 0:
                return of();
            case 1:
                return of((E)elements[0]);
            default:
                if (length < elements.length) {
                    elements = Arrays.copyOf(elements, length);
                }
                return new RegularImmutableList<E>(elements);
        }
    }

    ImmutableList() {}

    @Override
    public UnmodifiableIterator<E> iterator() {
        return listIterator();
    }

    @Override
    public UnmodifiableListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * 由其他的类去实现迭代器处理的逻辑！
     *
     * @param index
     * @return
     */
    @Override
    public UnmodifiableListIterator<E> listIterator(int index) {
        return new AbstractIndexedListIterator<E>(size(), index) {
            @Override
            protected E get(int index) {
                return ImmutableList.this.get(index);
            }
        };
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        checkNotNull(consumer);
        int n = size();
        for (int i = 0; i < n; i++) {
            consumer.accept(get(i));
        }
    }

    /**
     * 获取元素存在的位置
     *
     * @param object
     * @return
     */
    @Override
    public int indexOf(@Nullable Object object) {
        return (object == null) ? -1 : Lists.indexOfImpl(this, object);
    }

    /**
     * 从后往前遍历数据
     *
     * @param object
     * @return
     */
    @Override
    public int lastIndexOf(@Nullable Object object) {
        return (object == null) ? -1 : Lists.lastIndexOfImpl(this, object);
    }

    /**
     * 会遍历整个列表的数据哦
     *
     * @param object
     * @return
     */
    @Override
    public boolean contains(@Nullable Object object) {
        return indexOf(object) >= 0;
    }

    // constrain the return type to ImmutableList<E>

    /**
     * 返回一个不可变的子列表的数据信息
     *
     * @param fromIndex
     * @param toIndex
     * @return
     */
    @Override
    public ImmutableList<E> subList(int fromIndex, int toIndex) {
        checkPositionIndexes(fromIndex, toIndex, size());
        int length = toIndex - fromIndex;
        if (length == size()) {
            return this;
        } else if (length == 0) {
            return of();
        } else if (length == 1) {
            return of(get(fromIndex));
        } else {
            //未检查的子列表
            return subListUnchecked(fromIndex, toIndex);
        }
    }

    /**
     * Called by the default implementation of {@link #subList} when {@code toIndex - fromIndex > 1}, after index
     * validation has already been performed.
     */
    ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
        return new SubList(fromIndex, toIndex - fromIndex);
    }

    /**
     * 子列表数据信息！这里没有进行重新的数据的复制处理！只是对于原数据的下标进行操作哦！
     */
    class SubList extends ImmutableList<E> {
        final transient int offset;
        final transient int length;

        SubList(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public E get(int index) {
            checkElementIndex(index, length);
            return ImmutableList.this.get(index + offset);
        }

        @Override
        public ImmutableList<E> subList(int fromIndex, int toIndex) {
            checkPositionIndexes(fromIndex, toIndex, length);
            return ImmutableList.this.subList(fromIndex + offset, toIndex + offset);
        }

        /**
         * 这个是一个子数据哦！
         *
         * @return
         */
        @Override
        boolean isPartialView() {
            return true;
        }
    }

    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final boolean addAll(int index, Collection<? extends E> newElements) {
        throw new UnsupportedOperationException();
    }

    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @CanIgnoreReturnValue
    @Deprecated
    @Override
    public final E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns this list instance.
     *
     * @since 2.0
     */
    @Override
    public final ImmutableList<E> asList() {
        return this;
    }

    /**
     * [JDK8源码之Spliterator并行遍历迭代器](https://blog.csdn.net/lh513828570/article/details/56673804)
     *
     * @return
     */
    @Override
    public Spliterator<E> spliterator() {
        return CollectSpliterators.indexed(size(), SPLITERATOR_CHARACTERISTICS, this::get);
    }

    @Override
    int copyIntoArray(Object[] dst, int offset) {
        //这个循环对于随机访问实例来说是更快的，这是不可变的
        int size = size();
        for (int i = 0; i < size; i++) {
            dst[offset + i] = get(i);
        }
        return offset + size;
    }

    /**
     * 数据反转处理哦！交给子类去实现哦 Returns a view of this immutable list in reverse order. For example, {@code ImmutableList.of(1,
     * 2, 3).reverse()} is equivalent to {@code ImmutableList.of(3, 2, 1)}.
     *
     * @return a view of this immutable list in reverse order
     * @since 7.0
     */
    public ImmutableList<E> reverse() {
        return (size() <= 1) ? this : new ReverseImmutableList<E>(this);
    }

    /**
     * 主要处理正向下标与反向下标的不同之处的处理！
     *
     * @param <E>
     */
    private static class ReverseImmutableList<E> extends ImmutableList<E> {
        /**
         * 保存之前顺序的实例
         */
        private final transient ImmutableList<E> forwardList;

        ReverseImmutableList(ImmutableList<E> backingList) {
            this.forwardList = backingList;
        }

        /**
         * 处理反转后数据的下标的信息
         *
         * @param index
         * @return
         */
        private int reverseIndex(int index) {
            return (size() - 1) - index;
        }

        /**
         * 反转后位置
         *
         * @param index
         * @return
         */
        private int reversePosition(int index) {
            return size() - index;
        }

        @Override
        public ImmutableList<E> reverse() {
            return forwardList;
        }

        @Override
        public boolean contains(@Nullable Object object) {
            return forwardList.contains(object);
        }

        @Override
        public int indexOf(@Nullable Object object) {
            int index = forwardList.lastIndexOf(object);
            return (index >= 0) ? reverseIndex(index) : -1;
        }

        @Override
        public int lastIndexOf(@Nullable Object object) {
            int index = forwardList.indexOf(object);
            return (index >= 0) ? reverseIndex(index) : -1;
        }

        @Override
        public ImmutableList<E> subList(int fromIndex, int toIndex) {
            checkPositionIndexes(fromIndex, toIndex, size());
            return forwardList.subList(reversePosition(toIndex), reversePosition(fromIndex)).reverse();
        }

        @Override
        public E get(int index) {
            checkElementIndex(index, size());
            return forwardList.get(reverseIndex(index));
        }

        @Override
        public int size() {
            return forwardList.size();
        }

        @Override
        boolean isPartialView() {
            return forwardList.isPartialView();
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return Lists.equalsImpl(this, obj);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        int n = size();
        for (int i = 0; i < n; i++) {
            hashCode = 31 * hashCode + get(i).hashCode();
            hashCode = ~~hashCode;
        }
        return hashCode;
    }

    /*
     * 将不可变语言作为逻辑内容进行序列化。这确保了实现类型不会泄漏到序列化表示中。
     * Serializes ImmutableLists as their logical contents. This ensures that
     * implementation types do not leak into the serialized representation.
     */
    static class SerializedForm implements Serializable {
        final Object[] elements;

        SerializedForm(Object[] elements) {
            this.elements = elements;
        }

        Object readResolve() {
            return copyOf(elements);
        }

        private static final long serialVersionUID = 0;
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @Override
    Object writeReplace() {
        return new SerializedForm(toArray());
    }

    /**
     * 构建一个建筑者模式哦 Returns a new builder. The generated builder is equivalent to the builder created by the {@link
     * Builder} constructor.
     */
    public static <E> Builder<E> builder() {
        return new Builder<E>();
    }

    /**
     * 构建并设置大小
     *
     * @since 23.1
     */
    @Beta
    public static <E> Builder<E> builderWithExpectedSize(int expectedSize) {
        checkNonnegative(expectedSize, "expectedSize");
        return new ImmutableList.Builder<E>(expectedSize);
    }

    /**
     * 实例：
     * <pre>{@code
     * public static final ImmutableList<Color> GOOGLE_COLORS
     *     = new ImmutableList.Builder<Color>()
     *         .addAll(WEBSAFE_COLORS)
     *         .add(new Color(0, 191, 255))
     *         .build();
     * }</pre>
     *
     * <p>Elements appear in the resulting list in the same order they were added to the builder.
     *
     * <p>Builder instances can be reused; it is safe to call {@link #build} multiple times to build
     * multiple lists in series. Each new list contains all the elements of the ones created before it.
     *
     * @since 2.0
     */
    public static final class Builder<E> extends ImmutableCollection.Builder<E> {
        /**
         * 保存的数据信息
         */
        @VisibleForTesting
        Object[] contents;
        private int size;
        private boolean forceCopy;

        /**
         * Creates a new builder. The returned builder is equivalent to the builder generated by {@link
         * ImmutableList#builder}.
         */
        public Builder() {
            this(DEFAULT_INITIAL_CAPACITY);
        }

        Builder(int capacity) {
            this.contents = new Object[capacity];
            this.size = 0;
        }

        private void getReadyToExpandTo(int minCapacity) {
            if (contents.length < minCapacity) {
                this.contents = Arrays.copyOf(contents, expandedCapacity(contents.length, minCapacity));
                forceCopy = false;
            } else if (forceCopy) {
                contents = Arrays.copyOf(contents, contents.length);
                forceCopy = false;
            }
        }

        @CanIgnoreReturnValue
        @Override
        public Builder<E> add(E element) {
            checkNotNull(element);
            getReadyToExpandTo(size + 1);
            contents[size++] = element;
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public Builder<E> add(E... elements) {
            checkElementsNotNull(elements);
            add(elements, elements.length);
            return this;
        }

        private void add(Object[] elements, int n) {
            getReadyToExpandTo(size + n);
            System.arraycopy(elements, 0, contents, size, n);
            size += n;
        }

        @CanIgnoreReturnValue
        @Override
        public Builder<E> addAll(Iterable<? extends E> elements) {
            checkNotNull(elements);
            if (elements instanceof Collection) {
                Collection<?> collection = (Collection<?>)elements;
                getReadyToExpandTo(size + collection.size());
                if (collection instanceof ImmutableCollection) {
                    ImmutableCollection<?> immutableCollection = (ImmutableCollection<?>)collection;
                    size = immutableCollection.copyIntoArray(contents, size);
                    return this;
                }
            }
            super.addAll(elements);
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        @CanIgnoreReturnValue
        Builder<E> combine(Builder<E> builder) {
            checkNotNull(builder);
            add(builder.contents, builder.size);
            return this;
        }

        /**
         * 根据数据创建一个不可变的集合
         *
         * @return
         */
        @Override
        public ImmutableList<E> build() {
            forceCopy = true;
            return asImmutableList(contents, size);
        }
    }
}
