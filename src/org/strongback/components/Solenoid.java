/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback.components;

import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;

/**
 * A solenoid is a device that can be extended and retracted.
 *
 * @author Zach Anderson
 */
@ThreadSafe
public interface Solenoid extends Requirable {

    /**
     * The direction of the solenoid.
     */
    static enum Direction {
        /** The solenoid is extending. */
        EXTENDED,
        /** The solenoid is retracting. */
        RETRACTED,
        /** The solenoid is stopped. */
        STOPPED
    }

    /**
     * Get the current direction of this solenoid.
     *
     * @return the current direction; never null
     */
    Direction getDirection();

    /**
     * Extends this solenoid.
     * @return this object to allow chaining of methods; never null
     */
    Solenoid extend();

    /**
     * Retracts this solenoid.
     * @return this object to allow chaining of methods; never null
     */
    Solenoid retract();

    /**
     * Determine if this solenoid is extended.
     *
     * @return {@code true} if this solenoid is fully extended
     */
    boolean isExtended();

    /**
     * Determine if this solenoid is retracted.
     *
     * @return {@code true} if this solenoid is fully retracted
     */
    boolean isRetracted();

    /**
     * Determine if this solenoid is stopped.
     *
     * @return {@code true} if this solenoid is not moving, or false otherwise
     */
    boolean isStopped();
}