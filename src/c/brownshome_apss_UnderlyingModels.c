#include <jni.h>
#include <stdio.h>
#include "GeomagnetismHeader.h"
#include "EGM9615.h"

MAGtype_Geoid Geoid;
MAGtype_Ellipsoid Ellip;
MAGtype_MagneticModel *MagneticModels[1], *TimedMagneticModel;
MAGtype_LegendreFunction *LegendreFunction;
MAGtype_SphericalHarmonicVariables *SphVariables;

JNIEXPORT jboolean JNICALL Java_brownshome_apss_UnderlyingModels_initializeWMMData(JNIEnv *env, jclass class) {
	int epochs = 1;
	char filename[] = "WMM.COF";
	int nMax = 0;

	MAG_SetDefaults(&Ellip, &Geoid); /* Set default values and constants */

	if(!MAG_robustReadMagModels(filename, &MagneticModels, epochs)) {
		printf("\n WMM.COF not found.  Press enter to exit... \n ");
		return JNI_FALSE;
	}

	if(nMax < MagneticModels[0]->nMax) {
		nMax = MagneticModels[0]->nMax;
	}

	int NumTerms = ((nMax + 1) * (nMax + 2) / 2);
	TimedMagneticModel = MAG_AllocateModelMemory(NumTerms); /* For storing the time modified WMM Model parameters */
	LegendreFunction = MAG_AllocateLegendreFunctionMemory(NumTerms); /* For storing the ALF functions */
	SphVariables = MAG_AllocateSphVarMemory(MagneticModels[0]->nMax);

	if(MagneticModels[0] == NULL || TimedMagneticModel == NULL) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

JNIEXPORT jobject JNICALL Java_brownshome_apss_UnderlyingModels_getWMMData(
		JNIEnv *env, jclass thisClass, jdouble latitude,
		jdouble longitude, jdouble height, jdouble time) {

	MAGtype_CoordSpherical CoordSpherical;
	CoordSpherical.lambda = longitude;
	CoordSpherical.phig = latitude;
	CoordSpherical.r = height / 1000.0;

	//Time input
	MAGtype_Date date;
	date.DecimalYear = time;

	MAG_TimelyModifyMagneticModel(date, MagneticModels[0], TimedMagneticModel);//

	//Unrolled MAG_GeoMag()
	MAGtype_MagneticResults MagneticResultsSph;

	MAG_ComputeSphericalHarmonicVariables(Ellip, CoordSpherical, TimedMagneticModel->nMax, SphVariables); // Compute Spherical Harmonic variables
	MAG_AssociatedLegendreFunction(CoordSpherical, TimedMagneticModel->nMax, LegendreFunction); // Compute ALF
	MAG_Summation(LegendreFunction, TimedMagneticModel, *SphVariables, CoordSpherical, &MagneticResultsSph); // Accumulate the spherical harmonic coefficients

	jclass cls = (*env)->FindClass(env, "brownshome/apss/Vec3");
	jmethodID constructor = (*env)->GetMethodID(env, cls, "<init>", "(DDD)V");
	jobject object = (*env)->NewObject(env, cls, constructor, MagneticResultsSph.Bx,
			MagneticResultsSph.By, MagneticResultsSph.Bz);

	return object;
}
