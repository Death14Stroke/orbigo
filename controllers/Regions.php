<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

class Regions extends CI_controller{
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

    public function get_mobile_codes(){
        $keyspace = $this->config->item('key_space');
        $entity = $this->sys_model1->get_list($keyspace.'country_codes');
        $codes = array();
        foreach ($entity as $country) {
            array_push($codes,$country['mobile_code']);
        }
        $resp['status']='success';
        $resp['codes']=$codes;
        exit(json_encode($resp));
    }

    public function get_pois(){
        $keyspace = $this->config->item('key_space');
        $entity = $this->sys_model1->get_list($keyspace.'pois');
        $resp=[];
        foreach($entity as $a){
            $resp.array_push(json_encode($a));
        }
        exit(json_encode($entity));
    }

    public function check_pip(){
        ini_set('memory_limit', '512M');
        $keyspace = $this->config->item('key_space');
        $pois = array();
        $entity = $this->sys_model1->get_list($keyspace.'pois');
        foreach($entity as $dataobject_key=>$dataobject_value){
            $location = json_decode($dataobject_value['location'],true);
            $lng = $location['lng'];
            $lat = $location['lat'];
            $p = array($lng,$lat);
            $poi['id']=$dataobject_value['poi_id'];
            $poi['location']=$p;
            array_push($pois,$poi);
        }
        echo count($pois);
        $entity = $this->sys_model1->get_list($keyspace.'regions');
        $i = 0;
        foreach($entity as $dataobject_key=>$dataobject_value){
            $i++;
            $vertices_x = array();
            $vertices_y = array();
            $array1=json_decode($dataobject_value['coordinates']);
            foreach ($array1 as $array2) {
                foreach ($array2 as $array3) {
                    foreach ($array3 as $array4) {
                        $lat = doubleval($array4[1]);
                        $lng = doubleval($array4[0]);
                        array_push($vertices_x,$lng);
                        array_push($vertices_y,$lat);
                    }
                }
            }
            $points_polygon = count($vertices_x);
            foreach ($pois as $poi) {
                $latitude_y = $poi['location'][1];
                $longitude_x = $poi['location'][0];
                if($this->is_in_polygon($points_polygon, $vertices_x, $vertices_y, $longitude_x, $latitude_y)){
                    $entry['is_in_region']=$dataobject_value['region_name'];
                    $entry['is_in_state']=$dataobject_value['is_in_state'];
                    $entry['is_in_country']=$dataobject_value['is_in_country'];
                    $this->sys_model1->update($keyspace.'pois','poi_id',$poi['id'],$entry);
                }
            }
        }
        echo($i);
        exit('done');
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