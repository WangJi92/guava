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
import com.google.common.primitives.Ints;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 用于实现基于散列的集合的静态方法。 MurmurHash3
 *
 * @author Kevin Bourrillion
 * @author Jesse Wilson
 * @author Austin Appleby
 */
@GwtCompatible
final class Hashing {
  private Hashing() {}

    /**
     * 3432918353
     * 这些应该是整数，但是我们需要使用long来迫使GWT以足够的精度进行乘法运算。
   */
  private static final long C1 = 0xcc9e2d51;

    /**
     * 461845907
     */
    private static final long C2 = 0x1b873593;

    /**
     * 这个方法是用Java中的Murmur哈希函数的中间步骤重写的
   * This method was rewritten in Java from an intermediate step of the Murmur hash function in
   * http://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp, which contained the
   * following header:
   *
   * MurmurHash3 was written by Austin Appleby, and is placed in the public domain. The author
   * hereby disclaims copyright to this source code.
     *
     * MurmurHash3由Austin Appleby编写，并被置于公共领域。作者特此声明不对此源代码进行版权保护。
   */
  static int smear(int hashCode) {
    return (int) (C2 * Integer.rotateLeft((int) (hashCode * C1), 15));
  }

  static int smearedHash(@Nullable Object o) {
    return smear((o == null) ? 0 : o.hashCode());
  }

    /**
     * 最大的Hash表的大小
     */
    private static final int MAX_TABLE_SIZE = Ints.MAX_POWER_OF_TWO;

    /**
     * 关闭表大小
     * @param expectedEntries  期待的大小
     * @param loadFactor  负载系数
     * @return
     */
    static int closedTableSize(int expectedEntries, double loadFactor) {
        // Get the recommended table size.
        // Round down to the nearest power of 2.

        //向下舍入到最接近2的幂。
        expectedEntries = Math.max(expectedEntries, 2);
        int tableSize = Integer.highestOneBit(expectedEntries);
        // Check to make sure that we will not exceed the maximum load factor.
        if (expectedEntries > (int) (loadFactor * tableSize)) {
            tableSize <<= 1;
            return (tableSize > 0) ? tableSize : MAX_TABLE_SIZE;
        }
        return tableSize;
    }

    /**
     * 是否需要扩展表的信息
     * @param size
     * @param tableSize
     * @param loadFactor
     * @return
     */
    static boolean needsResizing(int size, int tableSize, double loadFactor) {
        return size > loadFactor * tableSize && tableSize < MAX_TABLE_SIZE;
    }
}
