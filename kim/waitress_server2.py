from os.path import exists
##posts_json_text 실행파일
from waitress import serve
import posts_json_text
serve(posts_json_text.app, host='10.10.10.162', port=7230)