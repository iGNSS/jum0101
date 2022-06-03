//
// Created by user on 2022-06-03.
//

#include "U_Tag_07_08.h"
#include <android/log.h>
#include <jni.h>
#include <cstdlib>
#include <cstdint>
#include <cmath>
#include <cstring>
#include <cstdio>
#include <ctime>
#include <cmath>
//#include <Adafruit_TinyUSB.h> // for //Serial

#define FILENAME    "/N1_eeprom1.txt"

typedef unsigned char  byte;
int status;


/*************************** GPIO SETUP START ***************************************/
#define UART_TX           (0)   //P0.25
#define UART_RX           (1)   //P0.24
#define BARO_SDA          (22)  //P0.12
#define BARO_SCL          (23)  //P0.11
#define PIN_VBAT          (20)  //P0.29 A6 BATT CHECK

#define PWR_18            (8)   //P0.08 1.8V Power Enable
#define PWR_KEY           (38)   //P1.01 KEY1
#define WAKE_HIGH_PIN1    (46)  //P1.13 ICM-20948 INT1
#define LEFT_RESET        (10)  //Left BLE Reset

#define GPIO1             (24)  // LEFT GPIO 2
#define GPIO2             (25)  // LEFT GPIO 1

#define LED_G             (4)   //P0.15 LED_BLUE USB Download
#define LED_R             (12)  //P1.10 LED_GREEN BLE Active Blink
#define LED_B             (3)   //P0.08 LED_RED
/*************************** GPIO SETUP END ***************************************/

/************************ NRF 52840 Internal Memory ******************************/
char    char_eeprom_Buffer[64] = { 0 };
uint8_t unit8_eeprom_Buffer[64] = { 0 };
/************************ NRF 52840 Internal Memory ******************************/


/************************ User App Setting ****************************************/
byte          SystemVersion       = 0x01;
int           MANUFACTURER_ID     = 0x9191;   // eep0 : 나인원 제품_BLE
int           MAJOR               = 65278;    // eep1 : 비콘 메이저 값(고정)

byte          Startcode           = 0x02;     // eep 0    : 스타트 코드
byte          Update              = 0;        // eep 1    :
byte          ID                  = 7;        // eep 2    : 단말기 종류(0~255) ==> 201(BTU), 203(센서 추가)
int           MINOR               = 0;        // eep 3-4  : 단말기 ID (0~65,0000)
int8_t        TXPOWER             = -20;      // eep 5    : RF 파워
byte          S_ID                = 0;        // eep 6    : 센서 없음(0), O2(1), CO(2), H2S(3), Co2(4)
byte          GyroCal             = 0;        // eep 7    : 자이로 캘리브레이션(1)
byte          SON_Time            = 2;        // eep 8    : 센서 파워 온 시간
byte          SRE_Time            = 1;        // eep 9    : 센서 리딩 시간
byte          SOFF_Time           = 10;       // eep 10   : 센서 오프 시간
byte          Ms5607_ADC_Set      =  0;       // eep 11   : MS5607 ADC 세팅
byte          Endcode             = 0x03;     // eep 19   : 엔드 코드

byte          Baro_Sensor         = 1;
byte          Imu_Sensor          = 1;
int           eepdelaytime        = 10;

int           Beacon_Interval     = 40;       // eep8  40 = 25ms, 80 = 50ms, 160 = 100ms
int           ACC_Move_Range      = 80;      // eep9
unsigned long User_Check_Time     = 60000;    // 움직임 감지 시간
unsigned long Beacon_Change_Time  = 450;      // eep10 : 비콘 데이터 변경 주기(500ms ~ 5,000ms)
unsigned long Acc_Scan_Time       = 25;       // eep11 : 가속도 읽기 주기(50ms ~ 1,000ms), UART 전송 주기
unsigned long Sleep_Time          = 60000;    // eep12 : 슬립 주기(3,000ms ~ 60,0000ms)
unsigned long Adv_Timeout         = 0;        // eep13 :
unsigned long Serial_Time         = 30;       // UART MODE Data Tx 주기 ms
unsigned long BLE_Send_Time       = 3000;


float         gyroCount[3];
float         Gyr_Cal_x = 0;
float         Gyr_Cal_y = 0;
float         Gyr_Cal_z = 0;
float         meanGyro[3] = {0.000000,  0.000000, 0.000000};
uint8_t       Gyr_Cal_Buff[12];
byte          GyroCalMode = 0;
int           GyrLoopCount = 2400;         // 자이로 캘리브레이션 횟수 40 * 25ms =1초 ,400=10초, 1200=30초, 2400= 1분

/************************ Batt A/D Variable ************************************/
//float mv_per_lsb = 4920.0F / 1024.0F * 4.25F; // 10-bit ADC with 4.2V input range
float mv_per_lsb = 4920.0F/1024.0F; // 10-bit ADC with 4.2V input range
byte  battvolt = 0;
byte  Mean_battvolt = 0;
/************************ Batt A/D Variable End ************************************/

/********************** Barometer Variable ********************************/
float temp = 0;
double pressure=0;
double temperature=0;
double prepressure = 0;
uint16_t Mean_baro = 0;
uint16_t Pre_Mean_baro = 0;
int Mean_temp = 0;
int precount=0;
unsigned long Baro_Check_Count = 0;
unsigned long Baro_Check_Time  = 0;
/*********************** *******************************************************************/

/************************** State Check Variable ******************************************/
byte          SensorCheck = 0;
byte          accReadstate = 0;
float         currentTime = 0;
unsigned long currentTime_Long = 0;
unsigned long checkTime = 0;
unsigned long AcccheckTime = 0;
unsigned long sleepcheckTime = 0;
unsigned long serial1Time = 0;
unsigned long pdrTime = 0;
unsigned long User_State_Time      = 0;    // 움직임 감지 시간
unsigned long blesendTime = 0 ;


int N1=0, N2=0;               // Latitude =  3550(N1).0768(N2)
int E1=0, E2=0;               // Longitude = 12840(E1).8567(E2);
float Altitude1 = 0.0;
float Altitude2 = 0.0;
int pre_N1 = 0, pre_N2 = 0;
int pre_E1 = 0, pre_E2 = 0;
byte GpsState     = 0x00;  //  0x00 : 현재 GPS 데이터, 0x01 : 기존 GPS 데이터, 0x0x : GPS Normal Active , 0x1x : GLP Mode , 0x21 : GPS sleep
byte InOutState   = 0;     //
unsigned int pre_RN =0 , pre_RE = 0;
byte D_Count = 0;
/*********************** *******************************************************************/

/************************** Beacon Data Buffer *******************************/
uint8_t       Ble_TxBuffer[20];
uint8_t       Ble_RxBuffer[20];
byte          bleConnect=0;
byte          sendSize = 20;
int           blerxcount = 0;

uint8_t beaconUuid[16] =
        {
                0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        };
/************************** Beacon Data Buffer End *******************************/

//BLEBeacon beacon(beaconUuid, MAJOR, MINOR, ID);

/************************** State Check Variable End ***************************************/
uint8_t   Rx1_Buffer[100];    // STag data Receive
byte      Rx1count = 0;
byte      Userstate = 0x10;
byte      StepWidth = 68;  //  68 = 680 Cm
float     fax, fay, faz, fgx, fgy, fgz, fmx, fmy, fmz, fbaro, ftemp;
int16_t   iax, iay, iaz, igx, igy, igz, imx, imy, imz;
int       Av;
byte      MoveEvent = 0;
int       StepCounter = 0;
float     Direction = 0;              // float 형
int16_t   Direction_int = 0;          // int 형

/*********************** *******************************************************************/

/*********************** PDR Code ************************************************************/
const float pi =  3.14159265358979f;
const float deg2rad = pi / 180;
const float rad2deg = 1 / deg2rad;
const float step_length = 0.65;
const float ms2s = 0.001;
/*********************** *******************************************************************/

/*********************** PDR Setting ********************************************************/
const int pdr_data_size = 3; //pdr out data [step count, step length, heading(degree)]
static float pdr_data[pdr_data_size] = {0,};
static int idx = 0;
static float time_before = 0;
/*********************** *******************************************************************/

/*********************** Sensor data PreProcessing *****************************************/
const int avg_norm_acc_windowsize = 20;
static float pre_avg_norm_acc = 0;
static float smoothing_norm_buffer[3] = {0,};
const int avg_rollpitch_windowsize = 20; //////////////////////////////////////////////////////////////////////////////////변경
const int avg_gyro_windowsize = 20;  //////////////////////////////////////////////////////////////////////////////////추가
static float pre_roll = 0;
static float pre_pitch = 0;
static float pre_avg_nav_gyro_z = 0; ///////////////////////////////////////////////////////////////////////////////////////////////추가
static float heading_gyro = 0;
/*********************** *******************************************************************/

/*********************** Step detection ****************************************************/
const int step_data_length = 3;
const int step_return_data_size = step_data_length * 3 + 2;
const float acc_step_amp_threshold = 0.18;
const int acc_step_interval = 100;
static float acc_step_detection[step_data_length] = {0,};
static float last_peak[step_data_length] = {9999, -9999, 0};
static float last_valley[step_data_length] = {-9999, 7, 0};
static float acc_step_peak[step_data_length] = {0,};
static float acc_step_valley[step_data_length] = {0,};
static float pre_acc_step_peak[step_data_length] = {0,};
static float pre_acc_step_valley[step_data_length] = {0,};
static int updating = 1;
static int step_result = 0; //0 : none 1 : peak  2 : valleyl
static int acc_step_count = 0;
static int acc_step_count_before = 0;
/*********************** *******************************************************************/

/*********************** Normal Step detection *********************************************/
static int normal_check_count = 0;
static int normal_step_count = 0;
static int normal_step_count_before = 0;
/*********************** *******************************************************************/


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 시작
/******************** Walking Motion Gyro bias Calibration  *********************************/
static int gyro_cali_step_count = 0;
static float gyro_cali_bias = 0;
static float gyro_cali_bias_before = 0;
static jboolean gyro_cali_flag = false;
const int gyro_buffer_size = 40 * 6;
static int gyro_buffer_count = 0;
static float normal_step_gyro_z_buffer[gyro_buffer_size] = {0,};
/*********************** *******************************************************************/

/*********************** Straight Heading Flag detection ***********************************/
const int heading_flag_str_windowsize = 5;
static float str_step_heading_buffer[heading_flag_str_windowsize]= {0,};
static int str_heading_flag = 0;
/*********************** *******************************************************************/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 끝
void PDR_Gyro_Cal(float gyroX,float gyroY,float gyroZ);


void ms5607_check();

void icm20948_check(float accX, float accY, float accZ, float gyrox, float gyroy, float gyroz);

void pdrStart();

void BeaconMode();

void Uart_Mode();

void serial1Read_OK();

void bleSend();

float averaging(int idx, float acc, float acc1, const int windowsize);

float calRollUsingAcc(float pDouble[3]);
float calPitchUsingAcc(float pDouble[3]);

float calGyrozNavFrame(float roll, float pitch, float pDouble[3]);
float calGyroHeading(int idx, float time, float before, float d, float gyro);
void stepDataUpdata(float d);

jboolean
normalStepDetection(float pDouble[3], float pDouble1[3], float pDouble2[3], float pDouble3[3]);

void strDetection();

float calgyroBufferMean(int count);

int normalStepCheck(jboolean flag, int count);

void stepDetection();

float calPitchUsingAcc(float pDouble[3]);

float calGyrozNavFrame(float roll, float pitch, float pDouble[3]);

float calGyroHeading(int idx, float time, float before, float d, float gyro);

float calNorm(float pDouble[3], int i);
void eepromecheck();

struct timeval getTime;

struct timespec time1, time2;

clock_t start1, start2, end1, end2;
float res1, res2 ,res3;
long res4;
void setup()
{
//  while ( !//Serial ) delay(10);   // for nrf52840 with native usb
    for(int i=0; i<10; i++)
    {
      //  ms5607_check();
    }
    clock_gettime(CLOCK_REALTIME, &time1);
    start1 = clock();
    eepromecheck();
    memset(Rx1_Buffer, 0, sizeof(Rx1_Buffer));
   // User_State_Time = millis();//long
}
void loop(float accX, float accY, float accZ, float gyrox, float gyroy, float gyroz)
{
    end1 = clock();
    res1 = (float)(end1 - start1)/CLOCKS_PER_SEC;
    res2 = end1/10000;
    res3 = (float)(end1 - start1)/CLOCKS_MONO;
    res4 = end1/1000;
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","ellapsed %f %f %ld second\n",res1,res2,res4);
    unsigned long curr_ms = end1/1000;//millis();
    static unsigned long prev_interval_ms;
    static unsigned long prev_ms;

    currentTime = end1/1000;// millis();//float
    currentTime_Long = end1/1000;// millis();//long
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt1 %lu %lu", curr_ms,prev_interval_ms);
    //if ((curr_ms - prev_interval_ms) > 24 ) //25ms 단위로 호출
   //{
        prev_interval_ms = curr_ms;
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt2 %lu %lu", curr_ms,prev_interval_ms);
        //ms5607_check();
        icm20948_check( accX,  accY,  accZ,  gyrox,  gyroy,  gyroz);//가속도 자이로                      // IMU Sensor Data Read
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt3 %lu %lu", curr_ms,prev_interval_ms);
        pdrStart();
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt4 %lu %lu", curr_ms,prev_interval_ms);
        prev_ms = end1/1000;//millis();
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt5 %lu %lu", curr_ms,prev_interval_ms);
   // }
    if (bleConnect == 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt6 %lu %lu", curr_ms,prev_interval_ms);
      //  BeaconMode();
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldt7 %lu %lu", curr_ms,prev_interval_ms);
    }

}
void eepromecheck()
{
    if((unit8_eeprom_Buffer[1] == 2) && (unit8_eeprom_Buffer[2] == 1)&&(unit8_eeprom_Buffer[20] == 3) )
    {
        Startcode = unit8_eeprom_Buffer[1];
        ////Serial.printf("Startcode = %d",Startcode);

        Update = unit8_eeprom_Buffer[2];
        ////Serial.printf("Update = %d",Update);

        ID = unit8_eeprom_Buffer[3];
        ////Serial.printf("ID = %d",ID);

        int m_high = unit8_eeprom_Buffer[4];
        int m_low  = unit8_eeprom_Buffer[5];
        MINOR = (m_high*256) + m_low;
        ////Serial.printf("MINOR = %d",MINOR);

        TXPOWER = unit8_eeprom_Buffer[6];
        ////Serial.printf("TXPOWER = %d", TXPOWER);

        S_ID = unit8_eeprom_Buffer[7];
        ////Serial.printf("S_ID = %d",S_ID);

        GyroCal = unit8_eeprom_Buffer[8];
        ////Serial.printf("GyroCal = %d",GyroCal);

        Ms5607_ADC_Set = unit8_eeprom_Buffer[12];
        ////Serial.printf("Ms5607_ADC_Set = %d",Ms5607_ADC_Set);

        ////Serial.printf("SystemVersion = %d",SystemVersion);

        Endcode = unit8_eeprom_Buffer[20];
        ////Serial.printf("Endcode = %d",Endcode);

        if(GyroCal == 1)
        {
            memcpy(&Gyr_Cal_x,&unit8_eeprom_Buffer[21],4);
            memcpy(&Gyr_Cal_y,&unit8_eeprom_Buffer[25],4);
            memcpy(&Gyr_Cal_z,&unit8_eeprom_Buffer[29],4);
            meanGyro[0] = Gyr_Cal_x;
            meanGyro[1] = Gyr_Cal_y;
            meanGyro[2] = Gyr_Cal_z;
            //    //Serial.println("GyroCal Setting");
        }
        //  else //Serial.println("GyroCal Not Setting");
    }
    //  else //Serial.println("NRF52840 Parameter Not Setting");
}



void datamake_2();

void data_display();

void BeaconMode()
{
    if ((currentTime_Long - checkTime) > Beacon_Change_Time )//long
    {
        if (ID == 7)
        {
            checkTime = end1/1000;//millis();//long
            memset(Rx1_Buffer, 0, sizeof(Rx1_Buffer));
            //Serial1.println("OK");  // Left BLE RSSI CALL
            serial1Read_OK();       // Left BLE RX Read
            if((currentTime-User_State_Time) > User_Check_Time) Userstate =0x00;
            else Userstate = 0x10;
            Userstate = (Userstate & 0xF0) | (D_Count & 0x0F) ;
            GpsState = (GpsState & 0xF0) | (D_Count & 0x0F) ;
            data_display();
            ID = 8;
            SensorCheck = 0;
        }
        else if (ID == 8)
        {
            checkTime = end1/1000;//millis();
            datamake_2();
            ID = 7;
        }
        else ;
        Rx1count = 0;

    }
}

void data_display()
{
    float DIR = 0;
    currentTime = end1/1000;//millis();//float
    Baro_Check_Count++;
}


void serial1Read_OK()
{
    int i, idx;
    unsigned long serial_ms = end1/1000;//millis();
    while (Rx1count < 38) {
        currentTime = end1/1000;//millis();
        if ((currentTime - serial_ms) > 4000)
            break;
    }
    N1 = (Rx1_Buffer[18]*256) + Rx1_Buffer[17];
    N2 = (Rx1_Buffer[19]*256) + Rx1_Buffer[18];
    E1 = (Rx1_Buffer[21]*256) + Rx1_Buffer[20];
    E2 = (Rx1_Buffer[23]*256) + Rx1_Buffer[22];
    memcpy(&Altitude1,&Rx1_Buffer[24],4);        // GPS
    memcpy(&Altitude2,&Rx1_Buffer[28],4);        // GPS
    GpsState = Rx1_Buffer[32];
    InOutState = Rx1_Buffer[33];
    memcpy(&pre_RN,&Rx1_Buffer[34],2);        // GPS 상대 좌표
    memcpy(&pre_RE,&Rx1_Buffer[36],2);        // GPS 상대 좌표
    Rx1count = 0;
}


void ms5607_check()
{
 /*   double barotemp;
    prepressure = pressure;
    barometer.checkUpdates();
    if (barometer.isReady())
    {
        temperature = barometer.GetTemp(); // Returns temperature in C
        pressure = barometer.GetPres(); // Returns pressure in Pascals
        Mean_temp = int(temperature * 100);
        barotemp = ( (pressure+prepressure) / 2) - 80000;
        precount++;

        if(Baro_Check_Count >1)
        {
            Pre_Mean_baro = Mean_baro;
            Mean_baro = uint16_t(barotemp);
            Baro_Check_Count = 0;
            precount=0;
            Baro_Check_Time = millis();
        }
    }
    else ;*/
}

void icm20948_check(float accX, float accY, float accZ, float gyrox, float gyroy, float gyroz)
{
    float Pow_X, Pow_Y, Pow_Z;
    fax = accX;
    fay = accY;
    faz = accZ;
    fgx = gyrox;
    fgy = gyroy;
    fgz = gyroz;

    iax = int16_t(fax * 100);
    iay = int16_t(fay * 100);
    iaz = int16_t(faz * 100);
    igx = int16_t(fgx * 100);
    igy = int16_t(fgy * 100);
    igz = int16_t(fgz * 100);

    Pow_X = pow(fax, 2);
    Pow_Y = pow(fay, 2);
    Pow_Z = pow(faz, 2);
    Av = sqrt(Pow_X + Pow_Y + Pow_Z) * 100;     // Norm of Acceleration

    if ((Av < (980 - ACC_Move_Range)) || (Av > (980 + ACC_Move_Range)))
    {
        User_State_Time = end1/1000;//millis();
        MoveEvent = 1;
//    //Serial.printf(" Av = %d,  MoveEvent = %d" , Av , MoveEvent);
//    //Serial.println(" ");
    }
    else MoveEvent = 0;

}


void datamake_2()
{
    memset(beaconUuid, 0, sizeof(beaconUuid));
    memcpy(&beaconUuid[0], &Rx1_Buffer[1], 15);   // BTU : RSSI
    memcpy(&beaconUuid[15],&GpsState, 1);   // GPS State
    if(D_Count == 0x0f) D_Count = 0;
    else  D_Count++;
}


//sensordata input [time(ms), accx, accy, accz, gyrox, gyroy, gyroz], datasize : 7
//float* pdrStart(float sensordata[])
void pdrStart()
{
    //Data Preprocessing
//  float cur_time = sensordata[0]; //ms
    float cur_time = currentTime; //ms
    float acc[3] = {fax, fay, faz}; //accx accy accz
    float gyro[3] = {fgx - meanGyro[0], fgy - meanGyro[1], fgz - meanGyro[2]}; //gyro calibration, gyrox gyroy gyroz
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds1 %f %f %f", acc[0],acc[1],acc[2]);
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds2 %f %f %f", gyro[0],gyro[1],gyro[2]);
    float norm_acc = calNorm(acc, 3); //acc norm calculation

    float avg_norm_acc = averaging(idx, pre_avg_norm_acc, norm_acc, avg_norm_acc_windowsize);
    pre_avg_norm_acc = avg_norm_acc;

    //avg_norm_buffer update
    if (idx < 3)
    {
        smoothing_norm_buffer[idx] = avg_norm_acc;
    }
    else
    {
        smoothing_norm_buffer[0] = smoothing_norm_buffer[1];
        smoothing_norm_buffer[1] = smoothing_norm_buffer[2];
        smoothing_norm_buffer[2] = avg_norm_acc;
    }

    //Calculate Heading Using Acc
    //Roll Pitch Averaging windowsize : 10
    float roll = calRollUsingAcc(acc);
    float pitch = calPitchUsingAcc(acc);
    float avg_roll = averaging(idx, pre_roll, roll, avg_rollpitch_windowsize);
    float avg_pitch = averaging(idx, pre_pitch, pitch, avg_rollpitch_windowsize);

    pre_roll = avg_roll;
    pre_pitch = avg_pitch;

    float nav_gyro_z = calGyrozNavFrame(avg_roll, avg_pitch, gyro);
    //heading_gyro = calGyroHeading(idx, cur_time, time_before, nav_gyro_z, heading_gyro); //////////////////////////////////////////////////////////////////////////기존 코드 주석처리

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 시작
    float avg_nav_gyro_z = averaging(idx, pre_avg_nav_gyro_z, nav_gyro_z, avg_gyro_windowsize);
    pre_avg_nav_gyro_z = avg_nav_gyro_z;

    //gyro cali cali
    if (gyro_cali_flag)
    {
        heading_gyro = calGyroHeading(idx, cur_time, time_before, avg_nav_gyro_z - gyro_cali_bias, heading_gyro);
    }
    else
    {
        heading_gyro = calGyroHeading(idx, cur_time, time_before, avg_nav_gyro_z, heading_gyro);
    }

    if (gyro_cali_flag)
    {
        if (gyro_buffer_count < gyro_buffer_size)
        {
            normal_step_gyro_z_buffer[gyro_buffer_count] = avg_nav_gyro_z;
            gyro_buffer_count++;
            if (gyro_buffer_count == gyro_buffer_size)
            {
                normal_step_gyro_z_buffer[gyro_buffer_size] = {0,};
                gyro_buffer_count = 0;
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 끝

    float att[3] = {avg_roll, avg_pitch, heading_gyro};
    //Acc Step Detection
    if (idx >= avg_norm_acc_windowsize)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds3 %d %d ", idx,avg_norm_acc_windowsize);
        stepDetection();
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds4 %d ",step_result);
        if (step_result == 1)
        { //if peak

            //memcpy(acc_step_peak, acc_step_detection, sizeof(acc_step_detection));
            //0 : time  1 : value 2 : heading
            acc_step_count++; //acc step count up

        }
        else if (step_result == 2)
        {
            //memcpy(acc_step_valley, acc_step_detection, sizeof(acc_step_detection));
            //0 : time  1 : value 2 : heading
        }

    }//step detection end

    if (acc_step_count != acc_step_count_before)
    { //step count change
        if (acc_step_count > 1)
        {
            jboolean normal_check_flag = normalStepDetection(acc_step_peak, acc_step_valley, pre_acc_step_peak, pre_acc_step_valley);
            normal_check_count = normalStepCheck(normal_check_flag, normal_check_count);
            if (normal_check_count == 2)
            {
                normal_step_count++;
            }
        }
        if (acc_step_count <= 3)
        {
            normal_step_count++;
        }

        gyro_cali_flag = false;  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가

        if (normal_step_count != normal_step_count_before)
        {//Normal step count change
            pdr_data[0] = normal_step_count; //add step count   // int
            pdr_data[1] = step_length;       //add step length
            pdr_data[2] = acc_step_peak[2] * rad2deg;  //add heading(degree)

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 시작
            stepDataUpdata(pdr_data[2]); //Heading flag buffer Update
            strDetection();
            if (str_heading_flag == 1)
            {
                gyro_cali_flag = true; //cali flag ON
                gyro_cali_step_count++;
                if (gyro_cali_step_count == 4)
                {
                    if (gyro_buffer_count != 0)
                    {
                        float nav_gyro_bias = calgyroBufferMean(gyro_buffer_count);
                        gyro_cali_bias = (nav_gyro_bias + gyro_cali_bias_before) / 2;
                        gyro_cali_bias_before = gyro_cali_bias;
                        gyro_cali_step_count = 0;
                        gyro_buffer_count = 0;
                    }
                }
            }
            else
            {
                gyro_cali_flag = false; //cali flag OFF
                gyro_cali_step_count = 0;
                gyro_buffer_count = 0;
                normal_step_gyro_z_buffer[gyro_buffer_size] = {0,};
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 끝

            StepCounter   = normal_step_count;           // int
            Direction     = acc_step_peak[2] * rad2deg;  // float
            Direction_int = int16_t (Direction * 100);
            StepWidth     = byte (step_length * 100);    // byte

        }
        memcpy(pre_acc_step_peak, acc_step_peak, sizeof(acc_step_peak));
        memcpy(pre_acc_step_valley, acc_step_valley, sizeof(acc_step_peak));
    }
    idx++;
    acc_step_count_before = acc_step_count;
    time_before = cur_time;
    normal_step_count_before = normal_step_count;

//  return pdr_data;
}//pdrstart end


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//CalFunction
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//가속도 norm 을 구하기 위한 function
float calNorm(float data[], int buffer_size){
    float norm_value = 0;
    for(int i = 0; i < buffer_size; i ++){
        norm_value += data[i] * data[i];
    }
    float norm_data = sqrt(norm_value);
    return norm_data;
}


//buffer 평균을 구하기 위한 function
float meanFunc(float data[], int buffer_size){
    float data_sum = 0;
    for(int i = 0; i < buffer_size; i++){
        data_sum += data[i];
    }
    float mean_data = data_sum / buffer_size;
    return mean_data;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////변경 영역 시작
// 기존 varianc를 구하는 코드에서 std를 구하는 코드로 변경
//buffer 표준편차을 구하기 위한 function
float stdFunc(float data[], int buffer_size){
    float data_diff_sum = 0;
    float data_mean = meanFunc(data, buffer_size);

    for(int i = 0; i < buffer_size; i++){
        data_diff_sum += pow((data[i] - data_mean), 2);
    }
    float std_data = sqrt(data_diff_sum / (buffer_size - 1));
    return std_data;
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////변경 영역 끝


//data 부호확인을 위한 function
int sign(float data){
    if (data < 0){
        return -1;
    }
    else if(data > 0){
        return 1;
    }
    else{
        return 0;
    }
}

//Moving averaging function
float averaging(int idx_in, float pre_avg_data_input, float data, int windowsize){
    int idx = idx_in + 1; //idx 시작을 1로 만들어주기 위함

    if (idx >= windowsize){
        idx = 10000;
    }

    if (idx == 1){
        return data;
    }
    else if (idx < windowsize){
        float avg_data = pre_avg_data_input * (idx - 1) / idx + data / idx;
        return avg_data;
    }
    else{
        float avg_data = pre_avg_data_input * (windowsize - 1) / windowsize + data / windowsize;
        return avg_data;
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Heading Calculation
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
float calGyroHeading(int idx, float cur_time, float time_before, float nav_gyro_z, float heading){
    float heading_nav = 0;
    if (idx != 0){
        heading_nav = heading + nav_gyro_z * (cur_time - time_before) * ms2s;
    }else{
        heading_nav = nav_gyro_z * 0.25 * ms2s;
    }
    return heading_nav;
}

float calRollUsingAcc(float acc[]){
    float roll = atan(acc[0] / sqrt(acc[1] * acc[1] + acc[2] * acc[2]));
    return roll;
}

float calPitchUsingAcc(float acc[]){
    float pitch = pi / 2 - atan(acc[1] / sqrt(acc[0] * acc[0] + acc[2] * acc[2]));
    return pitch;
}

float calGyrozNavFrame(float roll, float pitch, float gyro[]){
    float nav_gyro_z = gyro[0] * sin(roll) + gyro[1] * cos(roll) * cos(pitch) - gyro[2] * cos(roll) * sin(pitch);
    return nav_gyro_z;
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step Detection
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

int localPVFinder(float data[]){
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds7 %f %f %f",data[0],data[1], data[2]);
    if ((data[1] > data[0]) && (data[1] >= data[2]))
    {
        return 1;
    }

    else if ((data[1] < data[0]) && (data[1] <= data[2]))
    {
        return 2;
    }

    else{
        return 0;
    }
}

jboolean peakUpdater(float local_peak_value, float last_peak_value)
{
    if (local_peak_value > last_peak_value){
        return true;
    }
    else{
        return false;
    }
}

jboolean valleyUpdater(float local_valley_value, float last_valley_value){
    if (local_valley_value < last_valley_value){
        return true;
    }
    else{
        return false;
    }
}

jboolean peakFinder(float last_peak[], float local_valley[], float time_before){
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss12 %f %f %f %f",last_peak[1],local_valley[1],(last_peak[1] - local_valley[1]),acc_step_amp_threshold);
    if ((last_peak[1] - local_valley[1]) > acc_step_amp_threshold){
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldsss13 %f %f %f %d",time_before,last_peak[0], (time_before - last_peak[0]), acc_step_interval );
        if ((time_before - last_peak[0]) > acc_step_interval){
            __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss14");
            return true;
        }
        else{
            __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss15");
            return false;
        }
    }
    else{
        return false;
    }
}

jboolean valleyFinder(float last_valley[], float local_peak[], float time_before){
    if ((local_peak[1] - last_valley[1]) > acc_step_amp_threshold)
    {
        if ((time_before - last_valley[0]) > acc_step_interval){
            return true;
        }
        else{
            return false;
        }
    }
    else{
        return false;
    }
}
/*Step Detection*/
void stepDetection()
{
    float local_peak[step_data_length]= {0,};
    float local_valley[step_data_length] = {0,};
    float step_peak[step_data_length]= {0,};
    float step_valley[step_data_length] = {0,};

    //find local peak valley
    jboolean local_peak_flag = false;
    jboolean local_valley_flag = false;
    step_result = 0; //step result 초기화

    int localpvfinder = localPVFinder(smoothing_norm_buffer); //0 : none  1 : peak  2 : valley
    __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds5 %d ",localpvfinder);
    if(localpvfinder == 1)
    { //find peak
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds6");
        local_peak[0] = time_before;
        local_peak[1] = smoothing_norm_buffer[1];
        local_peak[2] = heading_gyro;
        local_peak_flag = true;
    }
    else if(localpvfinder == 2)
    { //find valley
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worlds7");
        local_valley[0] = time_before;
        local_valley[1] = smoothing_norm_buffer[1];
        local_valley[2] = heading_gyro;
        local_valley_flag = true;
    }

    if (local_peak_flag)
    {
        //updata local peak
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss8");
        if (valleyFinder(last_valley, local_peak, time_before))
        {
            __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss9");
            //find valley
            memcpy(acc_step_detection, last_valley, sizeof(last_valley));
            step_result = 2;
            last_valley[0] = 9999;
            last_valley[1] = 9999;//9999;
            last_valley[2] = 0;
            updating = 1;
        }
        if (updating)
        {
            if(peakUpdater(local_peak[1], last_peak[1]))
            {
                memcpy(last_peak, local_peak, sizeof(local_peak));
            }
        }
    }

    if (local_valley_flag)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss10");
        //updata local peak
        if (peakFinder(last_peak, local_valley, time_before))
        {
            __android_log_print(ANDROID_LOG_DEBUG, "CHK","Hello Worldss11");
            //find valley
            memcpy(acc_step_detection, last_peak, sizeof(last_peak));
            step_result = 1;
            last_peak[0] = 9999;
            last_peak[1] = -9999;
            last_peak[2] = 0;
            updating = 0;
        }
        if (!updating)
        {
            if(valleyUpdater(local_valley[1], last_valley[1]))
            {
                memcpy(last_valley, local_valley, sizeof(local_valley));
            }
        }
    }
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Normal Step Detection
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
jboolean normalStepDetection(float step_peak[], float step_valley[], float pre_step_peak[], float pre_step_valley[]){
    float diff_peak_value = abs(step_peak[1] - pre_step_peak[1]);
    float diff_valley_value = abs(step_valley[1] - pre_step_valley[1]);
    float diff_peak_time_value = step_peak[0] - pre_step_peak[0];
    float diff_valley_time_value = step_valley[0] - pre_step_valley[0];

    jboolean normal_check = 0;
    if ((diff_peak_value < 0.6) && (diff_valley_value < 0.6) && (diff_peak_time_value < 1000) && (diff_valley_time_value < 1000)) /////////////////////////////////////////////////////////////////diff_peak(valley)_value 0.4 -> 0.6 변경
    {
        normal_check = 1;
        return normal_check;
    }
    return normal_check;
}

int normalStepCheck(jboolean normal_check_flag, int normal_check_count){
    if (normal_check_flag == 1)
    {
        normal_check_count++;
        if (normal_check_count > 2)
        {
            normal_check_count = 2;
        }
        return normal_check_count;
    }
    else
    {
        normal_check_count = 0;
        return normal_check_count;
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 시작
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Nav Gyro Z Bias Calibration
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

float calgyroBufferMean(int count_non_zero)
{
    float nav_gyro_z_sum = 0;
    float nav_gyro_z_mean = 0;
    for (int i=0; i < count_non_zero; i++)
    {
        nav_gyro_z_sum += normal_step_gyro_z_buffer[i];
    }

    nav_gyro_z_mean = nav_gyro_z_sum / count_non_zero;
    return nav_gyro_z_mean;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Straight Heading Flag Detection
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void stepDataUpdata(float step_heading)
{
    if (normal_step_count <= heading_flag_str_windowsize)
    {
        str_step_heading_buffer[normal_step_count - 1] = step_heading;
    }
    else
    {
        for(int i=0; i<heading_flag_str_windowsize - 1; i++)
        {
            str_step_heading_buffer[i] = str_step_heading_buffer[i+1];
        }
        str_step_heading_buffer[heading_flag_str_windowsize - 1] = step_heading;
    }
}

void strDetection()
{
    if (normal_step_count >= heading_flag_str_windowsize)
    {
        float heading_std = stdFunc(str_step_heading_buffer, heading_flag_str_windowsize);
        if (heading_std < 10)
        {
            str_heading_flag = 1;
        }
        else{
            str_heading_flag = 0;
        }
    }
    else{
        str_heading_flag = 0;
    }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////추가 영역 끝


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Gyro Calibration
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void ICM20948_readGyroData(float gyroX,float gyroY,float gyroZ)
{
    gyroCount[0] = gyroX;
    gyroCount[1] = gyroY;
    gyroCount[2] = gyroZ;
}

void PDR_Gyro_Cal(float gyroX,float gyroY,float gyroZ)

{
    float total_Gyr_X = 0;
    float total_Gyr_Y = 0;
    float total_Gyr_Z = 0;
    int   Cal_cnt     = 0;

    //Serial.println(" ******************** PDR_Gyro_Cal Start ************************** ");

    for(Cal_cnt = 0; Cal_cnt < GyrLoopCount; Cal_cnt++)
    {
        ICM20948_readGyroData(gyroX,gyroY,gyroZ);
        total_Gyr_X += gyroCount[0];//result.gx;
        total_Gyr_Y += gyroCount[1];//result.gy;
        total_Gyr_Z += gyroCount[2];//result.gz;
        //Serial.printf("Cal_cnt = %06d \tgyroCount[0] = %6f \tgyroCount[1] = %6f \tgyroCount[2] = %6f ", Cal_cnt, gyroCount[0], gyroCount[1], gyroCount[2]);
        //Serial.println(" ");
    }
    Gyr_Cal_x = (float)total_Gyr_X / (float)Cal_cnt;
    Gyr_Cal_y = (float)total_Gyr_Y / (float)Cal_cnt;   ////////////  total_Gyr_Y로 수정
    Gyr_Cal_z = (float)total_Gyr_Z / (float)Cal_cnt;   ////////////  total_Gyr_Z로 수정

    meanGyro[0] = Gyr_Cal_x;
    meanGyro[1] = Gyr_Cal_y;
    meanGyro[2] = Gyr_Cal_z;
    //Serial.println("");
    //Serial.println("");
    //Serial.printf("Cal_cnt = %06d \tGyr_Cal_x    = %6f \tGyr_Cal_y    = %6f \tGyr_Cal_z    = %6f ", Cal_cnt, Gyr_Cal_x, Gyr_Cal_y, Gyr_Cal_z);
    //Serial.println(" ");
    memcpy(&unit8_eeprom_Buffer[21],&Gyr_Cal_x,4);
    memcpy(&unit8_eeprom_Buffer[25],&Gyr_Cal_y,4);
    memcpy(&unit8_eeprom_Buffer[29],&Gyr_Cal_z,4);
    unit8_eeprom_Buffer[8] = 1;
}

jboolean asd=false;

float *
U_Tag_07_08::NEWACCGYR(float accX, float accY, float accZ, float gyrox, float gyroy, float gyroz) {
    if (!asd) {
        setup();
        asd=true;
    }
    loop( accX,  accY,  accZ,  gyrox,  gyroy,  gyroz);
    float * retrnfloat = pdr_data;
    return retrnfloat;
}