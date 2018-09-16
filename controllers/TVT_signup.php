<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

class TVT_signup extends CI_controller{

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
        $entry['tourist_id'] = $var['tourist_id'];
        $entry['facebook_id'] = $var['facebook_id'];
        $tokenData['user_id']=$var['tourist_id'];
        $tokenData['user_token']=$var['user_token'];
        $this->sys_model1->add($keyspace.'firebase_tokens',$tokenData); 
        $entity = $this->sys_model1->get_list($keyspace.'tourist');
        foreach($entity as $dataobject_key=>$dataobject_value){
            if(strcmp($dataobject_value['tourist_id'],$entry['tourist_id'])==0){
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
        $this->sys_model1->add($keyspace.'tourist',$entry); 
        $resp['status']='success';
        $resp['message']='Registration complete';
        exit(json_encode($resp));
    }
}
?>