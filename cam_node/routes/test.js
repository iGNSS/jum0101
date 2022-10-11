var express = require('express');
var router = express.Router();
var static = require('serve-static');

const results =[];

var path = require("path");
router.use(static(path.join(__dirname, 'img')));
router.get('/', (req, res, next) => {
  res.send(`
    <h1>이제 좀 됬으면 좋겠다</h1>
    <img src="2022.10.6_15_36_17_cam.jpg" width="600">
  `)
});

//r//outer.get('/preview', async (req, res, next)=>{
///	const imgUrl = "http://10.10.10.162:7220/images/"
//	result = imgUrl+"저장된 이미지명" //imgUrl+"kitty.png"
//	res.send(result); 
//});

//});

module.exports = router;
