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
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Spliterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 只有一个元素的不可变List！这种通过子类实现的！其实并没有对于外部进行暴露这个类的实现哦
 * Implementation of {@link ImmutableList} with exactly one element.
 *
 * @author Hayward Chan
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial")
final class SingletonImmutableList<E> extends ImmutableList<E> {

  final transient E element;

  SingletonImmutableList(E element) {
    this.element = checkNotNull(element);
  }

  @Override
  public E get(int index) {
    //检测index是否使用超标哦
    Preconditions.checkElementIndex(index, 1);
    return element;
  }

  @Override
  public UnmodifiableIterator<E> iterator() {
    //只有一个元素的迭代器
    return Iterators.singletonIterator(element);
  }

  @Override
  public Spliterator<E> spliterator() {
    return Collections.singleton(element).spliterator();
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public ImmutableList<E> subList(int fromIndex, int toIndex) {
    Preconditions.checkPositionIndexes(fromIndex, toIndex, 1);
    return (fromIndex == toIndex) ? ImmutableList.<E>of() : this;
  }

  @Override
  public String toString() {
    return '[' + element.toString() + ']';
  }

  @Override
  boolean isPartialView() {
    return false;
  }
}
