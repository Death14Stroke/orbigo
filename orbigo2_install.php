<?php

include('../phpqrcode/qrlib.php');

$iPod    = stripos($_SERVER['HTTP_USER_AGENT'],"iPod");
$iPhone  = stripos($_SERVER['HTTP_USER_AGENT'],"iPhone");
$iPad    = stripos($_SERVER['HTTP_USER_AGENT'],"iPad");
$Android = stripos($_SERVER['HTTP_USER_AGENT'],"Android");
$webOS   = stripos($_SERVER['HTTP_USER_AGENT'],"webOS");

$url = '';
if( $iPod || $iPhone ){
    $url = "youtube://watch?v=FwU24fGfwbA";
    header("Location:".$url);
}else if($iPad){
    $url = "youtube://watch?v=FwU24fGfwbA";
    header("Location:".$url);
}else if($Android){
    $url = "market://details?id=com.orbigo.orbigo";
    header("Location:".$url);
}else if($webOS){
    $url = "https://www.youtube.com/watch?v=FwU24fGfwbA";
    header("Location:".$url);
}
?>