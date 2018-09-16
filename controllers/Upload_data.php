<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

class Upload_data extends CI_controller {
    
    function __construct() {
        parent:: __construct();
        $this->load->model('sys_model');
	    $this->load->model('sys_model1');
		$this->load->model('blockchain_model');
		$this->load->library('form_validation');
        $this->load->library('session');
        $user_name = $this->session->userdata('username');
        $this->app_codex_version = $this->session->userdata('codex_version');
        $this->codex_version = 'orbigo.first_kernel_kave5';
        $link = "http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
    } 

    public function insert_countries(){
        ini_set('memory_limit', '512M');
	    $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('../osmdata/countries/datalinks.json'),true);
        $objects = $var['data'];
        foreach ($objects as $filedata) {
            $osm = json_decode(file_get_contents($filedata['file']),true);
            $entry['country_code']=$osm['features'][0]['properties']['id'];
            $entry['country_name']=$osm['features'][0]['properties']['name'];
            $entry['country_icon']=$filedata['image'];
            $entry['country_flag']=$osm['features'][0]['properties']['alltags']['flag'];
            $entry['create_timestamp']=$osm['features'][0]['properties']['timestamp'];
            $curl = curl_init('https://maps.googleapis.com/maps/api/geocode/json?address='.$entry['country_name'].'&key=AIzaSyDu2jU3yJD-mzzTDE2KkDg0YTHzEOU1qkI');
            curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "GET");
            curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);  // Make it so the data coming back is put into a string
            // Send the request
            $result = json_decode(curl_exec($curl),true);
            $entry['bounds']=json_encode($result['results'][0]['geometry']['viewport']);
            $this->sys_model1->add($keyspace.'countries',$entry);   
        }
        $resp['status']='success';
        exit(json_encode($resp));   
    }

    public function insert_regions(){
        ini_set('memory_limit', '2048M');
        $keyspace = $this->config->item('key_space');
        $dir = '../osmdata/regions';
        if (is_dir($dir)){
            if ($dh = opendir($dir)){
                $i=1;
                $countrydata = $this->sys_model1->get_list($keyspace.'countries');
                $statedata = $this->sys_model1->get_list($keyspace.'states');
                while (($file = readdir($dh)) !== false){
                    if(strcmp($file,'.')!=0 && strcmp($file,'..')!=0){
                        $var = file_get_contents($dir. DIRECTORY_SEPARATOR .$file);
                        $js = json_decode($var,true);
                        $array = $js['features'];
                        foreach ($array as $jsond) {
                            $entry['region_id'] = $jsond["properties"]["id"];
                            $v=$jsond['properties']['name'];
                            $entry['region_name']=str_replace("'","''",$v);
                            $x=str_replace(' ','%20',$entry['region_name']);
                            $entry['coordinates'] = json_encode($jsond["geometry"]["coordinates"]);
                            $entry['create_timestamp'] = $jsond["properties"]["timestamp"];
                            $rpath = explode(',',$jsond['properties']['rpath']);
                            $statecode = $rpath[1];
                            $countrycode = $rpath[2];
                            foreach($countrydata as $dataobject_key=>$dataobject_value){
                                if($dataobject_value['country_code'] == $countrycode){
                                    $entry['is_in_country']=$dataobject_value['country_name'];
                                    break;
                                }
                            }
                            foreach($statedata as $dataobject_key=>$dataobject_value){
                                if($dataobject_value['state_code'] == $statecode){
                                    $entry['is_in_state']=$dataobject_value['state_name'];
                                    break;
                                }
                            }
                            $curl = curl_init('https://maps.googleapis.com/maps/api/geocode/json?address='.$x.'&key=AIzaSyDu2jU3yJD-mzzTDE2KkDg0YTHzEOU1qkI');
                            curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "GET");
                            curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
                            curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);  // Make it so the data coming back is put into a string
                            // Send the request
                            $result = json_decode(curl_exec($curl),true);
                            $entry['location']=json_encode($result['results'][0]['geometry']['location']);
                            $this->sys_model1->add($keyspace.'regions',$entry);
                        }    
                   }
                   $i++;
                }
            }
            closedir($dh);
            exit('success');
        }
    }

    public function insert_states(){
        ini_set('memory_limit', '512M');
        $keyspace = $this->config->item('key_space');
        $dir = '../osmdata/states';
        if (is_dir($dir)){
            if ($dh = opendir($dir)){
                $i=1;
                while (($file = readdir($dh)) !== false){
                    if(strcmp($file,'.')!=0 && strcmp($file,'..')!=0){
                        $var = file_get_contents($dir. DIRECTORY_SEPARATOR .$file);
                        $js = json_decode($var,true);
                        $array = $js['features'];
                        foreach ($array as $jsond) {
                            $entry['state_code'] = $jsond["properties"]["id"];
                            $v=$jsond['properties']['name'];
                            $entry['state_name']=str_replace("'","''",$v);
                            $x=str_replace(' ','%20',$entry['state_name']);
                            $entry['coordinates'] = json_encode($jsond["geometry"]["coordinates"]);
                            $entry['create_timestamp'] = $jsond["properties"]["timestamp"];
                            $rpath = explode(',',$jsond['properties']['rpath']);
                            $countrycode = $rpath[1];
                            $entity = $this->sys_model1->get_list_by_key($keyspace.'countries','country_code',$countrycode);
                            foreach($entity as $dataobject_key=>$dataobject_value){
                                if($dataobject_value['country_code'] == $countrycode){
                                    $entry['is_in_country']=$dataobject_value['country_name'];
                                    break;
                                }
                            }
                            $curl = curl_init('https://maps.googleapis.com/maps/api/geocode/json?address='.$x.'&key=AIzaSyDu2jU3yJD-mzzTDE2KkDg0YTHzEOU1qkI');
                            curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "GET");
                            curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
                            curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
                            $result = json_decode(curl_exec($curl),true);
                            $entry['location']=json_encode($result['results'][0]['geometry']['location']);
                            $this->sys_model1->add($keyspace.'states',$entry);
                        }    
                   }
                   $i++;
                }
            }
            closedir($dh);
            exit('success');
        }
    }
}
?>