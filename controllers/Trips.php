<?php
    class Trips extends CI_controller{

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

        public function create_new_trip(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $entry['trip_id']=(string)$this->sys_model1->get_unique_id();
            $entry['user_id']=$var['user_id'];
            $entry['trip_name']=$var['trip_name'];
            $entry['created_ts']=$var['start_ts'];
            $entry['start_ts']=$var['start_ts'];
            $entry['start_from']=json_encode($var['start_from']);
            if(!is_null($var['added_poi'])){
                $places = array();
                array_push($places,$var['added_poi']);
                $entry['places_list']=json_encode($places);
            }
            $this->sys_model1->add($keyspace.'my_trips',$entry);
            $resp['status']='success';
            $resp['message']='Trip created successfully';
            exit(json_encode($resp));
        }

        public function get_my_trips(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $trips = array();
            $entity = $this->sys_model1->get_list_by_key($keyspace.'my_trips','user_id',$userid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                $trip['trip_id']=(string)$dataobject_value['trip_id'];
                $trip['trip_name']=$dataobject_value['trip_name'];
                $trip['created_ts']=$dataobject_value['created_ts'];
                array_push($trips,$trip);
            }
            $resp['status']='success';
            $resp['trips']=$trips;
            exit(json_encode($resp));
        }

        public function get_trip_members(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $tripid = $var['trip_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'my_trips','trip_id',$tripid);
            foreach ($entity as $dataobject_key=>$dataobject_value) {
                if(!is_null($dataobject_value['partners_list'])){
                    $resp['members'] = json_decode($dataobject_value['partners_list'],true);
                    $resp['status']='success';
                    exit(json_encode($resp));
                }
            }
            $resp['status']='error';
            $resp['message']='no members';
            exit(json_encode($resp));
        }

        public function remove_member(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $tripid = $var['trip_id'];
            $partnersList = array();
            $entity = $this->sys_model1->get_list_by_key($keyspace.'my_trips','trip_id',$tripid);
            foreach ($entity as $dataobject_key=>$dataobject_value) {
                if(!is_null($dataobject_value['partners_list']))
                    $partnersList = json_decode($dataobject_value['partners_list'],true);
            }
            $key=array_search($var['member'],$partnersList);
            if($key!==false){
                array_splice($partnersList,$key,1);
                $entry['partners_list']=json_encode($partnersList);
                $this->sys_model1->update($keyspace.'my_trips','trip_id',$tripid,$entry);
                $resp['status']='success';
                $resp['message']='Member removed';
                exit(json_encode($resp));
            }
            else{
                $resp['partnerList']=$partnersList[0]['member_name'];
                $resp['status']=$key;
                $resp['message']='Error in removing member';
                exit(json_encode($resp));
            }
        }

        public function add_trip_member(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $tripid = $var['trip_id'];
            $partnersList = array();
            $entity = $this->sys_model1->get_list_by_key($keyspace.'my_trips','trip_id',$tripid);
            foreach ($entity as $dataobject_key=>$dataobject_value) {
                if(!is_null($dataobject_value['partners_list']))
                    $partnersList = json_decode($dataobject_value['partners_list'],true);
            }
            if(!in_array($var['member'],$partnersList))
                array_push($partnersList,$var['member']);
            else{
                $resp['status']='error';
                $resp['message']='Member already added to trip';
                exit(json_encode($resp));
            }
            $entry['partners_list']=json_encode($partnersList);
            $this->sys_model1->update($keyspace.'my_trips','trip_id',$tripid,$entry);
            $resp['status']='success';
            $resp['message']='Member added to your trip';
            exit(json_encode($resp));
        }

        public function add_to_trip(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $tripid = $var['trip_id'];        
            $poiList = array();
            $entity = $this->sys_model1->get_list_by_key($keyspace.'my_trips','trip_id',$var['trip_id']);
            foreach ($entity as $dataobject_key=>$dataobject_value) {
                if(!is_null($dataobject_value['places_list']))
                    $poiList = json_decode($dataobject_value['places_list'],true);
            }
            if(!in_array($var['added_poi'],$poiList))
                array_push($poiList,$var['added_poi']);
            else{
                $resp['status']='error';
                $resp['message']='Destination already added to trip';
                exit(json_encode($resp));
            }
            $entry['places_list']=json_encode($poiList);
            $this->sys_model1->update($keyspace.'my_trips','trip_id',$tripid,$entry);
            $resp['status']='success';
            $resp['message']='Added to your trip';
            exit(json_encode($resp));
        }

        public function get_saved_pois(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'saved_pois','user_id',$userid);
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(is_null($dataobject_value['poi_list'])){
                    $resp['status']='error';
                    $resp['message']='no saved pois';
                }
                else{
                    $poiIds = json_decode($dataobject_value['poi_list'],true);
                    $poiList = array();
                    foreach ($poiIds as $id) {
                        $entity2 = $this->sys_model1->get_list_by_key($keyspace.'pois','poi_id',$id);
                        foreach ($entity2 as $poi) {
                            $mypoi['category']=$poi['category'];
                            $mypoi['picture']=$poi['poi_picture'];
                            $mypoi['name']=$poi['poi_name'];
                            $mypoi['is_in_region']=$poi['is_in_region'];
                            $mypoi['is_in_state']=$poi['is_in_state'];
                            $mypoi['is_in_country']=$poi['is_in_country'];
                            $mypoi['id']=$poi['poi_id'];
                            array_push($poiList,$mypoi);
                        }   
                    }
                    $resp['status']='success';
                    $resp['message']='Here is list';
                    $resp['poi_details']=$poiList;
                }
            }
            exit(json_encode($resp));
        }

        public function add_saved_poi(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'saved_pois','user_id',$userid);
            $savedList = array();
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(!is_null($dataobject_value['poi_list'])){
                    $savedList = json_decode($dataobject_value['poi_list'],true);
                }
            }
            if(!in_array($var['add_poi'],$savedList))
                array_push($savedList,$var['add_poi']);
            $entry['poi_list']=json_encode($savedList);
            $this->sys_model1->update($keyspace.'saved_pois','user_id',$userid,$entry);
            $resp['status']='success';
            $resp['message']='Poi saved successfully';
            exit(json_encode($resp));
        }

        public function remove_saved_poi(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'saved_pois','user_id',$userid);
            $savedList = array();
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(!is_null($dataobject_value['poi_list'])){
                    $savedList = json_decode($dataobject_value['poi_list'],true);
                }
            }
            $key = array_search($var['del_poi'],$savedList);
            if($key!==false)
                array_splice($savedList,$key,1);
            $resp['list']=$savedList;
            if(count($savedList)!=0){
                $entry['poi_list']=json_encode($savedList);
                $this->sys_model1->update($keyspace.'saved_pois','user_id',$userid,$entry);
            }
            else{
                $this->sys_model1->delete($keyspace.'saved_pois','user_id',$userid);
            }
            $resp['status']='success';
            $resp['message']='Removed from your saved list';
            //$resp['typeofitem']=gettype($savedList[0]);
            //$resp['typeofinp']=gettype($var['del_poi']);
            //if(in_array($var['del_poi'],$savedList,true))
              //  $resp['inarray']=true;
            //$resp['valueofarr0']=$savedList[0];
            exit(json_encode($resp));
        }

        public function get_trip_components(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $tripid = $var['trip_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'my_trips','trip_id',$tripid);
            $pois = array();
            foreach($entity as $dataobject_key=>$dataobject_value){
                $resp['start_ts']=$dataobject_value['start_ts'];
                $resp['start_from']=json_decode($dataobject_value['start_from']);
                $waypointIds = json_decode($dataobject_value['places_list'],true);
                foreach ($waypointIds as $id) {
                    $entity2 = $this->sys_model1->get_list_by_key($keyspace.'pois','poi_id',$id);
                    foreach ($entity2 as $poi) {
                        $item['name']=$poi['poi_name'];
                        $item['area']=$poi['is_in_state'];
                        $item['category']=$poi['category'];
                        $item['picture']=$poi['poi_picture'];
                        $item['location']=$poi['location'];
                        $item['id']=$poi['poi_id'];
                        array_push($pois,$item);
                    }
                }
            }
            //complete this function with directions api
            $resp['status']='success';
            $resp['list']=$pois;
            exit(json_encode($resp));
        }
    }
?>