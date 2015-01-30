/*
*  1. 목적 : ESP8266 wifi 모듈을 이용한 디버깅 정보를 PC에서 출력한다.
 * 2. 작성자 : 최의신
 * 3. 작성일 : 2015년 1월 25일
 * 4. 연결 구성
 *    (Target board) -- (Wifi debug) -- (wifi AP) -- (PC Server)
 *
 */
#include <SoftwareSerial.h>
#include <stdlib.h>

#pragma GCC diagnostic ignored "-Wwrite-strings"

/*
 * WIFI
 */
const int RX_PIN = 2;
const int TX_PIN = 3;
const int RESET_PIN = 9;
const int BOARD_LED = 13;

#define WIFI_SERIAL      Serial
SoftwareSerial PROBE_SERIAL(RX_PIN, TX_PIN);  

/*
 *
 */
#define SSID     ""
#define PASS     ""
#define SERVER   "192.168.10.104"

#define PORT     "7777"

#define LEN_BUFFER 128

char * buffer;
int readIndex;

boolean flagWifi = false;
boolean flagServer = false;

void setup()
{
  PROBE_SERIAL.begin(19200);
  
  WIFI_SERIAL.begin(19200);
  WIFI_SERIAL.setTimeout(10000);
  
  pinMode(RESET_PIN, OUTPUT);
  pinMode(BOARD_LED, OUTPUT);

  buffer = (char *)malloc(LEN_BUFFER);  
  memset(buffer, 0, LEN_BUFFER);
  readIndex = 0;

  initWiFi();  
}

unsigned long digitTime = 0;
unsigned long time = 0;
unsigned long down = 0;

void loop()
{
  if ( flagWifi == false )
  {
    flagWifi = connectWiFi();
    if ( flagWifi == false )
       setErrorCode(1);
  }

  if ( flagWifi == true && flagServer == false )
  {
    if (connectToServer() == true )
    {
      flagServer = true;
      time = millis();
      setErrorCode(0);
    }
    else {
      setErrorCode(2);
    }
  }
  
  /*
   * 디버깅 시리얼에 데이터가 있는지 검사한다.
   */
  int avaLen = PROBE_SERIAL.available();
  if ( avaLen > 0 )
  {
    char ch;
    do
    {
      ch = PROBE_SERIAL.read();
      if ( ch == '\n' )
      {
        buffer[readIndex] = 0;
        readIndex = 0;
        
        String data = "";
        data.concat("@@log,0=");
        data.concat(buffer);
        data.concat("\r\n");
        
        sendData(0, data);
      }
      else 
      {
        if ( readIndex == LEN_BUFFER-1 )
        {
          buffer[readIndex] = 0;
          readIndex = 0;
          
          String data = "";
          data.concat("@@log,0=");
          data.concat(buffer);
          data.concat("\r\n");
          
          sendData(0, data);
        }
        if ( ch != '\r' )
        {
          if ( readIndex < LEN_BUFFER-1 )
           {
             buffer[readIndex++] = ch;
           }
        }
      }
    }while(--avaLen > 0);
  }
  
  if (WIFI_SERIAL.available())
  {
    String line = readLine(&WIFI_SERIAL);
     
    if ( line.indexOf("+IPD") >= 0 )
    {
      int sp = line.indexOf(":");
      if ( sp > 0 )
      {
        String data = line.substring(sp+1);
        data.replace("OK", "");
        
        /*
         * data 변수는 수신된 데이터가 저장된다.
         */
      }
    }
    else if ( line.indexOf("Unlink") >= 0 )
    {
      flagServer = false;
    }
    else if ( line.indexOf("ERROR") >= 0 )
    {
      flagServer = false;
      flagWifi = false;
    }
  }
}

/*
 * 상태를 표시한다.
 * 0 - 정상,  LED OFF
 * 1 - wifi AP에 연결이 되지 않은 상태, LED ON
 * 2 - PC의 서버에 연결이 되지 않은 상태, LED 30번 점멸됨.
 */
void setErrorCode(int code)
{
  if ( code == 0 )
  {
    digitalWrite(BOARD_LED, LOW);
  }
  else if ( code == 1 )
  {
    digitalWrite(BOARD_LED, HIGH);
  }
  else if ( code == 2 )
  {
    for(int i = 0; i < 30; i++)
    {
      digitalWrite(BOARD_LED, HIGH);
      delay(100);
      digitalWrite(BOARD_LED, LOW);
      delay(100);      
    }
  }
}

/*
 * WIFI로그를 출력하는 서버에 접속한다.
 */
boolean connectToServer()
{
  WIFI_SERIAL.println("AT+CIPMUX=1");
  WIFI_SERIAL.find("OK");
  delay(10);
  String cmd = "AT+CIPSTART=0,\"TCP\",\"";
  cmd.concat(SERVER);
  cmd.concat("\",");
  cmd.concat(PORT);
  
  WIFI_SERIAL.println(cmd);

  if(WIFI_SERIAL.find("Linked"))
    return true;
  else
    return false;
}

/*
 * 서버로 데이터를 전송한다.
 */
boolean sendData(const int ch, const String data)
{
  char packetLen[8];
  memset(packetLen, 0x00, 8);
  itoa(data.length(), packetLen, 10);
  
  WIFI_SERIAL.print("AT+CIPSEND=");
  WIFI_SERIAL.print(ch);
  WIFI_SERIAL.print(',');
  WIFI_SERIAL.println(packetLen);
  if(WIFI_SERIAL.find(">")){        // wait for > to send data.
    WIFI_SERIAL.print(data);
    WIFI_SERIAL.flush();
    delay(2);
    return true;
  }else{
    delay(2);
    return false;
  }
}


/*
 * ESP8266 모듈을 RESET 하고 'ready' 문자열을 기다린다.
 */
void initWiFi()
{
  digitalWrite(RESET_PIN, LOW);
  delay(3);
  digitalWrite(RESET_PIN, HIGH);

  do
  {
    if ( WIFI_SERIAL.available() > 0 )
    {
      String line = readLine(&WIFI_SERIAL);
      
      if ( line.indexOf("ready") >= 0 )
        break;
      else
        delay(1);
    }
    else
      delay(1);
  }while(1);

//  WIFI_SERIAL.println("AT+CSYSWDTDISABLE");
//  WIFI_SERIAL.find("OK");
  
  WIFI_SERIAL.println("AT+ATE0");
  WIFI_SERIAL.find("OK");
}

/*
 * '\n' 문자를 만나거나 지정된 시간동안 데이터를 읽어 들인다.
 */
String readLine(Stream * serial)
{
  String line = "";
  char ch;
  int cnt = 0;
  int len;
  do
  {
    if ((len = serial->available()) > 0)
    {
      for(int ix = 0; ix < len; ix++)
      {
        ch = serial->read();
        
        if ( ch == '\n' )
        {
          break;
        }
        else {
          if ( ch != '\r' )
            line.concat(ch);
        }
      }
    }
    else {
      delay(1);
      cnt++;
    }
  }while(cnt < 300);
 
  return line;
}

/*
 * AP 연결을 수행한다.
 */
boolean connectWiFi()
{
  WIFI_SERIAL.println("AT+CWMODE=1");
  WIFI_SERIAL.find("OK");
  delay(10);

  String cmd = "AT+CWJAP=\"";
  cmd.concat(SSID);
  cmd.concat("\",\"");
  cmd.concat(PASS);
  cmd.concat("\"");

  WIFI_SERIAL.println(cmd);
  
  unsigned long start = millis();
  boolean flagCon = false;
  
  for(int ix = 0; ix < 3; ix++)
  {
    do
    {
      String line = readLine(&WIFI_SERIAL);
      if ( line.indexOf("OK") >= 0 ) {
        flagCon = true;
        break;
      }
      else
        delay(1);
    }while(millis() - start < 6000);

    if ( flagCon == true )
      break;
  }
  
  if ( flagCon == true )
  {
    return true;
  }
  else {
    return false;
  }
}
