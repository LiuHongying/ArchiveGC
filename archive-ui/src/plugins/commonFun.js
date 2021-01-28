
import Vue from 'vue'
//逻辑删除
Vue.prototype.logicallyDel=function(items,callback){
    let _self=this;
    if(!items||items.length==0){
        _self.$message({
            showClose: true,
            message: _self.$t("message.PleaseSelectOneOrMoreData"),
            duration: 2000,
            type: "error"
          });
          return;
    }
    
    let m=new Map();
    let ids=new Array();
    items.forEach(element => {
        ids.push(element.ID);
    });
    m.set("ids",ids);
    axios
    .post("/dc/logicallyDel", JSON.stringify(m))
    .then(function(response) {
      if(response.data.code!=1){
        _self.$message({
          showClose: true,
          message: _self.$t("message.deleteFailured"),
          duration: 2000,
          type: "error"
        });
        return;
      }else{
        _self.$message({
          showClose: true,
          message: _self.$t("message.deleteSuccess"),
          duration: 2000,
          type: "success"
        });
        if(callback){
          callback();
        }
      }
      
    });
}
//修改数据
Vue.prototype.updateData=function(param,callback){
    let _self=this;
    axios
    .post("/dc/updateData", JSON.stringify(param))
    .then(function(response) {
      if(response.data.code!=1){
        _self.$message({
          showClose: true,
          message: _self.$t("message.updateFailured"),
          duration: 2000,
          type: "error"
        });
        return;
      }else{
        _self.$message({
          showClose: true,
          message: _self.$t("message.updateSuccess"),
          duration: 2000,
          type: "success"
        });
        if(callback){
          callback();
        }
      }
      
    });
}
//获取对象byId
Vue.prototype.getObjectById=function(id,callback){
    axios
          .post("/dc/getDocumentById", id)
          .then(function(responsedoc) {
            let result = responsedoc.data;
            if (result.code == 1) {
              if(callback){
                callback(result.data);
              }
            }
          });
}
//根据ACL获取权限
Vue.prototype.getPermission=function(aclName,callback){
  let _self=this;
  axios
        .post("/acl/getPermission", aclName)
        .then(function(response) {
          let result = response.data;
          if (result.code == 1) {
            if(callback){
              callback(result.permission);
            }
          }else{
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "warning"
            });
          }
        });
}
Vue.prototype.sqlStringFilter = function(keyString){
	if(keyString && keyString.length>0){
    keyString = keyString.replace(new RegExp("'","gm"),"''");//.replace(new RegExp("(","gm"),"").replace(new RegExp(")","gm"),"");
    keyString = keyString.replace("(","");
    keyString = keyString.replace(")","");
	}
	return keyString;
}