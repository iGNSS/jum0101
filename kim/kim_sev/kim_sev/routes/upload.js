var express = require('express');
const bodyParser = require('body-parser')
const path = require('path');
var router = express.Router();
const multer = require('multer');
//var upload = multer({dest: './uploads/'});
const fs = require('fs');
var today;
var datename;
const { Client } = require("pg");
const { compile } = require('ejs');
console.log("start");

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}
const upload = multer({
  storage: multer.diskStorage({

    destination: function (req, file, cb) {
      console.log(file.originalname);
      fs.readdir('routes/uploads/', (error) => {
        // uploads 폴더 없으면 생성
        if (error) {
          fs.mkdirSync('routes/uploads/', { recursive: true });
          //fs.mkdirSync('routes/uploads/' + req.body.ID);
          sleep(2000)
          cb(null, 'routes/uploads/');
        } else {
          cb(null, 'routes/uploads/');
        }
      });
      console.log(req.body);
    },
    filename: function (req, file, cb) {
      console.log(typeof(file));
     today = new Date();
      var year = today.getFullYear(); // 년도
      var month = today.getMonth() + 1;  // 월
      var date = today.getDate();  // 날짜
      var day = today.getDay();
      var hours = today.getHours(); // 시
      var minutes = today.getMinutes();  // 분
      var seconds = today.getSeconds();  // 초
      var milliseconds = today.getMilliseconds();
      var dateString = year + '.' + month + '.' + date + "_" + hours + '_' + minutes + '_' + seconds + "_" + "W" +"_";
      datename = dateString + "cam.jpg";
      cb(null, dateString + file.originalname);
      //  cb(null, dateString + path.extname(file.originalname));
     // console.log(file.originalname);
     
    }
  }),
});
router.get('/', function (req, res, next) {
  console.log("start1");
  res.render('upload', { title: 'Express' });
});

router.post('/', upload.single('imageFile'),  function(req, res) {
  //var Keywords = req.body.Keywords;

  console.log(req.file);
  res.status(200).send("WIFI OK");
});
//router.post('/', function(req, res, next) {
//  res.render('upload', { title: 'Express' });
//});

router.get('/', function (req, res, next) {

});

module.exports = router;