import Vue from 'vue'
import $router from '../router'
Vue.prototype.validataPermission=async (compName)=>{
    let haspermission=false;
    let user = sessionStorage.getItem("ecm-current-user");
    if(user){
      let cuser =  JSON.parse(user);
      let systemPermission = Number(
        cuser.systemPermission
      );
      if(systemPermission>=9){
        return true;
       }
    }
    await axios.post("/user/validatapermission",compName).then(function(response){
         let code= response.data.code;
         if(code==1){
            haspermission = true;
         }
         if(!haspermission){
             
            $router.push({ path: "/NoPermission" });
          }
         
    })
    return haspermission;
}

Vue.prototype.validataSystemPermission=async (permitNum)=>{
  let haspermission=false;
  let user = sessionStorage.getItem("ecm-current-user");
  if(user){
    let cuser =  JSON.parse(user);
    let systemPermission = Number(
      cuser.systemPermission
    );
    if(systemPermission>=permitNum){
      return true;
     }
  }
  $router.push({ path: "/NoPermission" });
  return false;
}


