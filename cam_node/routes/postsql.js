var express = require('express');
var router = express.Router();

const { Client } = require("pg");
const dbClient = new Client({
    user: "minuk",
    host: "gw.nineone.com",
    database: "picmonitoring",
    password: "minuk7210",
    port: 5432
});
const fs = require('fs'); 

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}
const upload = multer({
  storage: multer.diskStorage({
  
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
      cb(null, dateString + "cam.jpg");
      //  cb(null, dateString + path.extname(file.originalname));
      console.log(file.originalname);
    }
  }),
});
router.get('/', function(req, res, next) {
  dbClient.connect(err => {
    if (err) {
      console.error('connection error', err.stack)
    } else {
      console.log('success!')
      const sql = "INSERT INTO userList (device_id, group_name, device_place, device_time, device_type, standard_photoname) VALUES($1, $2, $3, $4, $5, $6) RETURNING *";
      const values = ["asdad", 'nineone', 'nineone', req.dateString, 'raspberry', dateString + "cam.jpg"];
      console.log(request.dateString+","+dateString);
      dbClient.query(sql, values, (err, res) => {
          if (err) {
              console.log(err.stack)
          } else {
              console.log(res.rows[0])
          }
      });
    }
  });
});

module.exports = router;
