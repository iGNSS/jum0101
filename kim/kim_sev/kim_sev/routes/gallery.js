var express = require('express');
var router = express.Router();
const url = require('url');
const fs = require('fs');

router.get('/', function (req, res, next) {

  // const passedLists = req.query;

  var paths = [];
  var filename = [];
  const folder = 'routes/uploads/';

  fs.readdir(folder, function (error, filelist) {
    console.log(filelist);
    //paths=filelist;
  });
  fs.readdir(folder, (err, filelist) => { // 하나의 데이터씩 나누어 출
    filelist.forEach(file => {
      console.log('uploads/'+ file);
      paths.push('uploads/'+ file);
      filename.push(file);
    })
    res.render('gallery', { imgs: paths, layout: false, filename: filename});
  })
 // console.log(passedLists);




  // dbClient.end();



});

module.exports = router;
