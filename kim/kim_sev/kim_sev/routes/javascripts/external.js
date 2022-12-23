var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');
const { Client } = require("pg");
var path = require("path");
const dbClient = new Client({
    user: "minuk",
    host: "gw.nineone.com",
    database: "picmonitoring",
    password: "minuk7210",
    port: 5432
});
dbClient.connect();
function iderrmessge() {
    document.getElementById('iderr').innerHTML = '문자열을 변경합니다.';
   var a = document.getElementById('id');
    
    console.log("111")
}

function fnChangeString() {
    console.log("111")
    document.getElementById('content').innerHTML = '문자열을 변경합니다.';
}

function fnChangeFont() {
    console.log("111")
    document.getElementById('content').style.fontSize = '35px';
}

function fnChangeDisplayNone() {
    document.getElementById('content').style.display = 'none';
}

function fnChangeDisplayBlock() {
    document.getElementById('content').style.display = 'block';
}

function fnChangeImageChar() {
    document.getElementById('imgFgoChar').src = 'janedarc.png';
}

function fnChangeImageCharAlter() {
    document.getElementById('imgFgoChar').src = 'janedarc_alter.png';
}