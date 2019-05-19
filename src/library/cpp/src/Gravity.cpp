#include <string>
#include <GeographicLib/GravityModel.hpp>

using namespace GeographicLib;
typedef Math::real real;

static GravityModel gravityModel("egm2008", "./gravity");

extern "C" void getGravitationalAcceleration(double longitude, double latitude, double altitude, double *gx, double *gy, double *gz){	
	gravityModel.Gravity(latitude, longitude, altitude, *gx, *gy, *gz);
}
