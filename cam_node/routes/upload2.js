var express = require('express');
const bodyParser = require('body-parser')
const path = require('path');
var router = express.Router();
const multer  = require('multer');
//var upload = multer({dest: './uploads/'});
const fs = require('fs'); 

var datename;
const { Client } = require("pg");
const dbClient = new Client({
    user: "minuk",
    host: "gw.nineone.com",
    database: "picmonitoring",
    password: "minuk7210",
    port: 5432
});

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}
const upload = multer({
  storage: multer.diskStorage({
    
    destination: function (req, file, cb) {
      fs.readdir('uploads/'+ req.body.ID, (error) => {
        // uploads 폴더 없으면 생성
        if (error) {

          fs.mkdirSync('uploads/' + req.body.ID);
          sleep(2000)
          cb(null, './uploads/'+ req.body.ID);
        }else{
          cb(null, './uploads/'+ req.body.ID);
        }
      });
    
      console.log(req.body);
    },
    filename: function (req, file, cb) {
      var today = new Date();   
      var year = today.getFullYear(); // 년도
      var month = today.getMonth() + 1;  // 월
      var date = today.getDate();  // 날짜
      var day = today.getDay(); 
      var hours = today.getHours(); // 시
      var minutes = today.getMinutes();  // 분
      var seconds = today.getSeconds();  // 초
      var milliseconds = today.getMilliseconds(); 
      var dateString = year + '.' + month  + '.' + date + "_" + hours + '_' + minutes  + '_' + seconds +"_";
      datename =  dateString + "cam.jpg";
      cb(null, dateString + "cam.jpg");
      //  cb(null, dateString + path.extname(file.originalname));
      console.log(file.originalname);
    }
  }),
});
router.post('/', upload.single('file'), function(request, respond) {
  if(request.file) 
  {
    respond.send('OK'); 
    console.log(request.file);
    dbClient.connect(err => {
      if (err) {
        console.error('connection error', err.stack)
      } else {
        console.log('success!')
        
        const sql = "INSERT INTO cam_device_id (device_id, group_name, device_place, device_time, device_type, standard_photoname) VALUES($1, $2, $3, $4, $5, $6) RETURNING *";
        const values = ["asd", 'nineone', 'nineone', new Date(), 'raspberry', datename];
        console.log(request.dateString);
        dbClient.query(sql, values, (err, res) => {
            if (err) {
                console.log(err.stack)
            } else {
                console.log(res.rows[0])
            }
        });
      }
    });
  }else {
    respond.status(400).send('You need'); // BAD REQUEST 
  }
});
//router.post('/', function(req, res, next) {
//  res.render('upload', { title: 'Express' });
//});

router.get('/', function(req, res, next) {
 
});

module.exports = router;