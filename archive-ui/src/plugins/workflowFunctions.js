import Vue from 'vue'
Vue.prototype.startWorkflowFun = function (formId, processName, processInstanseId, processInstanceKey,callback) {
    var m = new Map();
    let _self=this;
    m.set("formId", formId);

    m.set("processName", processName);
    if(!(processInstanseId||processInstanceKey)){
        _self.$message({
            showClose: true,
            message: "流程名称和流程ID都为空无法发起流程!",
            duration: 2000,
            type: "warning",
        });
        return;
    }
    if(processInstanseId){
        m.set("processInstanceId", processInstanseId);
    }
    if(processInstanceKey){
        m.set("processInstanceKey",processInstanceKey);
    }
    
    axios
        .post("/workflow/startWorkflowById", JSON.stringify(m))
        .then(function (response) {
            console.log(response);
            _self.$message({
                showClose: true,
                message: "流程发起成功!",
                duration: 2000,
                type: "success",
            });
            if(callback){
                callback();
            }
        })
        .catch(function (error) {
            _self.$message({
                showClose: true,
                message: "流程发起失败!",
                duration: 2000,
                type: "warning",
            });
            console.log(error);
        });
}

Vue.prototype.startWorkflowFunByIds = function (formIds, processName, processInstanseId, processInstanceKey,callback) {
    
    let _self=this;
    
    if(!(processInstanseId||processInstanceKey)){
        _self.$message({
            showClose: true,
            message: "流程名称和流程ID都为空无法发起流程!",
            duration: 2000,
            type: "warning",
        });
        return;
    }

    if(!formIds||formIds.length==0){
        _self.$message({
            showClose: true,
            message: "没有选择数据，请先选择数据!",
            duration: 2000,
            type: "warning",
        });
        return;
    }

    let a=new Array();
    for(let n=0;n<formIds.length;n++){
        let formId=formIds[n];
        let m = new Map();
        m.set("formId", formId);
        m.set("processName", processName);
        if(processInstanseId){
            m.set("processInstanceId", processInstanseId);
        }
        if(processInstanceKey){
            m.set("processInstanceKey",processInstanceKey);
        }
        a.push(m)
    }
    
    axios
        .post("/workflow/startWorkflowByIds", JSON.stringify(a))
        .then(function (response) {
            console.log(response);
            _self.$message({
                showClose: true,
                message: "流程发起成功!",
                duration: 2000,
                type: "success",
            });
            if(callback){
                callback();
            }
        })
        .catch(function (error) {
            _self.$message({
                showClose: true,
                message: "流程发起失败!",
                duration: 2000,
                type: "warning",
            });
            console.log(error);
        });
}

