<?php

include('../phpqrcode/qrlib.php');
$codeContents = 'http://34.235.112.213/orbigo2/orbigo2_install.php';
QRcode::png($codeContents, 'code.png', QR_ECLEVEL_H,4); 
?>