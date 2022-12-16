import socket
import sys
##그냥 사진 전송
s = socket.socket()
s.connect(("222.103.110.36",7230))
f = open ("/home/minuk/minuk/epython/ppp.jpg", "rb")
l = f.read(1024)
while (l):
    s.send(l)
    l = f.read(1024)
s.close()