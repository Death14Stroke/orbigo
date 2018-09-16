<?php
    class Location extends CI_controller{

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

        public function get_icon(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $country = $var['country_name'];
            $entity = $this->sys_model1->get_list($keyspace.'countries');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['country_name'],$country)==0){
                  $resp['status']='success';
                  $resp['image']=$dataobject_value['country_icon'];
	          break;
                }
            }
            if(strcmp($resp['status'],"success")==0)
                exit(json_encode($resp));
            else{
                $resp['status']='error';
                exit(json_encode($resp));
            }
        }

        public function get_region_boundary(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $rid = $var['region_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'regions','region_id',$rid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                  $resp['coordinates'] = $dataobject_value['coordinates'];
                  $resp['location'] = $dataobject_value['location'];
            }
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_state_boundary(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $sid = $var['state_code'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'states','state_code',$sid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                  $resp['coordinates'] = $dataobject_value['coordinates'];
                  $resp['location'] = $dataobject_value['location'];
            }
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_country_focus(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $cid = $var['country_code'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'countries','country_code',$cid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                  $resp['location'] = $dataobject_value['location'];
            }
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_poi_details(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $pid = $var['poi_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'pois','poi_id',$pid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                  $resp['location'] = $dataobject_value['location'];
                  $resp['description'] = $dataobject_value['description'];
                  $resp['image'] = $dataobject_value['poi_picture'];
                  $resp['url'] = $dataobject_value['url'];
            }
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_region_pois(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $rid = $var['region_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'regions','region_id',$rid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                $region_name = $dataobject_value['region_name'];
            }
            $entity = $this->sys_model1->get_list($keyspace.'pois');
            $pois = array();
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(!is_null($dataobject_value['is_in_region']) && strcmp($region_name,$dataobject_value['is_in_region'])==0){
                    $resp['location'] = $dataobject_value['location'];
                    $resp['description'] = $dataobject_value['description'];
                    $resp['image'] = $dataobject_value['poi_picture'];
                    $resp['url'] = $dataobject_value['url'];
                    $resp['name'] = $dataobject_value['poi_name'];
                    array_push($pois,$resp);
                }
            }
            $jresp['status']='success';
            $jresp['poilist']=$pois;
            exit(json_encode($jresp));
        }

        public function get_state_regions(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $sid = $var['state_code'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'states','state_code',$sid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                $state_name = $dataobject_value['state_name'];
            }
            $entity = $this->sys_model1->get_list($keyspace.'regions');
            $regions = array();
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(!is_null($dataobject_value['is_in_state']) && strcmp($state_name,$dataobject_value['is_in_state'])==0){
                    $resp['region_name']=$dataobject_value['region_name'];
                    $resp['region_id']=$dataobject_value['region_id'];
                    $resp['coordinates']=$dataobject_value['coordinates'];
                    $resp['location']=$dataobject_value['location'];
                    array_push($regions,$resp);
                }
            }
            $jresp['status']='success';
            $jresp['regionlist']=$regions;
            exit(json_encode($jresp));
        }

        public function get_states(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $country=$var['country_name'];
            $statesdata=array();
            $entity = $this->sys_model1->get_list($keyspace.'states');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['is_in_country'],$country)==0){
                  $state['state_name']=$dataobject_value['state_name'];
                  $state['location']=$dataobject_value['location'];
           //       $state['coordinates']=$dataobject_value['coordinates'];
                  array_push($statesdata,$state);
                }
            }
            $entity = $this->sys_model1->get_list($keyspace.'countries');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['country_name'],$country)==0){
                  $resp['coordinates']=$dataobject_value['location'];
                  break;
                }
            }
            $resp['states']=$statesdata;
            exit(json_encode($resp));
        }

        public function get_world_pois(){
            $keyspace = $this->config->item('key_space');
            $countries=array();
            $entity = $this->sys_model1->get_list($keyspace.'countries');
            foreach($entity as $dataobject_key=>$dataobject_value){
                $item = array();
                $item['id']=$dataobject_value['country_code'];
                $item['name']=$dataobject_value['country_name'];
                $item['flagUrl']=$dataobject_value['country_flag'];
                $item['iconUrl']=$dataobject_value['country_icon'];
                $item['latLngBounds']=$dataobject_value['bounds'];
                array_push($countries,$item);        
            }
            $states = array();
            $entity = $this->sys_model1->get_list($keyspace.'states');
            foreach($entity as $dataobject_key=>$dataobject_value){
                $item = array();
                $item['id']=$dataobject_value['state_code'];
                $item['name']=$dataobject_value['state_name'];
                $item['is_in_country']=$dataobject_value['is_in_country'];
                array_push($states,$item);
            }
            $regions = array();
            $entity = $this->sys_model1->get_list($keyspace.'regions');
            foreach($entity as $dataobject_key=>$dataobject_value){
                $item = array();
                $item['id']=$dataobject_value['region_id'];
                $item['name']=$dataobject_value['region_name'];
                $item['is_in_state']=$dataobject_value['is_in_state'];
                $item['is_in_country']=$dataobject_value['is_in_country'];
                array_push($regions,$item);
            }
            $pois = array();
            $entity = $this->sys_model1->get_list($keyspace.'pois');
            foreach($entity as $dataobject_key=>$dataobject_value){
                $item = array();
                $item['id']=$dataobject_value['poi_id'];
                $item['name']=$dataobject_value['poi_name'];
                $item['is_in_region']=$dataobject_value['is_in_region'];
                $item['is_in_state']=$dataobject_value['is_in_state'];
                $item['is_in_country']=$dataobject_value['is_in_country'];
                $item['location']=$dataobject_value['location'];
                $item['address']=$dataobject_value['poi_address'];
                $item['category']=$dataobject_value['category'];
                $item['description']=$dataobject_value['description'];
                $item['eventdate']=$dataobject_value['eventdate'];
                $item['phone']=$dataobject_value['phone_number'];
                $item['url']=$dataobject_value['url'];
                $item['picture']=$dataobject_value['poi_picture'];
                array_push($pois,$item);
            }
            $resp['status']='success';
            $resp['countries']=$countries;
            $resp['states']=$states;
            $resp['regions']=$regions;
            $resp['pois']=$pois;
            exit(json_encode($resp));
        }
    }
?>	