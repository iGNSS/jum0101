from os.path import exists

from io import BufferedReader
from os.path import exists

import os
import time
import sys
import datetime
#import detector
#import vvv
import requests

datas = {
'imagefile' : 'value1'
, 'key2' : 'value2'
}

url = "http://222.103.110.36:7230/upload"

response = requests.post(url, data=datas)