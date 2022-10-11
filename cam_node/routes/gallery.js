var express = require('express');
var router = express.Router();
//var controller = require('../controller');
var paths = ['uploads/a.jpg','uploads/b.jpg','uploads/c.jpg'];      
//var paths = 'uploads/a.jpg';   
//router.get('/', controller.home);
/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('gallery', { imgs: paths, layout:false});
});

//router.get('/gallery', function(req, res, next) {
//  res.render('gallery', { imgs: paths, layout:false});
//});

module.exports = router;
