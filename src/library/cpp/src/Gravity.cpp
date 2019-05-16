/**
 * \file Gravity.cpp
 * \brief Command line utility for evaluating gravity fields
 *
 * Copyright (c) Charles Karney (2011-2017) <charles@karney.com> and licensed
 * under the MIT/X11 License.  For more information, see
 * https://geographiclib.sourceforge.io/
 *
 * See the <a href="Gravity.1.html">man page</a> for usage information.
 **********************************************************************/

#include <iostream>
#include <string>
#include <array>
#include <GeographicLib/GravityModel.hpp>

#if defined(_MSC_VER)
// Squelch warnings about constant conditional expressions and potentially
// uninitialized local variables
#  pragma warning (disable: 4127 4701)
#endif

using namespace GeographicLib;
using namespace std;
typedef Math::real real;

static GravityModel gravityModel("egm2008", "./gravity");

array<real, 3> getGravitationalAcceleration(real longitude, real latitude, real altitude){	
	real gx, gy, gz;

    gravityModel.Gravity(latitude, longitude, altitude, gx, gy, gz);

    array<real, 3> acceleration;
    acceleration[0] = gx;
    acceleration[1] = gy;
    acceleration[2] = gz;

    return acceleration;
}


int main(int argc, const char* const argv[]) {

	std::cout << "Running method..." << std::endl;
	array<real, 3> acceleration = getGravitationalAcceleration(0,0,3000);
	std::cout << acceleration[0] << std::endl;
	std::cout << acceleration[1] << std::endl;
	std::cout << acceleration[2] << std::endl;
	std::cout << "Method complete!";
}
