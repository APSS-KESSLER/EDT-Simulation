#include <jni.h>
#include <stdio.h>
#include "brownshome_apss_UnderlyingModels.h"
#include "GeomagnetismHeader.h"
#include "EGM9615.h"

JNIEXPORT jdoubleArray JNICALL Java_brownshome_apss_UnderlyingModels_getWMMData(
		JNIEnv *env, jclass thisClass, jdouble latitude,
		jdouble longitude, jdouble height, jdouble time) {
	
	int epochs = 1;
	char filename[] = "WMM.COF";
    int NumTerms, nMax = 0;
	MAGtype_Geoid Geoid;
    MAGtype_Ellipsoid Ellip; //Why are these needed?
	
	MAG_SetDefaults(&Ellip, &Geoid); /* Set default values and constants */
	
	//From wmm_point.c
	MAGtype_MagneticModel * MagneticModels[1], *TimedMagneticModel;

	if(!MAG_robustReadMagModels(filename, &MagneticModels, epochs)) {
        printf("\n WMM.COF not found.  Press enter to exit... \n ");
		return NULL;
    }
	
    if(nMax < MagneticModels[0]->nMax) {
		nMax = MagneticModels[0]->nMax;
	}
	
    NumTerms = ((nMax + 1) * (nMax + 2) / 2);
    TimedMagneticModel = MAG_AllocateModelMemory(NumTerms); /* For storing the time modified WMM Model parameters */
    
	if(MagneticModels[0] == NULL || TimedMagneticModel == NULL) {
        MAG_Error(2);
    }
	
	MAGtype_CoordSpherical CoordSpherical;
	CoordSpherical.lambda = longitude;
	CoordSpherical.phig = latitude;
	CoordSpherical.r = height / 1000.0;
	
	//Time input
	MAGtype_Date date;
	date.DecimalYear = time;
	
	MAG_TimelyModifyMagneticModel(date, MagneticModels[0], TimedMagneticModel);
	
	//Unrolled MAG_GeoMag()
	MAGtype_LegendreFunction *LegendreFunction;
    MAGtype_SphericalHarmonicVariables *SphVariables;
    MAGtype_MagneticResults MagneticResultsSph, MagneticResultsGeo, MagneticResultsSphVar, MagneticResultsGeoVar;

    NumTerms = ((TimedMagneticModel->nMax + 1) * (TimedMagneticModel->nMax + 2) / 2); 
    LegendreFunction = MAG_AllocateLegendreFunctionMemory(NumTerms); /* For storing the ALF functions */
    SphVariables = MAG_AllocateSphVarMemory(TimedMagneticModel->nMax);
    MAG_ComputeSphericalHarmonicVariables(Ellip, CoordSpherical, TimedMagneticModel->nMax, SphVariables); /* Compute Spherical Harmonic variables  */
    MAG_AssociatedLegendreFunction(CoordSpherical, TimedMagneticModel->nMax, LegendreFunction); /* Compute ALF  */
    MAG_Summation(LegendreFunction, TimedMagneticModel, *SphVariables, CoordSpherical, &MagneticResultsSph); /* Accumulate the spherical harmonic coefficients*/
    MAG_SecVarSummation(LegendreFunction, TimedMagneticModel, *SphVariables, CoordSpherical, &MagneticResultsSphVar); /*Sum the Secular Variation Coefficients  */
    
	MAG_FreeLegendreMemory(LegendreFunction);
    MAG_FreeSphVarMemory(SphVariables);
	
	jdoubleArray jarray = (*env)->NewDoubleArray(env, 3);
	
	double *array = (*env)->GetDoubleArrayElements(env, jarray, NULL);
	array[0] = MagneticResultsSph.Bx;
	array[1] = MagneticResultsSph.By;
	array[2] = MagneticResultsSph.Bz;
	(*env)->ReleaseDoubleArrayElements(env, jarray, array, 0);//COMMIT CHANGES
	
	return jarray;
}
