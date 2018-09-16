<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

class Login_signup extends CI_controller{

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

    public function signup(){
        ini_set('memory_limit',512);
        $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('php://input'),true);
        $tokenData['user_id']=$var['user_id'];
        $tokenData['user_token']=$var['user_token'];
        $this->sys_model1->add($keyspace.'firebase_tokens',$tokenData); 
        $entry['user_id']=$var['user_id'];
        $entry['created_date']=(string)time();
        $entry['is_bto']=$var['is_bto'];
        $entry['is_tvt']=$var['is_tvt'];
        if(!is_null($var['facebook_id'])){
            $entry['facebook_id']=$var['facebook_id'];
            $entity = $this->sys_model1->get_list_by_key($keyspace.'user','user_id',$var['user_id']);
            foreach ($entity as $value) {
                $entry['created_date']=$value['created_date'];
                $entry['is_bto']=$value['is_bto'];
                $entry['is_tvt']=$value['is_tvt'];
            }
            $entry['active_mode']=$var['active_mode'];
        }
        $entry['email_address']=$var['email_address'];
        if(!is_null($var['gender']))
            $entry['gender']=$var['gender'];
        $entry['name']=$var['name'];
        if(!is_null($var['phone_number']))
            $entry['phone_number']=$var['phone_number'];
        if(!is_null($var['profile_image']))
            $entry['profile_image']=(string)base64_encode($var['profile_image']);
        $this->sys_model1->add($keyspace.'user',$entry);
        $resp['status']='success';
        $resp['message'] = 'Account created successfully';
        $resp['type'] = gettype($var['is_tvt']).' '.gettype($var['is_bto']);
        exit(json_encode($resp));
    }

    public function login(){
        $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('php://input'),true);
        $tokenData['user_id']=$var['user_id'];
        $tokenData['user_token']=$var['user_token'];
        $this->sys_model1->add($keyspace.'firebase_tokens',$tokenData); 
        $entity = $this->sys_model1->get_list_by_key($keyspace.'user','user_id',$var['user_id']);
        if(count($entity)==0){
            $entry['user_id']=$var['user_id'];
            $entry['created_date']=(string)time();
            $entry['facebook_id']=$var['facebook_id'];
            $entry['active_mode']=$var['mode'];
            $entry['email_address']=$var['email_address'];
            $entry['gender']=$var['gender'];
            $entry['name']=$var['name'];
            switch($var['mode']){
                case 'tvt':
                    $entry['is_tvt']='true';
                    $entry['is_bto']='false';
                    break;
                case 'bto':
                    $entry['is_tvt']='false';
                    $entry['is_bto']='true';
                    break;
            }
            if(!is_null($var['profile_image']))
                $entry['profile_image']=(string)base64_encode($var['profile_image']);
            $this->sys_model1->add($keyspace.'user',$entry);
        }
        foreach($entity as $dataobject_key=>$dataobject_value){
            switch($var['mode']){
                case 'tvt':
                    if($dataobject_value['is_tvt']=='true'){
                        $entry['active_mode']='tvt';
                        $this->sys_model1->update($keyspace.'user','user_id',$var['user_id'],$entry);
                        $resp['status']='success';
                        $resp['message']='tvt login success';
                        exit(json_encode($resp));
                    }
                    else{
                        $resp['status']='error';
                        $resp['message']='tvt_error';
                        exit(json_encode($resp));
                    }
                    break;
                case 'bto':
                    if($dataobject_value['is_bto']=='true'){    
                        $entry['active_mode']='bto';
                        $this->sys_model1->update($keyspace.'user','user_id',$var['user_id'],$entry);
                        $resp['status']='success';
                        $resp['message']='bto login success';
                        exit(json_encode($resp));
                    }
                    else{
                        $resp['status']='error';
                        $resp['message']='bto_error';
                        exit(json_encode($resp));
                    }
                    break;
            }
        }
    }

    public function create_profile(){
        $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('php://input'),true);
        if($var['mode']=='tourist'){
            $entry['active_mode']='tvt';
            $entry['is_tvt']='true';
        }
        else{
            $entry['active_mode']='bto';
            $entry['is_bto']='true';
        }
        $this->sys_model1->update($keyspace.'user','user_id',$var['user_id'],$entry);
        $resp['status']='success';
        $resp['message']='profile created';
        exit(json_encode($resp));
    }

    public function logout(){
        $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('php://input'),true);
        $entry['active_mode']='inactive';
        $this->sys_model1->update($keyspace.'user','user_id',$var['user_id'],$entry);
        $resp['status']='success';
        $resp['message']='logout successful';
        exit(json_encode($resp));
    }
}
?>