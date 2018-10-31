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
import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.CollectPreconditions.checkNonnegative;

/**
 * 不可变的Set的集合信息处理 A {@link Set} whose contents will never change, with many other important properties detailed at
 * {@link ImmutableCollection}.
 *
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {
    static final int SPLITERATOR_CHARACTERISTICS =
        ImmutableCollection.SPLITERATOR_CHARACTERISTICS | Spliterator.DISTINCT;

    @Beta
    public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
        return CollectCollectors.toImmutableSet();
    }

    /**
     * 优先于这个类的使用{@link Collections#emptySet}，保证了返回的集合信息不可变
     */
    @SuppressWarnings({"unchecked"})
    public static <E> ImmutableSet<E> of() {
        return (ImmutableSet<E>)RegularImmutableSet.EMPTY;
    }

    /**
     * 优先于使用这个类 {@link Collections#singleton} ，不可变性得到了保证
     */
    public static <E> ImmutableSet<E> of(E element) {
        return new SingletonImmutableSet<E>(element);
    }

    public static <E> ImmutableSet<E> of(E e1, E e2) {
        return construct(2, e1, e2);
    }

    /**
     * 返回一个不可变的集合，其中包含给定元素，减去重复，按第一次指定的顺序。也就是说，如果多个元素{@linkplain Object#equals equal}'
     * 除了第一个之外的所有元素都被忽略。这里和ImmutableList的效果类似的处理思路，简单的API，方便调用，最后集中的去处理
     */
    public static <E> ImmutableSet<E> of(E e1, E e2, E e3) {
        return construct(3, e1, e2, e3);
    }

    public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4) {
        return construct(4, e1, e2, e3, e4);
    }

    public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        return construct(5, e1, e2, e3, e4, e5);
    }

    /**
     * <p>The array {@code others} must not be longer than {@code Integer.MAX_VALUE - 6}
     *
     * @param e1
     * @param e2
     * @param e3
     * @param e4
     * @param e5
     * @param e6
     * @param others
     * @param <E>
     * @return
     */
    public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
        checkArgument(
            others.length <= Integer.MAX_VALUE - 6,
            "the total number of elements must fit in an int");
        final int paramCount = 6;
        Object[] elements = new Object[paramCount + others.length];
        elements[0] = e1;
        elements[1] = e2;
        elements[2] = e3;
        elements[3] = e4;
        elements[4] = e5;
        elements[5] = e6;
        //对于后面的数据进行复制到数组中去，不对其直接引用
        System.arraycopy(others, 0, elements, paramCount, others.length);
        return construct(elements.length, elements);
    }

    /**
     * 这个其实就是之前的非常多的使用API的改造的处理！方便我们的使用！对于只有一个元素，或者没有元素进行了特殊化的处理！且这种类不对于外部就行暴露
     *
     * @throws NullPointerException if any of the first {@code n} elements of {@code elements} is null
     */
    private static <E> ImmutableSet<E> construct(int n, Object... elements) {
        switch (n) {
            case 0:
                //空元素特殊化处理！且子类为非public类，有访问权限的控制！设计这一的API，设计者考虑的比较多！
                return of();
            case 1:
                //只有一个元素的处理
                @SuppressWarnings("unchecked")
                E elem = (E)elements[0];
                return of(elem);
            default:
                //多个的通过建筑者模式进行创建处理，进行了非空性的校验
                SetBuilderImpl<E> builder =
                    new RegularSetBuilderImpl<E>(ImmutableCollection.Builder.DEFAULT_INITIAL_CAPACITY);
                for (int i = 0; i < n; i++) {
                    @SuppressWarnings("unchecked")
                    E e = (E)checkNotNull(elements[i]);
                    builder = builder.add(e);
                }
                return builder.review().build();
        }
    }

    /**
     * 返回包含每个{@code elements}，减去重复项的不可变集，按源序列中每个首先出现的顺序
     *
     * @throws NullPointerException if any of {@code elements} is null
     * @since 7.0 (source-compatible since 2.0)
     */
    public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
        /**
         * 不要按名称引用ImmutableSortedSet，因此它不会引入所有代码
         */
        if (elements instanceof ImmutableSet && !(elements instanceof SortedSet)) {
            @SuppressWarnings("unchecked")
            ImmutableSet<E> set = (ImmutableSet<E>)elements;
            if (!set.isPartialView()) {
                return set;
            }
        } else if (elements instanceof EnumSet) {
            //由子类具体的实现
            return copyOfEnumSet((EnumSet)elements);
        }
        Object[] array = elements.toArray();
        return construct(array.length, array);
    }

    /**
     * @throws NullPointerException if any of {@code elements} is null
     */
    public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
        return (elements instanceof Collection)
            ? copyOf((Collection<? extends E>)elements)
            : copyOf(elements.iterator());
    }

    /**
     * @throws NullPointerException if any of {@code elements} is null
     */
    public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
        /**
         * 0 1 特殊处理
         */
        if (!elements.hasNext()) {
            return of();
        }
        E first = elements.next();
        if (!elements.hasNext()) {
            return of(first);
        } else {
            return new ImmutableSet.Builder<E>().add(first).addAll(elements).build();
        }
    }

    /**
     * 构造复制处理哦！常规的处理手段！
     *
     * @throws NullPointerException if any of {@code elements} is null
     * @since 3.0
     */
    public static <E> ImmutableSet<E> copyOf(E[] elements) {
        switch (elements.length) {
            case 0:
                return of();
            case 1:
                return of(elements[0]);
            default:
                return construct(elements.length, elements.clone());
        }
    }

    /**
     * 由子类具体的实现哦
     *
     * @param enumSet
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static ImmutableSet copyOfEnumSet(EnumSet enumSet) {
        return ImmutableEnumSet.asImmutable(EnumSet.copyOf(enumSet));
    }

    ImmutableSet() {}

    /**
     * Returns {@code true} if the {@code hashCode()} method runs quickly.
     */
    boolean isHashCodeFast() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof ImmutableSet
            && isHashCodeFast()
            && ((ImmutableSet<?>)object).isHashCodeFast()
            && hashCode() != object.hashCode()) {
            return false;
        }
        return Sets.equalsImpl(this, object);
    }

    @Override
    public int hashCode() {
        return Sets.hashCodeImpl(this);
    }

    // This declaration is needed to make Set.iterator() and
    // ImmutableCollection.iterator() consistent.
    @Override
    public abstract UnmodifiableIterator<E> iterator();

    /**
     * 方便执行迭代获取Set中的数据信息
     */
    @LazyInit
    @RetainedWith
    private transient @Nullable ImmutableList<E> asList;

    @Override
    public ImmutableList<E> asList() {
        ImmutableList<E> result = asList;
        return (result == null) ? asList = createAsList() : result;
    }

    ImmutableList<E> createAsList() {
        //方便遍历哦~
        return new RegularImmutableAsList<E>(this, toArray());
    }

    /**
     * Indexed 主要的意思还是方便遍历其中的Set集合中的数据信息
     *
     * @param <E>
     */
    abstract static class Indexed<E> extends ImmutableSet<E> {
        abstract E get(int index);

        @Override
        public UnmodifiableIterator<E> iterator() {
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
                consumer.accept(get(i));
            }
        }

        @Override
        int copyIntoArray(Object[] dst, int offset) {
            return asList().copyIntoArray(dst, offset);
        }

        @Override
        ImmutableList<E> createAsList() {
            return new ImmutableAsList<E>() {
                @Override
                public E get(int index) {
                    return Indexed.this.get(index);
                }

                @Override
                Indexed<E> delegateCollection() {
                    return Indexed.this;
                }
            };
        }
    }

    /**
     * 此类用于序列化所有ImmutableSet实例，但* ImmutableEnumSet / ImmutableSortedSet除外，无论实现类型如何。它捕获它们的“逻辑内容”，并使用公共静态工厂重建它们。
     * 这对于确保特定实现类型的存在是实现细节是必要的。
     */
    private static class SerializedForm implements Serializable {
        final Object[] elements;

        SerializedForm(Object[] elements) {
            this.elements = elements;
        }

        Object readResolve() {
            return copyOf(elements);
        }

        private static final long serialVersionUID = 0;
    }

    @Override
    Object writeReplace() {
        return new SerializedForm(toArray());
    }

    /**
     * 构造一个建筑者创建 不可变对象的信息哦！
     */
    public static <E> Builder<E> builder() {
        return new Builder<E>();
    }

    /**
     * 构造一个建筑者创建 不可变对象的信息哦！预估大小！
     *
     * @param expectedSize
     * @param <E>
     * @return
     */
    @Beta
    public static <E> Builder<E> builderWithExpectedSize(int expectedSize) {
        checkNonnegative(expectedSize, "expectedSize");
        return new Builder<E>(expectedSize);
    }

    /**
     * 从元素中的前n个对象构建一个新的开放式散列表，Hash冲突的处理代码
     */
    static Object[] rebuildHashTable(int newTableSize, Object[] elements, int n) {
        Object[] hashTable = new Object[newTableSize];
        int mask = hashTable.length - 1;
        for (int i = 0; i < n; i++) {
            Object e = elements[i];
            //处理Hash值！然后添加冲突处理！
            int j0 = Hashing.smear(e.hashCode());
            for (int j = j0; ; j++) {
                int index = j & mask;
                if (hashTable[index] == null) {
                    hashTable[index] = e;
                    break;
                }
            }
        }
        return hashTable;
    }

    /**
     * 这个才是真正的Builder ！但是这个builder的实现不是自己！而是使用了代理模式 实现有Guava的实现以及JDK代理的实现！两种方式实现，好像就是为了防止Hash泛滥的处理！
     * 这里的实现细节挺好玩的！有些东西的思考值得我们去学习 具体的的实现细节自己可以尝试着去看一下！ A builder for creating {@code ImmutableSet} instances. Example:
     *
     * <pre>{@code
     * static final ImmutableSet<Color> GOOGLE_COLORS =
     *     ImmutableSet.<Color>builder()
     *         .addAll(WEBSAFE_COLORS)
     *         .add(new Color(0, 191, 255))
     *         .build();
     * }</pre>
     * <p>
     * 元素以与第一次添加到生成器相同的顺序出现在生成的集合中。
     * <p>
     * 构建不会改变生成器的状态，因此仍然可以添加更多的元素并重新构建。
     *
     * @since 2.0
     */
    public static class Builder<E> extends ImmutableCollection.Builder<E> {
        /**
         * builder的两个不同的实现哦
         */
        private SetBuilderImpl<E> impl;

        /**
         * 这个只是为了顶层是否进行复制的处理
         */
        boolean forceCopy;

        public Builder() {
            this(DEFAULT_INITIAL_CAPACITY);
        }

        Builder(int capacity) {
            //默认的builder的是吸纳
            impl = new RegularSetBuilderImpl<E>(capacity);
        }

        Builder(@SuppressWarnings("unused") boolean subclass) {
            this.impl = null; // unused
        }

        @VisibleForTesting
        void forceJdk() {
            //或者处理一些切换为JDK的
            this.impl = new JdkBackedSetBuilderImpl<E>(impl);
        }

        final void copyIfNecessary() {
            if (forceCopy) {
                copy();
                forceCopy = false;
            }
        }

        void copy() {
            impl = impl.copy();
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> add(E element) {
            checkNotNull(element);
            copyIfNecessary();
            impl = impl.add(element);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> add(E... elements) {
            super.add(elements);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> addAll(Iterable<? extends E> elements) {
            //最终还是调用add操作的
            super.addAll(elements);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        Builder<E> combine(Builder<E> other) {
            copyIfNecessary();
            this.impl = this.impl.combine(other.impl);
            return this;
        }

        @Override
        public ImmutableSet<E> build() {
            forceCopy = true;
            impl = impl.review();
            return impl.build();
        }
    }

    /**
     * 可交换的内部实现了ImmutableSet.Builder
     *
     * @param <E>
     */
    private abstract static class SetBuilderImpl<E> {
        /**
         * 非重复元素（由子类去调用！由于有两个子类~这里的作用就是可以相互的转换） 这个在SetBuilderImpl的子类去处理，在add非重复的元素之后进行添加哦 {@link
         * JdkBackedSetBuilderImpl#add(Object) 看这里哦}
         * <p>
         * 其实这里保存的数据就是一个牺牲空间换时间的做法！由于Hash表保存数据肯定会存在部分数据为空， 不太会统计Hash表的大小和长度以及内部的具体数据的遍历都不太好处理 {@link RegularImmutableSet
         * 这里就可以看到两个的区别}其实这里保存副本的意义就是这样的！
         */
        E[] dedupedElements;

        /**
         * 不同元素的大小
         */
        int distinct;

        /**
         * 根据容量进行构造处理
         *
         * @param expectedCapacity
         */
        @SuppressWarnings("unchecked")
        SetBuilderImpl(int expectedCapacity) {
            this.dedupedElements = (E[])new Object[expectedCapacity];
            this.distinct = 0;
        }

        /**
         * 使用当前类型的处理数据进行复制处理
         *
         * @param toCopy
         */
        SetBuilderImpl(SetBuilderImpl<E> toCopy) {
            this.dedupedElements = Arrays.copyOf(toCopy.dedupedElements, toCopy.dedupedElements.length);
            this.distinct = toCopy.distinct;
        }

        /**
         * 如果需要，调整内部数据结构以存储指定数量的不同元素。
         */
        private void ensureCapacity(int minCapacity) {
            if (minCapacity > dedupedElements.length) {
                int newCapacity =
                    ImmutableCollection.Builder.expandedCapacity(dedupedElements.length, minCapacity);
                dedupedElements = Arrays.copyOf(dedupedElements, newCapacity);
            }
        }

        /**
         * 将E添加到去重复元素的插入顺序数组中,并进行扩容处理（这）
         *
         * @param e
         */
        final void addDedupedElement(E e) {
            ensureCapacity(distinct + 1);
            dedupedElements[distinct++] = e;
        }

        /**
         * 将E添加到此SetBuilderImpl中，返回更新后的结果。只使用返回 this， 因为如果检测到哈希洪泛，我们可能会切换实现。 由子类去实现去重的处理
         */
        abstract SetBuilderImpl<E> add(E e);

        /**
         * 合并处理哦~这里会自动的根据hash值就像排除粗粒 合并代码的信息 Adds all the elements from the specified SetBuilderImpl to this
         * SetBuilderImpl
         *
         * @param other
         * @return
         */
        final SetBuilderImpl<E> combine(SetBuilderImpl<E> other) {
            SetBuilderImpl<E> result = this;
            for (int i = 0; i < other.distinct; i++) {
                result = result.add(other.dedupedElements[i]);
            }
            return result;
        }

        /**
         * 复制构建，从已经创建好的一个SetBuilderImpl中复制信息处理穿甲一个新的数据！ 主要是这里引用了JDK的方式进行处理，会可能设计两个之间的切换处理
         */
        abstract SetBuilderImpl<E> copy();

        /**
         * 在build 之前调用这个，对于内部数据进行检查（例如收缩不必要的大结构或检测以前未被注意的哈希洪泛）
         */
        SetBuilderImpl<E> review() {
            return this;
        }

        /**
         * 构建表的信息
         *
         * @return
         */
        abstract ImmutableSet<E> build();
    }

    /**
     * 我们使用power-of-2表，这是2的幂的最高int
     */
    static final int MAX_TABLE_SIZE = Ints.MAX_POWER_OF_TWO;

    /**
     * 期望的负荷因素 代表我们能够紧紧包装东西的程度。( Represents how tightly we can pack things, as a maximum.)
     */
    private static final double DESIRED_LOAD_FACTOR = 0.7;

    /**
     * 如果集合中包含这么多元素，它将“最大化”表格大小!相当于当前的数据量有当前表格的数量 * 负载系数 就需要扩容处理啦 If the set has this many elements, it will "max out"
     * the table size 隔断(CUTOFF)
     */
    private static final int CUTOFF = (int)(MAX_TABLE_SIZE * DESIRED_LOAD_FACTOR);

    /**
     * 返回适合哈希表的后备数组的数组大小，该哈希表在其实现中使用带有线性探测的开放寻址。返回的大小是2的最小幂， 它可以容纳具有所需载荷因子的setSize元素。始终至少返回setSize + 2。
     */
    @VisibleForTesting
    static int chooseTableSize(int setSize) {
        setSize = Math.max(setSize, 2);
        // Correct the size for open addressing to match desired load factor.
        // 更正开放寻址的大小以匹配所需的负载系数,已经超过了最大的负载了就不处理啦！直接返回最大的数据就好了
        if (setSize < CUTOFF) {
            // Round up to the next highest power of 2.
            //找到当前元素的最后的2的次幂，这个是根据科学的hash处理有关系！0.7倍的处理有关系
            int tableSize = Integer.highestOneBit(setSize - 1) << 1;
            while (tableSize * DESIRED_LOAD_FACTOR < setSize) {
                tableSize <<= 1;
            }
            return tableSize;
        }
        //桌子不能完全填满，否则我们会得到无限的口袋
        checkArgument(setSize < MAX_TABLE_SIZE, "collection too large");
        return MAX_TABLE_SIZE;
    }

    /**
     * We attempt to detect deliberate hash flooding attempts, and if one is detected, fall back to a wrapper around
     * j.u.HashSet, which has built in flooding protection. HASH_FLOODING_FPP is the maximum allowed probability of
     * falsely detecting a hash flooding attack if the input is randomly generated.
     *
     * <p>MAX_RUN_MULTIPLIER was determined experimentally to match this FPP.
     */
    static final double HASH_FLOODING_FPP = 0.001;

    /**
     * NB: 是的，这是惊人的高，但这是实验所说的必要
     */
    static final int MAX_RUN_MULTIPLIER = 12;

    /**
     * TODO 没有仔细的看这个检测Hash分布的处理 检查整个哈希表是否存在糟糕的哈希分布。取O（n） Checks the whole hash table for poor hash distribution. Takes
     * O(n).
     *
     * <p>The online hash flooding detecting in RegularSetBuilderImpl.add can detect e.g. many exactly
     * matching hash codes, which would cause construction to take O(n^2), but can't detect e.g. hash codes
     * adversarially designed to go into ascending table locations, which keeps construction O(n) (as desired) but then
     * can have O(n) queries later.
     *
     * <p>If this returns false, then no query can take more than O(log n).
     * 如果返回false，则查询不能超过O（log n）。
     *
     * <p>Note that for a RegularImmutableSet with elements with truly random hash codes, contains
     * operations take expected O(1) time but with high probability take O(log n) for at least some element.
     * (https://en.wikipedia.org/wiki/Linear_probing#Analysis)
     */
    static boolean hashFloodingDetected(Object[] hashTable) {
        int maxRunBeforeFallback = maxRunBeforeFallback(hashTable.length);

        // Test for a run wrapping around the end of the table, then check for runs in the middle.
        int endOfStartRun;
        for (endOfStartRun = 0; endOfStartRun < hashTable.length; ) {
            if (hashTable[endOfStartRun] == null) {
                break;
            }
            endOfStartRun++;
            if (endOfStartRun > maxRunBeforeFallback) {
                return true;
            }
        }
        int startOfEndRun;
        for (startOfEndRun = hashTable.length - 1; startOfEndRun > endOfStartRun; startOfEndRun--) {
            if (hashTable[startOfEndRun] == null) {
                break;
            }
            if (endOfStartRun + (hashTable.length - 1 - startOfEndRun) > maxRunBeforeFallback) {
                return true;
            }
        }
        for (int i = endOfStartRun + 1; i < startOfEndRun; i++) {
            for (int runLength = 0; i < startOfEndRun && hashTable[i] != null; i++) {
                runLength++;
                if (runLength > maxRunBeforeFallback) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 如果在指定大小的表中填充了多个连续位置，则报告可能的散列泛滥。 If more than this many consecutive positions are filled in a table of the
     * specified size, report probable hash flooding.
     */
    static int maxRunBeforeFallback(int tableSize) {
        return MAX_RUN_MULTIPLIER * IntMath.log2(tableSize, RoundingMode.UNNECESSARY);
    }

    /**
     * Default implementation of the guts of ImmutableSet.Builder, creating an open-addressed hash table and
     * deduplicating elements as they come, so it only allocates O(max(distinct, expectedCapacity)) rather than O(calls
     * to add).
     *
     * <p>This implementation attempts to detect hash flooding, and if it's identified, falls back to
     * JdkBackedSetBuilderImpl.
     */
    private static final class RegularSetBuilderImpl<E> extends SetBuilderImpl<E> {
        /**
         * 整个数据的HashTable的存储数据的数组
         */
        private Object[] hashTable;
        /**
         * 最大运行前后退
         */
        private int maxRunBeforeFallback;
        /**
         * 展开表阈值
         */
        private int expandTableThreshold;
        /**
         * 哈希码（所有的元素哈希码之和的大小）
         */
        private int hashCode;

        RegularSetBuilderImpl(int expectedCapacity) {
            super(expectedCapacity);
            //根据长度计算HashTable表的长度
            int tableSize = chooseTableSize(expectedCapacity);

            //创建Hash表的信息
            this.hashTable = new Object[tableSize];

            //Hash泛滥的标识信息
            this.maxRunBeforeFallback = maxRunBeforeFallback(tableSize);
            //展开阀值信息
            this.expandTableThreshold = (int)(DESIRED_LOAD_FACTOR * tableSize);
        }

        RegularSetBuilderImpl(RegularSetBuilderImpl<E> toCopy) {
            super(toCopy);
            this.hashTable = Arrays.copyOf(toCopy.hashTable, toCopy.hashTable.length);
            this.maxRunBeforeFallback = toCopy.maxRunBeforeFallback;
            this.expandTableThreshold = toCopy.expandTableThreshold;
            this.hashCode = toCopy.hashCode;
        }

        /**
         * 是否需要扩容的处理
         *
         * @param minCapacity
         */
        void ensureTableCapacity(int minCapacity) {
            //是否超过了阀值
            if (minCapacity > expandTableThreshold && hashTable.length < MAX_TABLE_SIZE) {
                int newTableSize = hashTable.length * 2;

                //重建HashTable的数据信息
                hashTable = rebuildHashTable(newTableSize, dedupedElements, distinct);

                //重新计算Hash泛滥的标识
                maxRunBeforeFallback = maxRunBeforeFallback(newTableSize);

                //重新计算下次扩容的大小
                expandTableThreshold = (int)(DESIRED_LOAD_FACTOR * newTableSize);
            }
        }

        @Override
        SetBuilderImpl<E> add(E e) {
            checkNotNull(e);
            int eHash = e.hashCode();
            int i0 = Hashing.smear(eHash);
            int mask = hashTable.length - 1;
            for (int i = i0; i - i0 < maxRunBeforeFallback; i++) {
                //这个是处理？Hash泛滥？TODO 没有看懂
                int index = i & mask;
                Object tableEntry = hashTable[index];
                if (tableEntry == null) {
                    //这里保存当前存在的所有的数据！方便遍历！以及统计数量，以及迭代器遍历的支持
                    addDedupedElement(e);
                    hashTable[index] = e;
                    hashCode += eHash;
                    //是否需要扩容呢？
                    ensureTableCapacity(distinct);
                    return this;
                } else if (tableEntry.equals(e)) {
                    //不是新的元素，忽略
                    return this;
                }
            }
            //由于长时间运行，我们失去了循环(是不是Hash泛滥了...);回归到JDK impl
            return new JdkBackedSetBuilderImpl<E>(this).add(e);
        }

        @Override
        SetBuilderImpl<E> copy() {
            //复制一个副本哦~
            return new RegularSetBuilderImpl<E>(this);
        }

        @Override
        SetBuilderImpl<E> review() {
            //build之前的处理
            int targetTableSize = chooseTableSize(distinct);
            if (targetTableSize * 2 < hashTable.length) {
                hashTable = rebuildHashTable(targetTableSize, dedupedElements, distinct);
            }
            //是否hash泛滥？否则使用JDK默认的处理！
            return hashFloodingDetected(hashTable) ? new JdkBackedSetBuilderImpl<E>(this) : this;
        }

        @Override
        ImmutableSet<E> build() {
            //对于不同大小的进行处理哦~~
            switch (distinct) {
                case 0:
                    return of();
                case 1:
                    return of(dedupedElements[0]);
                default:
                    //hashTable是为了快速的获取数据信息，这里的table的大小不是真实的数据的大小
                    //dedupedElements 是为了保持当前所有的table中的数据，按照顺序的插入
                    Object[] elements =
                        (distinct == dedupedElements.length)
                            ? dedupedElements
                            : Arrays.copyOf(dedupedElements, distinct);
                    return new RegularImmutableSet<E>(elements, hashCode, hashTable, hashTable.length - 1);
            }
        }
    }

    /**
     * 使用JDK HashSet的SetBuilderImpl版本，它内置了哈希泛洪保护。 SetBuilderImpl version that uses a JDK HashSet, which has built in
     * hash flooding protection.
     */
    private static final class JdkBackedSetBuilderImpl<E> extends SetBuilderImpl<E> {
        private final Set<Object> delegate;

        JdkBackedSetBuilderImpl(SetBuilderImpl<E> toCopy) {
            /**
             * initializes dedupedElements and distinct
             * 初始化，存储数据的数组信息！，然后初始化数组的大小！这个方便
             * JdkBackedImmutableSet 实现的时候，方便获取Set集合的数据的大小！Set的迭代器遍历
             */
            super(toCopy);
            delegate = Sets.newHashSetWithExpectedSize(distinct);
            for (int i = 0; i < distinct; i++) {
                delegate.add(dedupedElements[i]);
            }
        }

        @Override
        SetBuilderImpl<E> add(E e) {
            checkNotNull(e);
            if (delegate.add(e)) {
                //非重复的元素，然后进行添加到副本中保存哦！
                addDedupedElement(e);
            }
            return this;
        }

        @Override
        SetBuilderImpl<E> copy() {
            return new JdkBackedSetBuilderImpl<>(this);
        }

        @Override
        ImmutableSet<E> build() {
            switch (distinct) {
                case 0:
                    return of();
                case 1:
                    return of(dedupedElements[0]);
                default:
                    //这里就是真实的创建了一个ImmutableSet的一个子类的实现逻辑！
                    //间接的利用List迭代器的特性来遍历数据！将来个数据类型的测试仪器
                    return new JdkBackedImmutableSet<E>(
                        delegate, ImmutableList.asImmutableList(dedupedElements, distinct));
            }
        }
    }
}
