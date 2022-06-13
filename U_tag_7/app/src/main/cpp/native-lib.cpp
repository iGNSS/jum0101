#include <jni.h>
#include <string>
#include "Acc_lib.h"
//#include "U_Tag_07_08.h"
#include "U_Tag_all.h"
#include <android/log.h>"
#include <iostream>
#include <sstream>
extern "C" JNIEXPORT jstring JNICALL
Java_com_nineone_c_1c_MainActivity_stringFromJNI(JNIEnv* env,jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_nineone_c_1c_MainActivity_stringFromJN0(JNIEnv* env2,jobject, jstring accx, jstring accy,jstring accz) {
    Acc_lib ac = Acc_lib();
    std::string hello = ac.accString(env2->GetStringUTFChars(accx,0),
                                     env2->GetStringUTFChars(accy,0),
                                     env2->GetStringUTFChars(accz,0));
    return env2->NewStringUTF(hello.c_str());
}
/*extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_nineone_c_1c_MainActivity_floatArrary(JNIEnv* env3, jobject, jfloat accX, jfloat accY, jfloat accZ, jfloat gyrox, jfloat gyroy, jfloat gyroz) {
    U_Tag_07 pdrCodeOnly = U_Tag_07();
    jfloatArray result = env3->NewFloatArray(3);

    //__android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World1");
    float* data1 = pdrCodeOnly.NEWACCGYR(accX, accY, accZ, gyrox, gyroy, gyroz);
    float tmpArr[3] = { data1[0],data1[1],data1[2] };
    std::string helo="helo";

    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello World22 %f", accX);
   // __android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World2");
    env3->SetFloatArrayRegion(result, 0, 3, data1);
   // __android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World3");
    return result;
}*/
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_nineone_c_1c_MainActivity_Utag_1Arrary(JNIEnv *env3, jobject thiz, jfloat accX, jfloat accY,jfloat accZ, jfloat gyroX, jfloat gyroY,jfloat gyroZ) {
    U_Tag_all pdrCodeOnly = U_Tag_all();
    jfloatArray result = env3->NewFloatArray(3);

    //__android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World1");
    float* data1 = pdrCodeOnly.NEWACCGYR2(accX, accY, accZ, gyroX, gyroY, gyroZ);
    float tmpArr[3] = { data1[0],data1[1],data1[2] };
    std::string helo="helo";

    //__android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello World22 %f", accX);
    // __android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World2");
    env3->SetFloatArrayRegion(result, 0, 3, data1);
    // __android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World3");
    return result;
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_nineone_c_1c_background_1Service_Utag_1Arrary(JNIEnv *env3, jobject thiz, jfloat accX,jfloat accY, jfloat accZ, jfloat gyroX,jfloat gyroY, jfloat gyroZ) {
    U_Tag_all uTag0708 = U_Tag_all();
    jfloatArray result = env3->NewFloatArray(5);

    //__android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World1");
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello World21 %f %f %f", accX,accY,accZ);
    float* data1 = uTag0708.NEWACCGYR2(accX, accY, accZ, gyroX, gyroY, gyroZ);
    float tmpArr[3] = { data1[0],data1[1],data1[2] };
    std::string helo="helo";
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello World22 %f %f %f", data1[0],data1[1],data1[2]);
    //__android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello World22 %f", accX);
    // __android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World2");
    env3->SetFloatArrayRegion(result, 0, 5, data1);

    // __android_log_print(ANDROID_LOG_DEBUG, "CHK", "Hello World3");
    return result;
    // TODO: implement Utag_Arrary()
}