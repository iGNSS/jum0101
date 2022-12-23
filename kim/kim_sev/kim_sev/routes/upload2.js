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
router.use(bodyParser.json({limit: 5000000}));
router.use(bodyParser.urlencoded({limit: 5000000, extended: true, parameterLimit:50000}));

//router.use(bodyParser.json({ limit: 5000000 }));
//router.use(bodyParser.urlencoded({ limit: 5000000,extended: false, parameterLimit:50000 }));
//app.use(express.urlencoded({ extended: false }));
router.use(bodyParser.raw());
router.use(bodyParser.text());

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

router.get('/', function (req, res, next) {
  console.log("start1");
  res.render('upload2', { title: 'Express' });
});

router.post('/',  function (req, res) {
  //var Keywords = req.body.Keywords;
  var userAgent = req.header("imagefile");
  console.log(userAgent);
  var userAgent2 = req.get("imageFile");
  console.log(userAgent2);
  console.log("Yoooooo");
  console.log(req.date);
  console.log("====1")
  console.log(req.headers);//
  console.log("====2")
  console.log(req.params);
  console.log("====3")
  console.log(req.query);
  console.log("====4")
  console.log(req.file);//
  console.log("====5")
  const jbody = req.body;
  //console.log(jbody)
  console.log(typeof (jbody))
  console.log("====6")
  //print
  
  const jbodylen = jbody.length
  const jbodysub1 = jbody.substring(0,14)
  const jbodysub2 = jbody.substring(14,jbodylen-2)
  var bitmap = Buffer.from(jbodysub2, 'base64');
  //const bytes = stringToByteArray(jbodysub2);
  console.log(jbodysub2);
  console.log(bitmap);
  //console.log(Buffer.from(jbodysub2));
  let utf8Encode = new TextEncoder();

  var aaa = [];
  /*for (var i = 0; i < jbodysub.length; i++) {      
    aaa[i] = jbodysub.substring(i, i+1);   
    if(i==jbodysub.length-1){
      console.log("aaa");
    }                       
  }*/
  //console.log(aaa);
  // console.log(aaa.length);
  sleep(1000);
  //saveImage("aaa.jpg",aaa);

  /*var buf = new ArrayBuffer(this.length * 2);                   
  var bufView = new Uint8Array(buf);                            
  for (var i = 0, strLen = jbodysub.length; i < strLen; i++) {      
      bufView[i] = this.charCodeAt(i);                          
  }*/
  //console.log(jbodysub);
  //console.log(typeof(jbodysub));

  /*var jbodybu = Buffer.from(jbodysub, 'utf8');
 // console.log(jbodybu);
  //console.log(jbodybu);
  console.log(jbodybu.length);
  //console.log(jbody.substring(7,jbodylen-1));
  var binarys = Buffer.from(jbodysub,'binary')
  sleep(1000);
  console.log("=====2");
  console.log(binarys);
  console.log(binarys.length);
  const utf16leu = Buffer.from(jbodysub, 'utf16le'); 
  console.log(utf16leu.length);
  //console.log(jbodylen);
  //console.log(binarys);
  
  //jsonp = JSON.parse(req.body);
  //console.log(jsonp.imagefile);
  var myBuffer = Buffer.alloc(aaa.length*2);
  for (var i = 0; i < aaa.length; i++) {
      myBuffer.write(aaa[i]);
      
  }
  console.log("=====1");
  console.log(myBuffer.toString('utf-8'));
  fs.writeFile("uploads/"+"aaa.jpg", myBuffer, function(err) {
      if(err) {
          console.log(err);
      } else {
          console.log("The file was saved!");
      }
  });*/
  console.log("end");
  //res.send("yay2");
  today = new Date();
      var year = today.getFullYear(); // 년도
      var month = today.getMonth() + 1;  // 월
      var date = today.getDate();  // 날짜
      var day = today.getDay();
      var hours = today.getHours(); // 시
      var minutes = today.getMinutes();  // 분
      var seconds = today.getSeconds();  // 초
      var dateString = year + '_' + month + '_' + date + "_" + hours + '_' + minutes + '_' + seconds + "_";
      datename = "routes/uploads/"+dateString + "cam.jpg";

 // fs.writeFile(datename,  bytes);
 // var image = jbodysub;
  //var bitmap = Buffer.from(jbodysub, 'base64');
  

  fs.writeFileSync(datename, bitmap);
  res.render('upload');
});
//router.post('/', function(req, res, next) {
//  res.render('upload', { title: 'Express' });
//});

router.get('/', function (req, res, next) {

});

module.exports = router;