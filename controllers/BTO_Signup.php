<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

class BTO_Signup extends CI_controller{

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
        $keyspace = $this->config->item('key_space');
        $var = json_decode(file_get_contents('php://input'),true);
        $entry['b_user_id'] = $var['b_user_id'];
        $entry['facebook_id'] = $var['facebook_id'];
        $tokenData['user_id']=$var['tourist_id'];
        $tokenData['user_token']=$var['user_token'];
        $this->sys_model1->add($keyspace.'firebase_tokens',$tokenData); 
        $entity = $this->sys_model1->get_list($keyspace.'businessmen');
        foreach($entity as $dataobject_key=>$dataobject_value){
            if(strcmp($dataobject_value['b_user_id'],$entry['b_user_id'])==0){
                $resp['status']='success';
                $resp['message']='Already registered';
                exit(json_encode($resp));
            }
        }
        if(strcmp($var['case'],'login')==0 && is_null($entry['facebook_id'])){
            $resp['status']='error';
            $resp['message']='register first';
            exit(json_encode($resp));
        }
        $entry['created_date'] = (string)time();
        $entry['email_address'] = $var['email_address'];
        $entry['gender'] = $var['gender'];
        $entry['name'] = $var['name'];
        $entry['phone_number'] = $var['phone_number'];
        $entry['privacy'] = $var['privacy'];
        $entry['profile_image'] = $var['profile_image'];
        $entry['status'] = $var['status'];
        $this->sys_model1->add($keyspace.'businessmen',$entry); 
        $resp['status'] = 'success';
        $resp['message'] = 'Account created successfully';
        exit(json_encode($resp));
    }
}
?>