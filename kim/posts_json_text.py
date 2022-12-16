from os.path import exists

from io import BufferedReader
from os.path import exists

import os
import time
import sys
import datetime
import json
#import detector
#import vvv
from flask import Flask, request, jsonify
from waitress import serve
from urllib import parse
##포스트로 사진받기
src = "/home/minuk/minuk/epython/py/"
# def array_page(aaa):
#     print("a")
#     file_data=aaa
#     print(type(file_data))
#     result = bytes(file_data, 'utf-8')
#     dnowtime = datetime.datetime.now()
#     duploadtime = dnowtime.strftime('%Y-%m-%d_%H_%M_%S')    
#     data_fileDay = src#파일이 저장되는 리눅스 서버 주소
#     os.makedirs(data_fileDay,exist_ok=True)
#     data_fileDay =  data_fileDay + "/"+ duploadtime +".jpg" 
#     data_file = open(data_fileDay, "wb")
 
#     print("finish img recv")
#     print(sys.getsizeof(result))
 
#     data_file.write(result)#리눅스서버에 저장 및 닫기
#     data_file.flush
#     data_file.close()
    # int_list = []
    # resulta = [aaa[i:i+2] for i in range(0, len(aaa), 2)]
    # print(resulta)
    # int_list = list(map(int, resulta))
    
    # print(int_list)
    # b = bytes(int_list)
    # print(b)


app = Flask(__name__)
if __name__== "__main__":
    app.run(host='222.103.110.36', port=7230)

    
@app.route('/', methods=['GET','POST'])
def root():
    print("welcome")
    return 'welcome to flask'

@app.route('/upload', methods=['GET','POST'])
def hello():
    print("0")
    if(request.method =='GET'):
        param = request.get_json()
        print(param)
        return jsonify(param)
       
    elif(request.method == 'POST'):
        print("1")
        params2 = request.form.get('qew', False)
        print(params2)
        params3 = request.data
        print(params3)
        params4 = request.files
        print(params4)
        print("=============0")
        params5 =request.args.get
        print(params5)
        print("=============1")
        params6 =request.get_data()
        print(params6)
        print("=============2")
        params8 =request.headers
        print(params8)
        print("=============3")
        params9=request.files.get("name")
        print(params9)
        print("=============4")
        params9=request.headers.get
        print(params9)
        print("=============5")
        print(dict(request.headers))
        print("=============6")
        dnowtime = datetime.datetime.now()
        duploadtime = dnowtime.strftime('%Y-%m-%d_%H_%M_%S')    
     
        
        
      # jsonString = json.dumps(params3)
       # print(jsonString)
        print("3")
        
        print(len(params3))
        #print(data)
        print(params3[:14])
        imagfile = params3[14:len(params3)-2]
        print(params3[14:len(params3)-2])
        print(type(imagfile))
        #result = bytes(imagfile, encoding = "utf-8")
        dnowtime = datetime.datetime.now()
        duploadtime = dnowtime.strftime('%Y-%m-%d_%H_%M_%S')    
        data_fileDay = src#파일이 저장되는 리눅스 서버 주소
        os.makedirs(data_fileDay,exist_ok=True)
        data_fileDay =  data_fileDay + "/"+ duploadtime +".jpg" 
        data_file = open(data_fileDay, "wb")
    
        print("finish img recv")
    
        data_file.write(imagfile)#리눅스서버에 저장 및 닫기
        data_file.flush
        data_file.close()
        #jsonObject = json.loads(params3)
        #jsonimage = jsonObject.get("imagefile")
        #print(jsonimage)
        #array_page(jsonimage)
        # result = params3.decode('utf-8', 'ignore')
        # print(result)
        # print(len(result))
        # print("2")
        # keys = []
        # values = []
        
        # data_list = result.replace('{', '').replace('}', '').split(",")
        # print(data_list)
        # for data in data_list:
        #     pair = data.split(":")
        #     keys.append(pair[0])
        #     values.append(pair[1])
        # print(keys)
        # print(values)  
        # my_dict = {}
        # my_dict = dict(zip(keys, values))
        # print(my_dict)
        # print(my_dict.get('imagefile'))
        # print(len(my_dict.get('imagefile')))
     
        # print("3")
        return params3
print("TCPServer Waiting for client on port 7230")




## csv 파일 저장경로
# src = "/home/minuk/minuk/expres/py/"#파일이 저장되는 리눅스 서버 주소


# dnowtime = datetime.datetime.now()
# duploadtime = dnowtime.strftime('%Y-%m-%d_%H_%M_%S')
# # 서버 소켓 오픈


# count=0
# print("TCPServer Waiting for client on port 7230")
  
# print("SOCKET closed... END")
