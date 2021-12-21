/*
 * Copyright (c) 2013-2021 Erik van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.grons.metrics4.scala

import java.util.concurrent.atomic.AtomicReference

import _root_.scala.annotation.tailrec

private[scala] object MoreImplicits {

  /**
    * Extends [[AtomicReference]] with `getAndTransform`.
    */
  private[scala] implicit class RichAtomicReference[A](val atomicReference: AtomicReference[A]) {
    /**
      * Invokes `transformation` with the current value and then sets the result as the new value.
      * When another concurrent change was detected, the operation is retried. The retry is repeated
      * as often as is necessary.
      *
      * @param transformation the transformation function
      * @return the old value
      */
    @tailrec
    final def getAndTransform(transformation: A => A): A = {
      val oldValue = atomicReference.get()
      val update = transformation(oldValue)

      if (!atomicReference.compareAndSet(oldValue, update))
        this.getAndTransform(transformation)
      else
        oldValue
    }
  }

}
