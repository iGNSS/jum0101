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

router.get('/', function(req, res, next) {
  dbClient.connect(err => {
    if (err) {
      console.error('connection error', err.stack)
    } else {
      console.log('success!')
    }
  });
});

module.exports = router;
