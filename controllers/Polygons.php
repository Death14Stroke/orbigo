<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');
    
class Polygons extends CI_controller{

    public $pointInterval = 30;
    public $speed = 18.64;
    public $distToDrive = 0;
    public $respPoints = array();
    public $latlng;
    public $searchPoints = array();

    function __construct() {

        parent:: __construct();

        $this->load->model('sys_model');
	    $this->load->model('sys_model1');
		$this->load->model('blockchain_model');
		
		$this->load->library('form_validation');
        $this->load->library('session');
        $user_name = $this->session->userdata('username');
        $this->app_codex_version = $this->session->userdata('codex_version');
        //$this->codex_version = 'orbigo.'.$this->app_codex_version;
        $this->codex_version = 'orbigo.first_kernel_kave5';
        $link = "http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";  
    }

    public function get_regions_in_polygon(){
        ini_set('memory_limit', '512M');
        $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('php://input'),true);
        $mode = $var['mode'];
        if(strcmp($mode,"distance")==0){
            $this->distToDrive = $var['distance'];
        }
        else if(strcmp($mode,'time')==0){
            $this->distToDrive = $var['time']/60*$this->speed;
        }
        $mytime=60;
        $this->distToDrive = $mytime/60*$this->speed;
        $this->latlng['lat']=-33.8082;
        $this->latlng['lng']=151.0835;
        //$this->latlng['lat']=$var['location']['lat'];
        //$this->latlng['lng']=$var['location']['lng'];
        $this->searchPoints = $this->getCirclePoints($this->latlng,floatval($this->distToDrive));
        $this->getDirections();
        $statesToCheck = array();
        $regions = array();
        foreach ($this->respPoints as $point) {
            $url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=".$point['lat'].','.$point['lng'].'&key=AIzaSyB3M1ZN8buGDnN6RkchvBrk8TNN8_FL6OA';
            $curl = curl_init($url);
            curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "GET");
            curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);  // Make it so the data coming back is put into a string
            // Send the request
            $result = json_decode(curl_exec($curl),true);
            $resp['statuscode']=$result['status'];
            if($result['status']=='OK'){
                $addComp = $result['results'][0]['address_components'];
                foreach ($addComp as $comp) {
                    $types = $comp['types'];
                    if(in_array('administrative_area_level_1',$types)){
                        if(!in_array($comp['long_name'],$statesToCheck)){
                            array_push($statesToCheck,$comp['long_name']);
                            break;
                        }
                    }
                }
            }
        }
        $resp['states']=$statesToCheck;
        foreach ($statesToCheck as $state) {
            $entity = $this->sys_model1->get_list_by_key($keyspace.'regions','is_in_state',$state);
            foreach ($entity as $region) {
                array_push($regions,$region);
            }
        }
        $resp['regionsdata']=count($regions);
        $resp['ini2']=$this->respPoints;
        $regionsToPlot = array();
        foreach ($regions as $region) {
            $resp['regionname']=$region['region_name'];
            $array1=json_decode($region['coordinates']);
            $vertices_x = array();
            $vertices_y = array();    
            $resp['ini1']=$this->respPoints;

            foreach ($array1 as $array2) {
                foreach ($array2 as $array3) {
                    foreach ($array3 as $array4) {
                        $latitude_y = doubleval($array4[1]);
                        $longitude_x = doubleval($array4[0]);
                        array_push($vertices_x,$longitude_x);
                        array_push($vertices_y,$latitude_y);   
                    }   
                }
            }
            $resp['ini']=$this->respPoints;
            $points_polygon = count($vertices_x);
            if($this->is_in_polygon($points_polygon, $vertices_x, $vertices_y, $this->latlng['lng'], $this->latlng['lat'])){
                $resp['loc']=$region['region_name'];
                $region['is_current']=true;
                if(!in_array($region,$regionsToPlot)){
                    array_push($regionsToPlot,$region);
                    $resp['pushed']=$region['region_name'];
                }
            }
            $resp['count']=count($this->respPoints);
            foreach ($this->respPoints as $value) {
                $resp['value']=$value;
                if($this->is_in_polygon($points_polygon, $vertices_x, $vertices_y, $value['lng'], $value['lat'])){
                    $resp['plotount']=count($regionsToPlot);
                    if(!in_array($region,$regionsToPlot)){
                        array_push($regionsToPlot,$region);
                    }
                    $key = array_search($value,$this->respPoints);
                    if($key!==false)
                        array_splice($this->respPoints,$key,1);
                }                
            }
          }
        $resp['status']='success';
        $resp['regions']=$regionsToPlot;
        exit(json_encode($resp));
    }

    function getCirclePoints($center,$radius){
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
            if($a % $this->pointInterval==0){
                array_push($searchPoints,$point);
            }
        }
        return $searchPoints;
    }

    function getDirections(){
        if(count($this->searchPoints)==0){
            return;
        }
        $to = array_shift($this->searchPoints);
        $url = "https://maps.googleapis.com/maps/api/directions/json?origin=".$this->latlng['lat'].','.$this->latlng['lng']."&destination=".$to['lat'].','.$to['lng']."&key=AIzaSyDwJgGn8K58gN-0jotJYvpENp8FvqxLr2I";
        $curl = curl_init($url);
        curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "GET");
        curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);  // Make it so the data coming back is put into a string
        // Send the request
        $result = json_decode(curl_exec($curl),true);
        $distReached = 0.0;
        if($result['status']=="OK"){
            $legs = $result['routes'][0]['legs'];
            foreach ($legs as $leg) {
                $steps = $leg['steps'];
            //    $trialArray = array();
                foreach ($steps as $step) {
                    $dist = floatval($step['distance']['value']*0.000621371);
                    $distReached+=$dist;
             //       array_push($trialArray,$step['location']);
                    if($distReached > $this->distToDrive){
              //          array_pop($trialArray);
                        array_push($this->respPoints,$step['end_location']);
                        break;
                    }
                }
            }
            $this->getDirections();
        }
        else{
//            echo 'Request failed. Status is '.$result['status'];
            $this->getDirections();
        }
    }

    function is_in_polygon($points_polygon, $vertices_x, $vertices_y, $longitude_x, $latitude_y){
        $i = $j = $c = 0;
        for ($i = 0, $j = $points_polygon-1 ; $i < $points_polygon; $j = $i++) {
            if ( (($vertices_y[$i] > $latitude_y != ($vertices_y[$j] > $latitude_y)) &&
                ($longitude_x < ($vertices_x[$j] - $vertices_x[$i]) * ($latitude_y - $vertices_y[$i]) / ($vertices_y[$j] - $vertices_y[$i]) + $vertices_x[$i]) ) ) 
                $c = !$c;
        }
        return $c;
    }
}
?>