/*
 * Copyright (C) 2010 The Guava Authors
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

import java.util.ListIterator;

/**
 * 一个数据迭代器不支持添加删除等操作哦
 * A list iterator that does not support {@link #remove}, {@link #add}, or {@link #set}.
 *
 * @since 7.0
 * @author Louis Wasserman
 */
@GwtCompatible
public abstract class UnmodifiableListIterator<E> extends UnmodifiableIterator<E>
    implements ListIterator<E> {

  protected UnmodifiableListIterator() {}


  @Deprecated
  @Override
  public final void add(E e) {
    throw new UnsupportedOperationException();
  }


  @Deprecated
  @Override
  public final void set(E e) {
    throw new UnsupportedOperationException();
  }
}
