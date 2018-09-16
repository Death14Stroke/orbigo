<?php
    class Businesshelper extends CI_controller{

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

        public function add_business(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $entry['abn']=$var['abn'];
            if(!is_null($var['business_email']))
                $entry['business_email']=$var['business_email'];
            $entry['business_phone_number']=$var['business_phone'];
            $entry['business_name']=$var['business_name'];
            $entry['business_address']=$var['business_address'];
            $entry['b_user_id']=$var['user_id'];
            $entry['capacity']='0';
            $entry['no_of_bookings']='0';
            $entry['no_of_visits']='0';
            $this->sys_model1->add($keyspace.'businesses',$entry); 
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_stats(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $userid = $var['b_user_id'];
            $resp['listed_businesses']=0;
            $entity = $this->sys_model1->get_list($keyspace.'businesses');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['b_user_id'],$userid)==0){
                    $resp['listed_businesses']++;
                }
            }
            $resp['earnings_30_days']=0.0;
            $resp['no_of_bookings_30_days']=0;
            $entity = $this->sys_model1->get_list($keyspace.'bookings');
            foreach($entity as $dataobject_key=>$dataobject_value){
                $duration = time()-$dataobject_value['create_ts'];
                //change timestamp variable datatype in db. Parse date in android to adjust once tvt booking is available           
                if($userid==$dataobject_value['b_user_id'] && ($duration>0 && $duration<=30*24*3600)){
                    $resp['no_of_bookings_30_days']++;
                    $resp['earnings_30_days']+=floatval($dataobject_value['amount']);
                }
            }
            $resp['visits_30_days']=0;
            $resp['campaigns_30_days']=0;
            $resp['active_campaigns']=0;
            exit(json_encode($resp));
        }

        public function add_booking(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $entry['booking_id']=$this->sys_model1-> get_unique_id();
            $entry['abn']=$var['abn'];
            $entry['business_name']=$var['business_name'];
            $entry['business_address']=$var['business_address'];
            $entry['customer_name']=$var['customer_name'];
            $entry['customer_phone']=$var['customer_phone'];
            $entry['no_of_adult']=$var['no_of_adult'];
            $entry['no_of_children']=$var['no_of_children'];
            $entry['status']='pending';
            $entry['tourist_id']=$var['tourist_id'];
            $entry['create_ts']=(string)time();
            $this->sys_model1->add($keyspace.'bookings',$entry); 
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function confirm_booking(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $id = $var['booking_id'];
            $entry['status']='confirmed';
            $this->sys_model1->update($keyspace.'bookings','booking_id',$id,$entry);
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_earnings_history(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $id=$var['b_user_id'];
            $resarray = array();
            $entity = $this->sys_model1->get_list($keyspace.'bookings');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['b_user_id'],$id)==0 && strcmp($dataobject_value['status'],'confirmed')==0){
                    array_push($resarray,$dataobject_value);
                }
            }
            $resp['status']='success';
            $resp['earnings']=$resarray;
            exit(json_encode($resp));
        }

        public function get_business_list(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $id = $var['b_user_id'];
            $resarray = array();
            $entity = $this->sys_model1->get_list($keyspace.'businesses');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['b_user_id'],$id)==0)
                    array_push($resarray,$dataobject_value);
            }
            $resp['status']='success';
            $resp['businesses']=$resarray;
            exit(json_encode($resp));
        }

        public function get_categories(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $id=$var['b_user_id'];
            $resarray = array();
            $entity = $this->sys_model1->get_list($keyspace.'business_types');
            foreach($entity as $dataobject_key=>$dataobject_value){
                array_push($resarray,$dataobject_value);
            }
            $resp['status']='success';
            $resp['categories']=$resarray;
            exit(json_encode($resp));
        }

        public function update_business(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $abn = $var['abn'];
            $entry['category']=$var['category'];
            $entry['capacity']=$var['capacity'];
            $entry['price_range']=$var['price_range'];
            if(is_null($var['description']))
                $entry['description']=null;
            else
                $entry['description']=$var['description'];
            $this->sys_model1->update($keyspace.'businesses','abn',$abn,$entry);
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function update_business_calendar(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $abn = $var['abn'];
            $entry['opening_days']=$var['opening_days'];
            $entry['opening_hours']=$var['opening_hours'];
            $this->sys_model1->update($keyspace.'businesses','abn',$abn,$entry);
            $resp['status']='success';
            exit(json_encode($resp));
        }

        public function get_pending_bookings(){
            $keyspace = $this->config->item('key_space');
            $var = json_decode(file_get_contents('php://input'),true);
            $abn = $var['abn'];
            $resarray = array();
            $entity = $this->sys_model1->get_list($keyspace.'bookings');
            foreach($entity as $dataobject_key=>$dataobject_value){
                if(strcmp($dataobject_value['abn'],$abn)==0 && strcmp($dataobject_value['status'],'pending')==0){
                    array_push($resarray,$dataobject_value);
                }
            }
            $resp['status']='success';
            $resp['requests']=$resarray;
            exit(json_encode($resp));
        }
    }
?>