<?php

class ATDW extends CI_controller{

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
    
    public function insert_to_dbase(){
        $keyspace = $this->config->item('key_space');
        $poi = json_decode(file_get_contents('php://input'),true);
        $this->sys_model1->add($keyspace.'pois',$poi); 
        exit('success');
    }
}
?>