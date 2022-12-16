from os.path import exists

from io import BufferedReader
from socket import *
from os.path import exists

import socket#소켓통신 이용
import os
import time
import sys
import datetime
#import detector
#import vvv
 
# csv 파일 저장경로
src = "/home/minuk/minuk/epython/py/"#파일이 저장되는 리눅스 서버 주소


dnowtime = datetime.datetime.now()
duploadtime = dnowtime.strftime('%Y-%m-%d_%H_%M_%S')
# 서버 소켓 오픈
#server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serverSock = socket.socket(AF_INET, SOCK_STREAM)


serverSock.bind(("",7230))

serverSock.listen(1)
 
file_recive_cnt = 0
count=0
print("TCPServer Waiting for client on port 7230")
  
while True:
 
    # 클라이언트 요청 대기중 .
    client_socket, address = serverSock.accept()
   
    # 연결 요청 성공
    print("I got a connection from ", address)
 
    data = None
 
    # Data 수신
    while True:
        path = "/home/minuk/minuk/epython/py"
        dir_list = os.listdir(path)
       
        img_data = client_socket.recv(104857600)#데이터를 수신
       
        data = img_data
        

        #print(data.decode('utf-8', 'ignore'))
       
        print("1")
        if img_data:
            while img_data:
               # print("recving Img...")
                img_data = client_socket.recv(300000)
                data += img_data
        else:
             break
        print(sys.getsizeof(data))
        #print(data)
        print(data[:50])
        print(data[sys.getsizeof(data)-50:sys.getsizeof(data)])
        #byteDivision=data.split()#수신한 데이터를 자르기

     
        #user_information=data.split(b',',20)
        #file_name_conversion= byteDivision.decode('utf-8', 'ignore')
        #name_len=len(file_name_conversion)
    # 받은 데이터 저장
        file_data=data
        #print(file_data.decode('utf-8', 'ignore'))
        data_fileDay = src#파일이 저장되는 리눅스 서버 주소
        #print(file_data)
        os.makedirs(data_fileDay,exist_ok=True)
        """if file_recive_cnt == 0:
            data_fileDay = src +"/a1"+".csv"
        elif file_recive_cnt == 1:
            data_fileDay = src+"/a2"+`'.mp4'"""
        data_fileDay =  data_fileDay + "/"+ duploadtime +".jpg" 
        #print(data_fileDay)    
        data_file = open(data_fileDay, "wb")
 
        print("finish img recv")
        print(sys.getsizeof(file_data))
 
        data_file.write(file_data)#리눅스서버에 저장 및 닫기
        data_file.flush
        data_file.close()
       
        #client_socket.close()
 
        print("Finish ")
        file_recive_cnt+=1
       
       # print(file_day_conversion)
        print("전송완료")
        #doc_ref = db.collection(android_id_conversion).document(fordel_name_conversion)
        #doc_ref2 = doc_ref.collection(file_day_conversion).document(file_name_conversion)
        #doc_ref2.set({
        #    u'filepath': data_fileDay,       
        #})
       
    print("SOCKET closed... END")
 
serverSock.close() 
print("SOCKET closed... END")
