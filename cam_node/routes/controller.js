module.exports.home = (req,res,next)=>{
  var paths = ['uploads/a.jpg','uploads/b.jpg','uploads/c.jpg'];              
  res.render('gallery', { imgs: paths, layout:false});
};