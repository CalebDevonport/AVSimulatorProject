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
package aim4.driver.aim;

import aim4.map.BasicAIMIntersectionMap;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;
import aim4.vehicle.aim.AIMAutoVehicleDriverModel;

/**
 * A proxy driver.
 */
public class ProxyDriver extends AIMAutoDriver {

  /**
   * Construct a proxy driver.
   *
   * @param vehicle the vehicle object
   * @param basicAIMIntersectionMap  the map object
   */
  public ProxyDriver(AIMAutoVehicleDriverModel vehicle, BasicAIMIntersectionMap basicAIMIntersectionMap) {
    super(vehicle, basicAIMIntersectionMap);
    // TODO Auto-generated constructor stub
  }

  /**
   * Take control actions for driving the agent's Vehicle.  This allows
   * both the Coordinator and the Pilot to act (in that order).
   */
  @Override
  public void act() {
  }

  /**
   * Get where this DriverAgent is coming from.
   *
   * @return the Road where this DriverAgent is coming from
   */
  @Override
  public AIMSpawnPoint getSpawnPoint() {
    return null;
  }

  /**
   * Get where this DriverAgent is going.
   *
   * @return the Road where this DriverAgent is going
   */
  @Override
  public Road getDestination() {
    return null;
  }

}
