<?php
    $circlePoints = array();
    $drivePolyPoints = array();
    $distToDrive = 3.10686;
    $pointInterval = 30;
    $latlng["lat"]=35.149534;
    $latlng["lng"]=-90.04898;
    $searchPoints = getCirclePoints($latlng,$distToDrive);
    echo json_encode($searchPoints);
    $drivePolyPoints = array();
    getDirections();

    function getCirclePoints($center,$radius){
        global $pointInterval;
        $circlePoints = array();
        $searchPoints = array();
        $rLat = ($radius/3963.189) * (180/M_PI);
        $rLng = $rLat/cos($center['lat']*(M_PI/180));
        for($a=0;$a<361;$a++){
            $aRad = $a*(M_PI/180);
            $x = $center['lng'] + ($rLng*cos($aRad));
            $y = $center['lat'] + ($rLat*sin($aRad));
            $point['lat']=$y;
            $point['lng']=$x;
            array_push($circlePoints,$point);
            if($a % $pointInterval==0){
                array_push($searchPoints,$point);
            }
        }
        return $searchPoints;
    }

    function getDirections(){
        global $searchPoints;
        if(count($searchPoints)==0)
            return;
        $to = array_shift($searchPoints);
        global $latlng;
        $url = "https://maps.googleapis.com/maps/api/directions/json?origin=".$latlng['lat'].','.$latlng['lng']."&destination=".$to['lat'].','.$to['lng']."&key=AIzaSyDwJgGn8K58gN-0jotJYvpENp8FvqxLr2I";
        $curl = curl_init($url);
        curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "GET");
        curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);  // Make it so the data coming back is put into a string
        // Send the request
        $result = json_decode(curl_exec($curl),true);
        $polyline = '';
        if($result['status']=="OK"){
            $distance = intval($result['routes'][0]['legs'][0]['distance']['value']/1609);
            $duration = round(floatval($result['routes'][0]['legs'][0]['duration']['value']/3600),2);
            echo $distance.' '.$duration;
            //what next?
            $path = $result['routes'][0]['overview_polyline']['points'];
            $legs = $result['routes'][0]['legs'];
            foreach ($legs as $leg) {
                $steps = $leg['steps'];
                foreach ($steps as $step) {
                    $nextSegment = $step['polyline']['points'];
                    $polyline.$nextSegment;
                }
            }
            shortenAndShow($polyline);
            getDirections();
        }
        else{
            echo 'Request failed. Status is '.$result['status'];
            getDirections();
        }
    }

    function shortenAndShow($polyline){
        global $distToDrive;
        $distToDriveM = $distToDrive * 1609;
        $dist = 0;
        $cutoffIndex = 0;
        $copyPoints = array();
    }
?>