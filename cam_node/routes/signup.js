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
      const sql = "INSERT INTO userList (id, name, nickname, email, password, favorite_type, favorite_country) VALUES($1, $2, $3, $4, $5, $6, $7) RETURNING *";
      const values = ['id', 'name', 'nickname', 'email', 'pw', 'favorite_type', 'favorite_country'];

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
