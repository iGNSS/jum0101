<?php
// Rui Santos
// Complete project details at https://RandomNerdTutorials.com/esp32-cam-post-image-photo-server/
// Code Based on this example: w3schools.com/php/php_file_upload.asp
//$target_dir = "/home/minuk/minuk/www/uploads/" . $_POST['ID'] . "/";
$target_dir = "/home/minuk/minuk/www/cam_php/uploads/adre/";
if (!is_dir($path)) {
    mkdir($path, 0777, true);
}
#$target_dir = "/home/minuk/minuk/www/uploads/" . $_POST['ID'] . "/";;
$config['upload_path'] = $target_dir;
if (!file_exists($target_dir)) {
  mkdir($target_dir, 0777, true);
}
#$target_dir = "/home/minuk/minuk/www/uploads/"+$_FILES["adress"];
$datum = mktime(date('H')+0, date('i'), date('s'), date('m'), date('d'), date('y'));
$target_file = $target_dir . date('Y.m.d_H_i_s_', $datum) . basename($_FILES["imageFile"]["name"]);
$uploadOk = 1;
$imageFileType = strtolower(pathinfo($target_file,PATHINFO_EXTENSION));
foreach($_POST as $key=>$value)
{
  echo "$key=$value";
}

echo "<br>\n";
echo "Hello world<br>\n";
print_r($target_file);
echo "<br>\n";
print_r($_POST);
print_r($_FILES);
print_r($check);
// Check if image file is a actual image or fake image
if(isset($_POST["submit"])) {

  $check = getimagesize($_FILES["imageFile"]["tmp_name"]);
  if($check !== false) {
    echo "File is an image - " . $check["mime"] . ".";
    $uploadOk = 1;
  }
  else {
    echo "File is not an image.";
    $uploadOk = 0;
  }
}

// Check if file already exists
if (file_exists($target_file)) {
  echo "Sorry, file already exists.";
  $uploadOk = 0;
}

// Check file size
if ($_FILES["imageFile"]["size"] > 500000) {
  echo "Sorry, your file is too large.";
  $uploadOk = 0;
}

// Allow certain file formats
if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg"
&& $imageFileType != "gif" ) {
  echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.";
  $uploadOk = 0;
}

// Check if $uploadOk is set to 0 by an error
if ($uploadOk == 0) {
  echo "Sorry, your file was not uploaded.";
// if everything is ok, try to upload file
}
else {
  if (move_uploaded_file($_FILES["imageFile"]["tmp_name"], $target_file)) {
    echo "The file ". basename( $_FILES["imageFile"]["name"]). " has been uploaded.";
    
  }
  else {
    echo "Sorry, there was an error uploading your file.";
  }
}
function array_value_count ()
{
   foreach($this->input->post() as $k => $v) {
    echo $v;
   }
}

   

    /********************* request-new (dialog) *********************/

?>
