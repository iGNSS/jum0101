from os.path import exists
import firebase_admin
from firebase_admin import credentials#파이어스토어 DB사용
from firebase_admin import firestore
from firebase_admin import db
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
src = "/home/minuk/minuk/expres/py/"#파일이 저장되는 리눅스 서버 주소


cred = credentials.Certificate('/home/minuk/minuk/testdb-6cfe6-firebase-adminsdk-34n91-1e1f636096.json')#파이어스토어
firebase_admin.initialize_app(cred)
db = firestore.client() 
dnowtime = datetime.datetime.now()
duploadtime = dnowtime.strftime('%Y-%m-%d %H시 %M분 %S초')
# 서버 소켓 오픈
#server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serverSock = socket.socket(AF_INET, SOCK_STREAM)


serverSock.bind(("",7210))

serverSock.listen(1)
 
file_recive_cnt = 0
count=0
print("TCPServer Waiting for client on port 7210")
  
while True:
 
    # 클라이언트 요청 대기중 .
    client_socket, address = serverSock.accept()
   
    # 연결 요청 성공
    print("I got a connection from ", address)
 
    data = None
 
    # Data 수신
    while True:
        path = "/home/minuk/minuk/expres"
        dir_list = os.listdir(path)
       
        img_data = client_socket.recv(104857600)#데이터를 수신
       
        data = img_data
        

        #print(data.decode('utf-8', 'ignore'))
       
        print("1")
        if img_data:
            while img_data:
               # print("recving Img...")
                img_data = client_socket.recv(104857600)
                data += img_data
        else:
             break
        #print(data)
        #print(data[:100])
        byteDivision=data.split()#수신한 데이터를 자르기
        
        file_name_extraction=byteDivision[0]
        fordel_name_extraction2=byteDivision[1]
        android_id=byteDivision[2]
        file_day=byteDivision[3]
        
        user_information=data.split(b',',20)
        age=user_information[3]
        gen_der=user_information[5]
        file_name_conversion= file_name_extraction.decode('utf-8', 'ignore')
        fordel_name_conversion= fordel_name_extraction2.decode('utf-8', 'ignore')
        android_id_conversion=android_id.decode('utf-8', 'ignore')
        file_day_conversion=file_day.decode('utf-8', 'ignore')
        age_conversion=age.decode('EUC_KR', 'ignore')
        gen_der_conversion=gen_der.decode('EUC_KR', 'ignore')
        print(age)
        print(age_conversion)
        name_len=len(file_name_conversion)
        name_len2=len(fordel_name_conversion)
        android_id_len=len(android_id_conversion)
        file_day_len=len(file_day_conversion)
    # 받은 데이터 저장
        file_data=data[len(file_name_extraction)+len(fordel_name_extraction2)+len(android_id)+len(file_day)+4:]
        #print(file_data.decode('utf-8', 'ignore'))
        data_fileDay = src +android_id_conversion+"/"+fordel_name_conversion+"/"+file_day_conversion#파일이 저장되는 리눅스 서버 주소
        #print(file_data)
        os.makedirs(data_fileDay,exist_ok=True)
        """if file_recive_cnt == 0:
            data_fileDay = src +"/a1"+".csv"
        elif file_recive_cnt == 1:
            data_fileDay = src+"/a2"+`'.mp4'"""
        data_fileDay =  data_fileDay + "/"+ file_name_conversion 
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
        #Firebase database 인증 및 앱 초기화
        if file_name_conversion[-3:]=='csv':#파일이 cdv 파일이면 파이어 스토어에 해당 파일 이름 저장
            if age_conversion == " ":
                if gen_der_conversion == " ":
                    doc_ref = db.collection(android_id_conversion).document(fordel_name_conversion)
                    doc_ref.set({
                        u'uploadtime':duploadtime
                    }, merge=True)
                else:     
                    doc_ref = db.collection(android_id_conversion).document(fordel_name_conversion)
                    doc_ref.set({
                        u'uploadtime':duploadtime,
                        u'gender':gen_der_conversion
                    }, merge=True)
            else:
                if gen_der_conversion == " ":
                    doc_ref = db.collection(android_id_conversion).document(fordel_name_conversion)
                    doc_ref.set({
                        u'uploadtime':duploadtime,
                         u'age':age_conversion + " 세",
                    }, merge=True)
                else:        
                    doc_ref = db.collection(android_id_conversion).document(fordel_name_conversion)
                    doc_ref.set({
                        u'uploadtime':duploadtime,
                        u'age':age_conversion + " 세",
                        u'gender':gen_der_conversion
                    }, merge=True)
            if fordel_name_conversion == "NineOne":
                doc_ref2 = doc_ref.collection(file_day_conversion).document(file_day_conversion)
                doc_ref2.set({
                    u'csvfilename':file_name_conversion,
                    u'uploadtime':duploadtime,
                    u'age':" ",
                    u'gender':" "
                }, merge=True)
            else:
                doc_ref2 = doc_ref.collection(file_day_conversion).document(file_day_conversion)
                doc_ref2.set({
                    u'csvfilename':file_name_conversion,
                    u'uploadtime':duploadtime,
                    u'age':age_conversion + " 세",
                    u'gender':gen_der_conversion
                }, merge=True)
            
        elif file_name_conversion[-3:]=='mp4':
            doc_ref = db.collection("data").document(android_id_conversion)
            doc_ref2 = doc_ref.collection(fordel_name_conversion).document(file_day_conversion)
       
            doc_ref2.set({
                u'mp4filename':file_name_conversion,
               
            }, merge=True)
        print(file_day_conversion)
        print("전송완료")
        #doc_ref = db.collection(android_id_conversion).document(fordel_name_conversion)
        #doc_ref2 = doc_ref.collection(file_day_conversion).document(file_name_conversion)
        #doc_ref2.set({
        #    u'filepath': data_fileDay,       
        #})
       
    print("SOCKET closed... END")
 
serverSock.close() 
print("SOCKET closed... END")
