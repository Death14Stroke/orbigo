<?php
    class User_details extends CI_controller{
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

        public function upload_picture(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $type = $var['type'];
            $entry['profile_image']=$var['profile_image'];
            $this->sys_model1->update($keyspace.'user','user_id',$userid,$entry);
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function upload_picture_business(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $abn = $var['abn'];
            $entry['business_image']=$var['profile_image'];
            $this->sys_model1->update($keyspace.'businesses','abn',$abn,$entry);
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_my_profile(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'user','user_id',$userid);
                foreach($entity as $dataobject_key=>$dataobject_value){
                    $resp['email_address']=$dataobject_value['email_address'];
                    $resp['profile_image']=$dataobject_value['profile_image'];
                    $resp['name']=$dataobject_value['name'];
                    $resp['status']='success';
                    exit(json_encode($resp));
                }
            $resp['status']='error';
            exit(json_encode($resp));
        }

        public function switch_profile(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['user_id'];
            $switchTo = $var['switch_to'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'user','user_id',$userid);
                foreach($entity as $dataobject_key=>$dataobject_value){
                    switch($switchTo){
                        case 'tvt':
                            if($dataobject_value['is_tvt']=='true'){
                                $entry['active_mode']='tvt';
                                $this->sys_model1->update($keyspace.'user','user_id',$user,$entry);
                                $resp['status']='success';
                                $resp['message']='Switching to tourist profile';
                            }
                            else{
                                $resp['status']='error';
                                $resp['message']='tvt_error';
                            }
                            exit(json_encode($resp));
                            break;
                        case 'bto':
                            if($dataobject_value['is_bto']=='true'){
                                $entry['active_mode']='bto';
                                $this->sys_model1->update($keyspace.'user','user_id',$user,$entry);
                                $resp['status']='success';
                                $resp['message']='Switching to business profile';
                            }
                            else{
                                $resp['status']='error';
                                $resp['message']='bto_error';
                            }
                            exit(json_encode($resp));
                            break;
                    }
                }
            $resp['status']='error';
            $resp['message']='Unknown error';
            exit(json_encode($resp));
        }
    }
?>