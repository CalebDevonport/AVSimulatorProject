/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.map;

import aim4.im.aim.IntersectionManager;
import aim4.map.aim.AIMSpawnPoint;
import aim4.util.Registry;

import java.util.List;

/**
 * Essentially a structured grouping of AIM Roads and IntersectionManagers that
 * allows a unified interface so that we can re-use certain layouts and
 * create classes of layouts.
 */
public interface BasicAIMIntersectionMap extends BasicMap {
  /**
   * Get the Roads that exit this Layout.
   *
   * @return the Roads exit this Layout
   */
  List<Road> getDestinationRoads();

  /**
   * Get the intersection manager registry.
   *
   * @return the intersection manager registry.
   */
  Registry<IntersectionManager> getImRegistry();

  /**
   * Get the IntersectionManagers that are part of this Layout.
   *
   * @return the IntersectionManagers that are part of this Layout
   */
  List<IntersectionManager> getIntersectionManagers();

  /**
   * Set the intersection manager of a particular intersection.
   *
   * @param column  the column of the intersection
   * @param row     the row of the intersection
   * @param im      the intersection manager
   */
  void setManager(int column, int row, IntersectionManager im);

  /**
   * Get the list of spawn points.
   *
   * @return the list of spawn points
   */
  List<AIMSpawnPoint> getSpawnPoints();


}
