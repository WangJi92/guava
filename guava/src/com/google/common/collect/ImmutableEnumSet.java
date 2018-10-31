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
import com.google.errorprone.annotations.concurrent.LazyInit;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 这个是一种特殊的Set，限定了泛型的类型而已！必须是枚举的哦！其他的感觉和父类没有什么不一样的！
 * Implementation of {@link ImmutableSet} backed by a non-empty {@link java.util.EnumSet}.
 *
 * @author Jared Levy
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial")
final class ImmutableEnumSet<E extends Enum<E>> extends ImmutableSet<E> {
    @SuppressWarnings("rawtypes")
  static ImmutableSet asImmutable(EnumSet set) {
    switch (set.size()) {
      case 0:
        return ImmutableSet.of();
      case 1:
        return ImmutableSet.of(Iterables.getOnlyElement(set));
      default:
          //直接通过代理去处理数据的不可变性的！
        return new ImmutableEnumSet(set);
    }
  }

  /*
   * Notes on EnumSet and <E extends Enum<E>>:
   *
   * 此类不是任意的ForwardingImmutableSet，因为我们需要知道在反序列化期间调用{@code clone（）}将返回一个其他人没有引用的对象，
   * 从而允许我们保证不变性。因此，我们仅支持{@link EnumSet}
   *
   * 这个就是一个数据的存储哦！通过复制了一个集合中的数据信息！保证数据的不可变性
   */
  private final transient EnumSet<E> delegate;

  private ImmutableEnumSet(EnumSet<E> delegate) {
    this.delegate = delegate;
  }

  @Override
  boolean isPartialView() {
    return false;
  }

  @Override
  public UnmodifiableIterator<E> iterator() {
      //直接使用JDK的迭代器也是处理掉！通过代理进行禁止掉！
    return Iterators.unmodifiableIterator(delegate.iterator());
  }

  @Override
  public Spliterator<E> spliterator() {
    return delegate.spliterator();
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    delegate.forEach(action);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean contains(Object object) {
    return delegate.contains(object);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    if (collection instanceof ImmutableEnumSet<?>) {
      collection = ((ImmutableEnumSet<?>) collection).delegate;
    }
    return delegate.containsAll(collection);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof ImmutableEnumSet) {
      object = ((ImmutableEnumSet<?>) object).delegate;
    }
    return delegate.equals(object);
  }

  @Override
  boolean isHashCodeFast() {
    return true;
  }

    /**
     * 当前集合数据的hashcode总和是多少！
     */
    @LazyInit
    private transient int hashCode;

  @Override
  public int hashCode() {
    int result = hashCode;
    return (result == 0) ? hashCode = delegate.hashCode() : result;
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

    /**
     * Returns an unmodifiable view of {@code iterator}
     * @return
     */
  @Override
  Object writeReplace() {
    return new EnumSerializedForm<E>(delegate);
  }

    /**
     * 此类用于序列化ImmutableEnumSet实例
   * This class is used to serialize ImmutableEnumSet instances.
   */
  private static class EnumSerializedForm<E extends Enum<E>> implements Serializable {
    final EnumSet<E> delegate;

    EnumSerializedForm(EnumSet<E> delegate) {
      this.delegate = delegate;
    }

    Object readResolve() {
        /**
         * 防御性地编写readObject（）方法
         */
      return new ImmutableEnumSet<E>(delegate.clone());
    }

    private static final long serialVersionUID = 0;
  }
}
