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
router.get('/', function (req, res, next) {
  console.log("start1");
  res.render('upload', { title: 'Express' });
});

router.post('/',  function (request, respond) {
  console.log('================1');
  console.log(request.body);

  console.log("=================2");
  console.log(request.date);

  if (request.file) {
    respond.send('OK');
    console.log("=================3");
    console.log(request.file);
    console.log("0");
    console.log(request.body.ID);

    if (request.file) {
      console.log("1");
      console.error('connection error')
    } else {
      console.log("2");
      // const sql = "INSERT INTO cam_device_id (device_id, group_id, device_place, device_time, device_type, standard_photoname) VALUES($1, $2, $3, $4, $5, $6) RETURNING *";
      // const values = ["nineone", 1, 'nineone', new Date(), 'raspberry', datename];
      console.log(request.dateString);

    }
    console.log("3");

  } else {
    respond.status(400).send('You need'); // BAD REQUEST 
  }
});
//router.post('/', function(req, res, next) {
//  res.render('upload', { title: 'Express' });
//});

router.get('/', function (req, res, next) {

});

module.exports = router;