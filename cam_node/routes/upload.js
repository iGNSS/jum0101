var express = require('express');
var router = express.Router();
const multer  = require('multer');
var upload = multer({dest: './uploads/'});
router.post('/', upload.single('file'), function(request, respond) {
  console.log('Error getting documents', request.query.born);
  if(request.file) console.log(request.file);
});
//router.post('/', function(req, res, next) {
//  res.render('upload', { title: 'Express' });
//});

module.exports = router;
