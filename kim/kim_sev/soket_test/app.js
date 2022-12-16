const express = require('express');
const bodyParser = require('body-parser');
const bodyParser2 = bodyParser.json();
const app = express();
var multer = require('multer'); // express에 multer모듈 적용 (for 파일업로드)
const fs = require('fs');
var today; 
var datename;
const upload = multer({
  storage: multer.diskStorage({

    destination: function (req, file, cb) {
      fs.readdir('uploads/', (error) => {
        // uploads 폴더 없으면 생성
        if (error) {
          fs.mkdirSync('uploads/', { recursive: true });
          //fs.mkdirSync('routes/uploads/' + req.body.ID);
          sleep(2000)
          cb(null, 'uploads/');
        } else {
          cb(null, 'uploads/');
        }
      });
      console.log(req.body);
    },
    filename: function (req, file, cb) {
     today = new Date();
      var year = today.getFullYear(); // 년도
      var month = today.getMonth() + 1;  // 월
      var date = today.getDate();  // 날짜
      var day = today.getDay();
      var hours = today.getHours(); // 시
      var minutes = today.getMinutes();  // 분
      var seconds = today.getSeconds();  // 초
      var milliseconds = today.getMilliseconds();
      var dateString = year + '.' + month + '.' + date + "_" + hours + '_' + minutes + '_' + seconds + "_";
      datename = dateString + "cam.jpg";
      cb(null, dateString + "cam.jpg");
      //  cb(null, dateString + path.extname(file.originalname));
      console.log(file.originalname);
    }
  }),
});
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(bodyParser.raw());
app.use(bodyParser.text())
app.post('/upload2', bodyParser2, function(req, res) {
  //var Keywords = req.body.Keywords;
  console.log("Yoooooo");
  console.log(req.date);
  console.log(req.headers);
  console.log(req.body);
  res.status(200).send("yay");
});
app.post('/upload',  upload.single('imageFile'),  function(req, res) {
  //var Keywords = req.body.Keywords;

  console.log(req.file);
  res.status(200).send("yay");
});
app.listen(7230, () => console.log(`Started server at http://localhost:7230!`));
// // 설치한 express 모듈 불러오기 
// const express = require('express')

// // 설치한 socket.io 모듈 불러오기 
// const socket = require('socket.io')

// // Node.js 기본 내장 모듈 불러오기 
// const http = require('http')

// // Node.js 기본 내장 모듈 불러오기 
// const fs = require('fs')



// // express 객체 생성 
// const app = express()

// // express http 서버 생성 
// const server = http.createServer(app)

// // 생성된 서버를 socket.io에 바인딩 
// const io = socket(server)

// app.use('/css', express.static('./static/css'))
// app.use('/js', express.static('./static/js'))

// // Get 방식으로 / 경로에 접속하면 실행 됨 
// /*app.get('/', function(request, response) {
//   fs.readFile('./static/index.html', function(err, data) {
//     response.send("OK")
//     if(err) {
//       response.send('에러')
//     } else {
//       response.writeHead(200, {'Content-Type':'text/html'})
//       response.write(data)
//       response.end()
//     }
//   })
// })*/

// io.sockets.on('connection', function(socket) {

//   // 새로운 유저가 접속했을 경우 다른 소켓에게도 알려줌 
//   socket.on('newUser', function(name) {
//     console.log(name + ' 님이 접속하였습니다.')
    
//     // 소켓에 이름 저장해두기 
//     socket.name = name

//     // 모든 소켓에게 전송 
//     io.sockets.emit('update', {type: 'connect', name: 'SERVER', message: name + '님이 접속하였습니다.'})
//   })

//   // 전송한 메시지 받기 
//   socket.on('file', function(data) {
//     // 받은 데이터에 누가 보냈는지 이름을 추가 
//     data.name = socket.name
    
//     console.log(data)

//     // 보낸 사람을 제외한 나머지 유저에게 메시지 전송 
//     socket.broadcast.emit('update', data);
//   })

//   // 접속 종료 
//   socket.on('disconnect', function() {
//     console.log(socket.name + '님이 나가셨습니다.')

//     // 나가는 사람을 제외한 나머지 유저에게 메시지 전송 
//     socket.broadcast.emit('update', {type: 'disconnect', name: 'SERVER', message: socket.name + '님이 나가셨습니다.'});
//   })
// })

// // 서버를 8080 포트로 listen 
// server.listen(7230, function() {
//   console.log('서버 실행 중..')
// })