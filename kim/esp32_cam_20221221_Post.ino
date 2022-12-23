#include "esp_camera.h"
#include <WiFi.h>
#include "esp_timer.h"
#include "img_converters.h"
#include "Arduino.h"
#include "fb_gfx.h"
#include "soc/soc.h"             // disable brownout problems
#include "soc/rtc_cntl_reg.h"    // disable brownout problems
#include "esp_http_server.h"
#include "EEPROM.h"
#include "base64.h"
#define WM_RX 12
#define WM_TX 14
#define WM_POWER 13

HardwareSerial wm300(2);

// Replace with your network credentials
const char* ssid = "ESP32CAM_2";
const char* password = "0538130799";
const char* sta_ssid = "WonderView";
const char* sta_password = "0538130799";
int send_mode = 0; // 0 : Wifi, 1 : CDMA
int send_time = 30;


// EEPROM Save 
String eep_ssid = "WonderView";
String eep_pw = "0538130799";
String eep_stime = "30";
String eep_name = "ESP32CAM_2";
String eep_mode = "0";
bool   eep_state = false;
/*
String serverName = "gw.nineone.com";   // REPLACE WITH YOUR Raspberry Pi IP ADDRESS
String serverPath = "/upload.php";     // The default serverPath should be upload.php
const int serverPort = 5000;
*/
String serverName = "222.103.110.36";   // REPLACE WITH YOUR Raspberry Pi IP ADDRESS
String serverPath = "/upload";     // The default serverPath should be upload.php
const int serverPort = 7217;
uint8_t *wm300_fbBuf;
int    retry_cnt = 0;

WiFiClient client;

#define PART_BOUNDARY "123456789000000000000987654321"

#define CAMERA_MODEL_AI_THINKER
#define BUTTON_PIN_BITMASK 0x200000000 // 2^33 in hex

#define uS_TO_S_FACTOR 1000000ULL  /* Conversion factor for micro seconds to seconds */
//#define TIME_TO_SLEEP  30        /* Time ESP32 will go to sleep (in seconds) */
RTC_DATA_ATTR bool bootstate = false;


#if defined(CAMERA_MODEL_AI_THINKER)
  #define PWDN_GPIO_NUM     32
  #define RESET_GPIO_NUM    -1
  #define XCLK_GPIO_NUM      0
  #define SIOD_GPIO_NUM     26
  #define SIOC_GPIO_NUM     27
  
  #define Y9_GPIO_NUM       35
  #define Y8_GPIO_NUM       34
  #define Y7_GPIO_NUM       39
  #define Y6_GPIO_NUM       36
  #define Y5_GPIO_NUM       21
  #define Y4_GPIO_NUM       19
  #define Y3_GPIO_NUM       18
  #define Y2_GPIO_NUM        5
  #define VSYNC_GPIO_NUM    25
  #define HREF_GPIO_NUM     23
  #define PCLK_GPIO_NUM     22
#else
  #error "Camera model not selected"
#endif

#define WM_RX 12
#define WM_TX 14

static const char* _STREAM_CONTENT_TYPE = "multipart/x-mixed-replace;boundary=" PART_BOUNDARY;
static const char* _STREAM_BOUNDARY = "\r\n--" PART_BOUNDARY "\r\n";
static const char* _STREAM_PART = "Content-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n";

httpd_handle_t camera_httpd = NULL;
httpd_handle_t stream_httpd = NULL;

static const char PROGMEM INDEX_HTML[] = R"rawliteral(
<html>
  <head>
    <title>ESP32-CAM</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
      body { font-family: Arial; text-align: center; margin:0px auto; padding-top: 30px;}
      table { margin-left: auto; margin-right: auto; }
      td { padding: 8 px; }
      .button {
        background-color: #2f4468;
        border: none;
        color: white;
        padding: 10px 20px;
        text-align: center;
        text-decoration: none;
        display: inline-block;
        font-size: 18px;
        margin: 6px 3px;
        cursor: pointer;
        -webkit-touch-callout: none;
        -webkit-user-select: none;
        -khtml-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
        -webkit-tap-highlight-color: rgba(0,0,0,0);
      }
      img {  width: auto ;
        max-width: 100% ;
        height: auto ; 
      }
    </style>  
  </head>
  <body>
    <h1>ESP32-CAM TEST</h1>
    <img src="" id="photo" ><br><br>
    Send Mode: <input type="radio" id = "WIFI" name="MODE" value = "0" checked/>Wi-Fi
    <input type="radio" id = "CDMA" name="MODE" value = "1"/>CDMA<br><br>
    WiFi_ID  : <input type="text" id = "WiFi_ID" name="WiFi_ID"><br><br>    
    WiFi_PW  : <input type="text" id = "WiFi_PW" name="WiFi_PW"><br><br>
    SendTime : <input type="text" id = "SendTime" name="SendTime"><br><br> 
    Name     : <input type="text" id = "Name" name="Name"><br><br>        
    <table>   
      <tr><td align="center"><button class="button"  ontouchstart="myfun('Load');" onmousedown="myfun('Load');">Load Value</button></td><td align="center"><button class="button"  ontouchstart="toggleCheckbox('Mode');" onmousedown="toggleCheckbox('Mode');">Send Image</button></td></tr>                   
    </table>
   <script>   
   function myfun(x) 
   {
     document.getElementById("WiFi_ID").value = "WonderView";     
     document.getElementById("WiFi_PW").value = "0538130799";
     document.getElementById("SendTime").value = "30";   
     document.getElementById("Name").value = "ESP32_2";       
   }  
   function toggleCheckbox(x) 
   {
     var xhr = new XMLHttpRequest();
     var id = document.getElementById("WiFi_ID").value;
     var pw = document.getElementById("WiFi_PW").value;
     var stime = document.getElementById("SendTime").value;   
     var fname = document.getElementById("Name").value; 
     var listVar = document.querySelector('input[name="MODE"]:checked').value;;
     xhr.open("GET", "/action?go=" + x + "&WiFi_ID=" + id + "&WiFi_PW=" + pw + "&SendTime=" + stime + "&Name=" + fname + "&Mode=" + listVar, true);
     xhr.send();
   }
   window.onload = document.getElementById("photo").src = window.location.href.slice(0, -1) + ":81/stream";
  </script>
  </body>
</html>
)rawliteral";

static esp_err_t index_handler(httpd_req_t *req){
  httpd_resp_set_type(req, "text/html");
  return httpd_resp_send(req, (const char *)INDEX_HTML, strlen(INDEX_HTML));
}

static esp_err_t stream_handler(httpd_req_t *req){
  camera_fb_t * fb = NULL;
  esp_err_t res = ESP_OK;
  size_t _jpg_buf_len = 0;
  uint8_t * _jpg_buf = NULL;
  char * part_buf[64];

  res = httpd_resp_set_type(req, _STREAM_CONTENT_TYPE);
  if(res != ESP_OK){
    return res;
  }

  while(true){
    fb = esp_camera_fb_get();
    if (!fb) {
      Serial.println("Camera capture failed");
      res = ESP_FAIL;
    } else {
      if(fb->width > 400){
        if(fb->format != PIXFORMAT_JPEG){
          bool jpeg_converted = frame2jpg(fb, 80, &_jpg_buf, &_jpg_buf_len);
          esp_camera_fb_return(fb);
          fb = NULL;
          if(!jpeg_converted){
            Serial.println("JPEG compression failed");
            res = ESP_FAIL;
          }
        } else {
          _jpg_buf_len = fb->len;
          _jpg_buf = fb->buf;
        }
      }
    }
    if(res == ESP_OK){
      size_t hlen = snprintf((char *)part_buf, 64, _STREAM_PART, _jpg_buf_len);
      res = httpd_resp_send_chunk(req, (const char *)part_buf, hlen);
    }
    if(res == ESP_OK){
      res = httpd_resp_send_chunk(req, (const char *)_jpg_buf, _jpg_buf_len);
    }
    if(res == ESP_OK){
      res = httpd_resp_send_chunk(req, _STREAM_BOUNDARY, strlen(_STREAM_BOUNDARY));
    }
    if(fb){
      esp_camera_fb_return(fb);
      fb = NULL;
      _jpg_buf = NULL;
    } else if(_jpg_buf){
      free(_jpg_buf);
      _jpg_buf = NULL;
    }
    if(res != ESP_OK){
      break;
    }
    //Serial.printf("MJPG: %uB\n",(uint32_t)(_jpg_buf_len));
  }
  return res;
}

static esp_err_t cmd_handler(httpd_req_t *req){
  char*  buf;
  size_t buf_len;
  char variable[200] = {0,};
  
  buf_len = httpd_req_get_url_query_len(req) + 1;  
  if (buf_len > 1) 
  {
    buf = (char*)malloc(buf_len);
    if(!buf)
    {
      httpd_resp_send_500(req);
      return ESP_FAIL;
    }
    if (httpd_req_get_url_query_str(req, buf, buf_len) == ESP_OK) 
    {
      if (httpd_query_key_value(buf, "go", variable, sizeof(variable)) == ESP_OK) 
      {
        Serial.println(buf);
        esp32cam_strtok(buf);
      } 
      else 
      {
        free(buf);
        httpd_resp_send_404(req);
        return ESP_FAIL;
      }
    } 
    else 
    {
      free(buf);
      httpd_resp_send_404(req);
      return ESP_FAIL;
    }
    free(buf);
  } 
  else 
  {
    httpd_resp_send_404(req);
    return ESP_FAIL;
  }

  sensor_t * s = esp_camera_sensor_get();
  int res = 0;  
  if(!strcmp(variable, "Capture")) 
  {
    Serial.println("Capture");
  }
  else if(!strcmp(variable, "Mode")) 
  {    
    Serial.println("Mode Change");   
    eeprom_read();   
    Serial.println(send_mode);
    if(send_mode == 0)
    {
      start_sta_mode();
    }
    else
    {
      digitalWrite(WM_POWER, HIGH);  
    }
  }
  else if(!strcmp(variable, "Load")) 
  {    
    Serial.println("Load");      
  }
  else 
  {
    res = -1;
  }

  if(res){
    return httpd_resp_send_500(req);
  }

  httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");
  return httpd_resp_send(req, NULL, 0);
}

void startCameraServer()
{
  httpd_config_t config = HTTPD_DEFAULT_CONFIG();
  config.server_port = 80;
  httpd_uri_t index_uri = 
  {
    .uri       = "/",
    .method    = HTTP_GET,
    .handler   = index_handler,
    .user_ctx  = NULL
  };

  httpd_uri_t cmd_uri = 
  {
    .uri       = "/action",
    .method    = HTTP_GET,
    .handler   = cmd_handler,
    .user_ctx  = NULL
  };
  httpd_uri_t stream_uri = 
  {
    .uri       = "/stream",
    .method    = HTTP_GET,
    .handler   = stream_handler,
    .user_ctx  = NULL
  };
  if (httpd_start(&camera_httpd, &config) == ESP_OK) 
  {
    httpd_register_uri_handler(camera_httpd, &index_uri);
    httpd_register_uri_handler(camera_httpd, &cmd_uri);
  }
  config.server_port += 1;
  config.ctrl_port += 1;
  if (httpd_start(&stream_httpd, &config) == ESP_OK) 
  {
    httpd_register_uri_handler(stream_httpd, &stream_uri);
  }
}

void print_wakeup_reason()
{
  esp_sleep_wakeup_cause_t wakeup_reason;

  wakeup_reason = esp_sleep_get_wakeup_cause();

  switch(wakeup_reason)
  {
    case ESP_SLEEP_WAKEUP_EXT0 : Serial.println("Wakeup caused by external signal using RTC_IO"); EEPROM.write(0, false);EEPROM.write(81, send_mode);EEPROM.commit(); break;
    case ESP_SLEEP_WAKEUP_EXT1 : Serial.println("Wakeup caused by external signal using RTC_CNTL"); break;
    case ESP_SLEEP_WAKEUP_TIMER : Serial.println("Wakeup caused by timer"); break;
    case ESP_SLEEP_WAKEUP_TOUCHPAD : Serial.println("Wakeup caused by touchpad"); break;
    case ESP_SLEEP_WAKEUP_ULP : Serial.println("Wakeup caused by ULP program"); break;
    default : Serial.printf("Wakeup was not caused by deep sleep: %d\n",wakeup_reason); break;
  }
}

void start_sta_mode()
{
  WiFi.mode(WIFI_STA);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(sta_ssid);
  WiFi.begin(sta_ssid, sta_password);  
  while (WiFi.status() != WL_CONNECTED) 
  {
   Serial.print(".");
   delay(500);
  }
  Serial.println();
  Serial.print("ESP32-CAM IP Address: ");
  Serial.println(WiFi.localIP());
  sendPhoto();  
  esp_sleep_enable_timer_wakeup(send_time * uS_TO_S_FACTOR);
  esp_sleep_enable_ext0_wakeup(GPIO_NUM_33,1); //1 = High, 0 = Low
  Serial.println("Setup ESP32 to sleep for every " + String(send_time) +  " Seconds");
  Serial.println("Going to sleep now");
  Serial.flush(); 
  esp_deep_sleep_start();
}

void start_ap_mode()
{
  WiFi.mode(WIFI_AP); 
  WiFi.softAP(ssid, password);   //launch the access point
  Serial.println("Wait 100 ms for AP_START...");
  delay(100);
  Serial.println("Setting the AP");
  IPAddress Ip(10, 0, 0, 91);    //setto IP Access Point same as gateway
  IPAddress NMask(255, 255, 255, 0);
  WiFi.softAPConfig(Ip, Ip, NMask);
  IPAddress IP = WiFi.softAPIP();
  Serial.print("AP IP address : ");
  Serial.println(IP);  
  startCameraServer();
}

void esp32cam_strtok(char* str)
{
  char* temp = strtok(str, "&=");
  int index = 0;
  EEPROM.write(0, true);
  while(temp != NULL)
  {
    index++;    
    if(index == 4)
    {
      Serial.println(temp);
      EEPROM.writeString(1, temp);
    }
    else if(index == 6)
    {
      Serial.println(temp);
      EEPROM.writeString(21, temp);
    }
    else if(index == 8)
    {
      Serial.println(temp);
      EEPROM.writeString(41, temp);
    }
    else if(index == 10)
    {
      Serial.println(temp);
      EEPROM.writeString(61, temp);
    }
    else if(index == 12)
    {
      Serial.println(temp);
      EEPROM.writeString(81, temp);
    }
    temp = strtok(NULL, "&=");
  }
  EEPROM.commit();
}

void eeprom_read()
{
  eep_state = EEPROM.read(0);
  bootstate = eep_state;
  Serial.println("Setup");
  
  eep_ssid = EEPROM.readString(1);
  Serial.println(eep_ssid);
  eep_ssid.trim();
  if(eep_ssid.equals("") == 1)
  {
    eep_ssid = "ESP32CAM_0";
  }
  else
  {
    sta_ssid = eep_ssid.c_str();
  }  
    
  eep_pw = EEPROM.readString(21);
  Serial.println(eep_pw);
  eep_pw.trim();
  sta_password = eep_pw.c_str();
  
  eep_stime = EEPROM.readString(41);
  Serial.println(eep_stime);
  eep_stime.trim();
  send_time = eep_stime.toInt();
  
  eep_name = EEPROM.readString(61);
  Serial.println(eep_name);
  eep_name.trim();
  ssid = eep_name.c_str();

  eep_mode = EEPROM.readString(81);
  Serial.println(eep_mode);
  eep_mode.trim();
  send_mode = eep_mode.toInt();
}

void setup() 
{
  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector
  
  Serial.begin(230400);
  wm300.begin(115200,SERIAL_8N1,WM_RX,WM_TX);
  EEPROM.begin(100);
  pinMode(GPIO_NUM_33, INPUT);
  pinMode(WM_POWER, OUTPUT);
  digitalWrite(WM_POWER, LOW); 
  print_wakeup_reason();   
  eeprom_read();  
    
  Serial.setDebugOutput(false);  
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG; 
  
  if(psramFound())
  {
    config.frame_size = FRAMESIZE_SXGA; // FRAMESIZE_ + QVGA|CIF|VGA|SVGA|XGA|SXGA|UXGA(1600*1200)
    config.jpeg_quality = 10; //10-63 lower number means higher quality
    config.fb_count = 2;
  } 
  else 
  {
    config.frame_size = FRAMESIZE_SXGA;
    config.jpeg_quality = 12;
    config.fb_count = 1;
  }
  // Camera init
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) 
  {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }  
  if(bootstate == false)
  {
    start_ap_mode(); 
  }
  else
  {
    if(send_mode == 0)
    {
      start_sta_mode();
    }
    else
    {
      digitalWrite(WM_POWER, HIGH);      
    }
  }  
}

void loop() 
{
  if (Serial.available()) 
  {
    String inString = Serial.readStringUntil('\r'); 
    if(inString.equals("1") == 1)
    {      
      start_cdma_mode();  
    }      
  }
  if (wm300.available()) 
  {
    wm300_receive();   
  }   
}

void wm300_receive()
{
  String inString = wm300.readStringUntil('\r');
  Serial.println(inString);
  if(inString.indexOf("OK") != -1)
  {
    
  }
  else if(inString.indexOf("+CME ERROR:") != -1)
  {    
    Serial.print("ERROR : ");
    Serial.println(inString);
    retry_cnt++;
    if(retry_cnt > 3)
    {
      esp_sleep_enable_timer_wakeup(send_time * uS_TO_S_FACTOR);
      esp_sleep_enable_ext0_wakeup(GPIO_NUM_33,1); //1 = High, 0 = Low
      Serial.println("Setup ESP32 to sleep for every " + String(send_time) +  " Seconds");
      Serial.println("Going to sleep now");
      Serial.flush(); 
      esp_deep_sleep_start();
    }
    else
    {
      start_cdma_mode();
    }    
  }
  else if(inString.indexOf("+QHTTPPOST:") != -1)
  {
    if(inString.indexOf("+QHTTPPOST: 0,200") != -1)
    {
      esp_sleep_enable_timer_wakeup(send_time * uS_TO_S_FACTOR);
      esp_sleep_enable_ext0_wakeup(GPIO_NUM_33,1); //1 = High, 0 = Low
      Serial.println("Setup ESP32 to sleep for every " + String(send_time) +  " Seconds");
      Serial.println("Going to sleep now");
      Serial.flush(); 
      esp_deep_sleep_start();
    }
    else
    {
      Serial.print("HTTP POST Error : ");
      Serial.println(inString);
      retry_cnt++;
      if(retry_cnt > 3)
      {
        esp_sleep_enable_timer_wakeup(send_time * uS_TO_S_FACTOR);
        esp_sleep_enable_ext0_wakeup(GPIO_NUM_33,1); //1 = High, 0 = Low
        Serial.println("Setup ESP32 to sleep for every " + String(send_time) +  " Seconds");
        Serial.println("Going to sleep now");
        Serial.flush(); 
        esp_deep_sleep_start();
      }
      else
      {
        start_cdma_mode();
      }    
    }
  }
  else if(inString.indexOf("PB") != -1)
  {
    retry_cnt = 0;
    start_cdma_mode();
  }
}

String sendPhoto() 
{
  String getAll = "";
  String getBody = "";
  
  camera_fb_t * fb = NULL;
  fb = esp_camera_fb_get();
  if(!fb) 
  {
    Serial.println("Camera capture failed");
    delay(1000);
    ESP.restart();
  }

  if (client.connect(serverName.c_str(), serverPort)) 
  {
    Serial.println("Connection successful!");    
    String head = "--RandomNerdTutorials\r\nContent-Disposition: form-data; name=\"imageFile\"; filename=\""+ eep_name +".jpg\"\r\nContent-Type: image/jpeg\r\n\r\n";
    String tail = "\r\n--RandomNerdTutorials--\r\n";

    uint32_t imageLen = fb->len;
    uint32_t extraLen = head.length() + tail.length();
    uint32_t totalLen = imageLen + extraLen;
  
    client.println("POST " + serverPath + " HTTP/1.1");
    client.println("Host: " + serverName);
    client.println("Content-Length: " + String(totalLen));
    client.println("Content-Type: multipart/form-data; boundary=RandomNerdTutorials");
    client.println();

    client.print(head);
    uint8_t *fbBuf = fb->buf;
    size_t fbLen = fb->len;
    Serial.print("Image Size : ");
    Serial.println(fbLen);   
    for (size_t n=0; n<fbLen; n=n+1024) 
    {
      if (n+1024 < fbLen) 
      {
        client.write(fbBuf, 1024);
        Serial.println(n+1024);
        fbBuf += 1024;
      }
      else if (fbLen%1024>0) 
      {
        size_t remainder = fbLen%1024;
        client.write(fbBuf, remainder);
        Serial.println(remainder);
      }
    }   
    client.print(tail);
    
    esp_camera_fb_return(fb);
    int timoutTimer = 10000;
    long startTimer = millis();
    boolean state = false;
    
    while ((startTimer + timoutTimer) > millis()) 
    {
      Serial.print(".");
      delay(100);      
      while (client.available()) 
      {
        char c = client.read();
        if (c == '\n') 
        {
          if (getAll.length()==0) 
          { 
            state=true; 
          }
          getAll = "";
        }
        else if (c != '\r') 
        { 
          getAll += String(c); 
        }
        if (state==true) 
        { 
          getBody += String(c); 
        }
        startTimer = millis();
      }
      if (getBody.length()>0) 
      { 
        break; 
      }
    }
    Serial.println();
    client.stop();
    Serial.println(getBody);
  }
  else 
  {
    getBody = "Connection to " + serverName +  " failed.";
    Serial.println(getBody);
  }
  return getBody;
}

void start_cdma_mode()
{
  wm300.write("AT+QHTTPCFG=\"contenttype\",1\r");
  delay(100);
  
  wm300.write("AT+QHTTPURL=34,80\r");
  delay(300);
  
  wm300.write("http://222.103.110.36:7217/upload2\r");
  delay(100); 

  wm300_image_save();  
}
void print_hex(uint8_t *s, size_t len) {
    for(int i = 0; i < len; i++) {
        printf("0x%02x, ", s[i]);
    }
    printf("\n");
}

void wm300_image_save()
{
  camera_fb_t * fb = NULL;
  fb = esp_camera_fb_get();
  if(!fb) 
  {
    Serial.println("Camera capture failed");
    delay(1000);
    ESP.restart();
  }       
  
  uint8_t *fbBuf = fb->buf;  
  int data_size = 512;   
  uint8_t wm300buf[data_size];
  char post_buf[30]; 
  char head_buf[50];
  
  //String str1 = "esp32_2,\"imagefile\":\"";
  String str1 = "{\"imagefile\":\"";  
  String str3 = "\"}";  
  size_t fbLen = fb->len;
 // size_t fbLen = fbLen2*2;
  size_t slen1 = str1.length();
  //size_t slen2 = str2.length();
  size_t slen3 = str3.length();
  Serial.print("Image Size : ");
  Serial.println(fbLen);  
   sprintf(head_buf, "{\"imagefile\":\"");
  //sprintf(head_buf, "%s,\"imagefile\":\"",ssid);
  //sprintf(head_buf, "{\"len\":\"%02d\",\"filename\":\"%s\",\"imagefile\":\"",strlen(ssid), ssid);
  Serial.println((char*)head_buf);
  String base64image = base64::encode(fb->buf, fb->len);
  sprintf(post_buf, "AT+QHTTPPOST=%d,80,80\r",strlen(head_buf)+base64image.length()+slen3);   
  wm300.write(post_buf);
  delay(300); 
  Serial.println(base64image.c_str());
  wm300.write(head_buf, strlen(head_buf));
  wm300.write(base64image.c_str(), base64image.length());  

 /* sprintf(post_buf, "AT+QHTTPPOST=%d,80,80\r",strlen(head_buf)+fbLen+slen3);   
  wm300.write(post_buf);
  delay(300); 
 wm300.write(head_buf, strlen(head_buf));  
  for (size_t n=0; n<fbLen; n=n+data_size) 
  {
    if (n+data_size < fbLen) 
    {
      wm300.write(fbBuf, data_size);
      Serial.println(n+data_size);
      fbBuf += data_size;
    }
    else if (fbLen%data_size>0) 
    {
      size_t remainder = fbLen%data_size;      
      wm300.write(fbBuf, remainder);
      Serial.println(remainder);      
    }
  }*/    
  
 /* sprintf(post_buf, "AT+QHTTPPOST=%d,80,80\r",strlen(head_buf)+base64image.length()+slen3);   
  wm300.write(post_buf);
  delay(300); 
  wm300.write(head_buf, strlen(head_buf));

  //wm300.write(base64image.c_str(), base64image.length());  */
  //wm300.write(str1.c_str(), slen1); 
//  for (size_t n=0; n<fbLen; n=n+data_size) 
//  {
//    if (n+data_size < fbLen) 
//    {
//      //uint8_t * testcode2= test_Code(fbBuf, data_size*2);
//      //Serial.println(testcode2);
//     /* char hex[data_size * 2 + 1];
//      for (int i = 0; i < data_size; i++) 
//      {
//        sprintf(hex + i * 2, "%02x", fbBuf[i]);
// 
//      }
//      Serial.println((char *)fbBuf);*/
//      //String hexString = String(hex);    
//     // Serial.println(hexString);
//     // wm300.write(hexString.c_str(), data_size*2);
//     wm300.write((uint8_t *)fb->buf, fb->len);
//     // Serial.println((char *)fb->buf);
//      fbBuf += data_size;
//    }
//    else if (fbLen%data_size>0) 
//    {
//      size_t remainder = fbLen%data_size;    
//    /*  char hex[remainder * 2 + 1];
//      for (int i = 0; i < remainder; i++) 
//      {
//        sprintf(hex + i * 2, "%02x", fbBuf[i]);
//      }
//      String hexString = String(hex);    
//      Serial.println(hexString);*/
//      //wm300.write(hexString.c_str(), remainder*2);  
//      wm300.write((unsigned char *)fb->buf, fb->len);   
//      Serial.println(remainder);      
//    }
//  }    
  wm300.write(str3.c_str(), slen3);  
  esp_camera_fb_return(fb);    
}
