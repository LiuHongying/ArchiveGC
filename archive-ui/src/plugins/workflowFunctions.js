import Vue from 'vue'
Vue.prototype.startWorkflowFun = function (formId, processName, processInstanseId, processInstanceKey) {
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



