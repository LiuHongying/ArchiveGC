<template>
    <DataLayout>
        <template v-slot:header>
            <el-dialog
                :title="dialogName+$t('application.property')"
                :visible.sync="propertyVisible"
                @close="propertyVisible = false"
                width="90%"
                :close-on-click-modal="false"
                v-dialogDrag
                >
                <!-- <CommonStartup 
                ref="startup"
                :workflowObj="workflowObj" 
                @close="closeDialog()"
                :showUploadFile="false"></CommonStartup> -->
                <component
                    ref="startup"
                    :is="componentName"
                    :workflowObj="workflowObj"
                    :typeName="typeName"
                    @close="closeDialog()"
                    :showUploadFile="false"
                ></component>
            </el-dialog>
        </template>
        <template v-slot:main="{layout}">
           
                        <DataGrid
                            ref="mainDataGrid"
                            key="main"
                            dataUrl="/dc/getWorkflows"
                            v-bind:tableHeight="(layout.height-startHeight)"
                            v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                            gridViewName="WorkflowGrid"
                            condition="lastVersion"
                            @rowclick="rowClick"
                            :isshowCustom="false"
                            :isEditProperty="false"
                            showOptions="查看内容"
                            :optionWidth = "2"
                            isInitData
                            :isShowChangeList="false"
                        >
                           
                           <template slot="optionButton" slot-scope="scope">
                                <el-button
                                    type="primary"
                                    plain
                                    size="small"
                                    :title="$t('application.StartUpWorkflow')"
                                    icon="el-icon-more"
                                    @click="draftData(scope.data.row)"
                                    ></el-button>
                                <!-- <el-dropdown-item icon="el-icon-chat-line-square" >{{$t('application.Reply')}}</el-dropdown-item> -->
                            </template>
                        </DataGrid>

            
        </template>
    </DataLayout>
    
</template>
<script type="text/javascript">
    import ShowProperty from "@/components/ShowProperty";
    import DataGrid from "@/components/DataGrid";
    import AddCondition from '@/views/record/AddCondition';
    import RejectButton from "@/components/RejectButton";
    import ExcelUtil from '@/utils/excel.js'
    import DataSelect from '@/components/ecm-data-select'
    import DataLayout from '@/components/ecm-data-layout'
    import AttachmentFile from "@/views/dc/AttachmentFile.vue"
    import FormFile from "@/views/workflow/FormFile.vue"
    import CommonStartup from "@/views/workflow/CommonStartup.vue"
    export default {
        name: "TodoTask",
        permit: 1,
        components: {
            ShowProperty:ShowProperty,
            DataGrid:DataGrid,
            AddCondition:AddCondition,
            DataSelect:DataSelect,
            RejectButton:RejectButton,
            DataLayout:DataLayout,
            AttachmentFile:AttachmentFile,
            FormFile:FormFile,
            CommonStartup:CommonStartup
        },
        data() {
                return {
                    // 本地存储高度名称
                    topStorageName: 'ReceivedDCHeight',
                    // 非split pan 控制区域高度
                    startHeight: 145,
                    // 顶部百分比*100
                    topPercent: 65,
                    // 顶部除列表高度
                    topbarHeight: 35,
                    // 底部除列表高度
                    bottomHeight: 120,
                    dialogName:"",
                    propertyVisible:false,
                    typeName:"",
                    isOnly:false,
                    butt:false,
                    workflowObj:{},
                    workflowFileList:[],
                    componentName:""
                }
            },
            mounted(){
                // this.getWorkflows();
            },
            methods:{
                closeDialog(){
                    this.propertyVisible=false;
                },
                startUpWorkflow(workflow){
                   let _self = this;
                    _self.butt=true;
                    if(!this.$refs.ShowProperty.validFormValue()){
                        _self.butt=false;
                        return;
                    }
                    _self.butt=true
                    var m = new Map();
                    let fileIds=new Array();
                    for(let n=0;n<_self.workflowFileList.length;n++){
                        fileIds.push(_self.workflowFileList[n].ID);
                    }
                    m.set("childFileId",fileIds);
                    var c;
                    for(c in _self.$refs.ShowProperty.dataList)
                    {
                        let dataRows = _self.$refs.ShowProperty.dataList[c].ecmFormItems;
                        var i;
                        for (i in dataRows) {
                        if(dataRows[i].attrName && dataRows[i].attrName !='')
                        {
                            if(dataRows[i].attrName !='FOLDER_ID'&&dataRows[i].attrName !='ID')
                            {
                            var val = dataRows[i].defaultValue;
                            if(val && dataRows[i].isRepeat){
                                var temp = "";
                            // console.log(val);
                                for(let j=0,len=val.length;j<len;j++){
                                temp = temp + val[j]+";";
                                //console.log(temp);
                                }
                                temp = temp.substring(0,temp.length-1);
                                val = temp;
                                console.log(val);
                            }
                            m.set(dataRows[i].attrName, val);
                            }
                        }
                        }
                    }
                    if(_self.$refs.ShowProperty.myItemId!='')
                    {
                        m.set('ID',_self.$refs.ShowProperty.myItemId);
                    }
                    if(_self.$refs.ShowProperty.myTypeName!='')
                    {
                        m.set('TYPE_NAME',_self.$refs.ShowProperty.myTypeName);
                        m.set('FOLDER_ID',_self.$refs.ShowProperty.myFolderId);
                        m.set("parentDocId", _self.parentId);
                        m.set("relationName",_self.relationName);
                        console.log(_self.$refs.ShowProperty.myTypeName)
                        console.log(_self.$refs.ShowProperty.myFolderId)
                    }
                    _self.validateData(m,function(isOk)
                    {
                        _self.isOnly=isOk;

                        if(_self.isOnly==false){
                            _self.$message({
                                showClose: true,
                                message: _self.$t('message.dataIsnotOnly'),
                                duration: 2000,
                                type: 'error'
                            });
                            _self.butt=false;
                            return;
                        }
                        let formdata = new FormData();
                        formdata.append("metaData",JSON.stringify(m));
                        if(_self.$refs.ShowProperty.file!="")
                        {
                            //console.log(_self.file);
                            formdata.append("uploadFile",_self.$refs.ShowProperty.file.raw);
                        }
                        // console.log(JSON.stringify(m));
                        if(_self.$refs.ShowProperty.myItemId=='')
                        {
                            axios.post("/dc/createWorkflowFormData",formdata,{
                                'Content-Type': 'multipart/form-data'
                            })
                            .then(function(response) {
                            let code = response.data.code;
                            //console.log(JSON.stringify(response));
                            if (code == 1) {
                                
                                _self.$message({
                                    showClose: true,
                                    message: _self.$t('message.newSuccess'),//_self.$t('message.newSuccess')
                                    duration: 2000,
                                    type: "success"
                                });
                                //发起流程

                                _self.butt=false;
                                _self.propertyVisible = false;
                                _self.propertyrela=false

                                m.set("formId", response.data.id);
                                
                                m.set("processName",workflow.NAME);
                                m.set("processInstanceId",workflow.ID);
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
                            else if(response.data.MES!=""){

                                _self.$message({
                                    showClose: true,
                                    message: response.data.MES,
                                    duration: 2000,
                                    type: "warning"
                                });
                                _self.butt=false;
                            
                            }else{
                                _self.$message({
                                    showClose: true,
                                    message: _self.$t('message.newFailured'),
                                    duration: 2000,
                                    type: "warning"
                                });
                                _self.butt=false;
                            }
                            })
                            .catch(function(error) {
                                _self.$message(_self.$t('message.newFailured'));
                                console.log(error);
                                _self.butt=false;
                            });
                        }
                        
                    });

                },
                // 保存文档
                saveItem()
                {
                    let _self = this;
                    _self.saveButt=true;
                    if(!this.$refs.ShowProperty.validFormValue()){
                        _self.saveButt=false;
                        return;
                    }
                    var m = new Map();
                    let fileIds=new Array();
                    for(let n=0;n<_self.workflowFileList.length;n++){
                        fileIds.push(_self.workflowFileList[n].ID);
                    }
                    m.set("childFileId",fileIds);
                    var c;
                    for(c in _self.$refs.ShowProperty.dataList)
                    {
                        let dataRows = _self.$refs.ShowProperty.dataList[c].ecmFormItems;
                        var i;
                        for (i in dataRows) {
                        if(dataRows[i].attrName && dataRows[i].attrName !='')
                        {
                            if(dataRows[i].attrName !='FOLDER_ID'&&dataRows[i].attrName !='ID')
                            {
                            var val = dataRows[i].defaultValue;
                            if(val && dataRows[i].isRepeat){
                                var temp = "";
                            // console.log(val);
                                for(let j=0,len=val.length;j<len;j++){
                                temp = temp + val[j]+";";
                                //console.log(temp);
                                }
                                temp = temp.substring(0,temp.length-1);
                                val = temp;
                                console.log(val);
                            }
                            m.set(dataRows[i].attrName, val);
                            }
                        }
                        }
                    }
                    if(_self.$refs.ShowProperty.myItemId!='')
                    {
                        m.set('ID',_self.$refs.ShowProperty.myItemId);
                    }
                    if(_self.$refs.ShowProperty.myTypeName!='')
                    {
                        m.set('TYPE_NAME',_self.$refs.ShowProperty.myTypeName);
                        m.set('FOLDER_ID',_self.$refs.ShowProperty.myFolderId);
                        m.set("parentDocId", _self.parentId);
                        m.set("relationName",_self.relationName);
                        console.log(_self.$refs.ShowProperty.myTypeName)
                        console.log(_self.$refs.ShowProperty.myFolderId)
                    }
                    _self.validateData(m,function(isOk)
                    {
                        _self.isOnly=isOk;

                        if(_self.isOnly==false){
                            _self.$message({
                                showClose: true,
                                message: _self.$t('message.dataIsnotOnly'),
                                duration: 2000,
                                type: 'error'
                            });
                            _self.saveButt=false;
                            return;
                        }
                        let formdata = new FormData();
                        formdata.append("metaData",JSON.stringify(m));
                        if(_self.$refs.ShowProperty.file!="")
                        {
                            //console.log(_self.file);
                            formdata.append("uploadFile",_self.$refs.ShowProperty.file.raw);
                        }
                        // console.log(JSON.stringify(m));
                        if(_self.$refs.ShowProperty.myItemId=='')
                        {
                            axios.post("/dc/createWorkflowFormData",formdata,{
                                'Content-Type': 'multipart/form-data'
                            })
                            .then(function(response) {
                            let code = response.data.code;
                            //console.log(JSON.stringify(response));
                            if (code == 1) {
                                
                                _self.$message({
                                    showClose: true,
                                    message: _self.$t('message.newSuccess'),//_self.$t('message.newSuccess')
                                    duration: 2000,
                                    type: "success"
                                });
                                //发起流程

                                _self.saveButt=false;
                                _self.propertyVisible = false;
                                _self.propertyrela=false

                                

                            } 
                            else if(response.data.MES!=""){

                                _self.$message({
                                    showClose: true,
                                    message: response.data.MES,
                                    duration: 2000,
                                    type: "warning"
                                });
                                _self.butt=false;
                            
                            }else{
                                _self.$message({
                                    showClose: true,
                                    message: _self.$t('message.newFailured'),
                                    duration: 2000,
                                    type: "warning"
                                });
                                _self.butt=false;
                            }
                            })
                            .catch(function(error) {
                                _self.$message(_self.$t('message.newFailured'));
                                console.log(error);
                                _self.butt=false;
                            });
                        }
                        
                    });
                
                },
                draftData(workflow) 
                {
                    let _self = this;
                    _self.workflowObj=workflow;
                    // _self.propertyVisible = true;
                    _self.typeName=workflow.FORMNAME;
                    this.getEcmcfgActive(this.workflowObj.ID,"start",function(ecmCfgActivity){
                        _self.componentName=ecmCfgActivity.componentName;
                        _self.propertyVisible=true;
                        _self.$nextTick(()=>{
                            if(_self.$refs.startup){
                            _self.$refs.startup.loadFormInfo();
                            }
                        });
                    });
                    // setTimeout(()=>{
                    //     if(_self.$refs.startup){
                    //         _self.$refs.startup.typeName=workflow.FORMNAME;
                    //         // _self.$refs.ShowProperty.myFolderId = _self.selectTransferRow.id;
                    //         _self.$refs.startup.loadFormInfo();
                    //     }
                    // },10);

                },
                // 保存结果事件
                onSaved(indata) {
                    let _self=this;
                    if (indata == "update") {
                        // _self.$message(_self.$t("message.saveSuccess"));
                        _self.$message({
                            showClose: true,
                            message: _self.$t("message.saveSuccess"),
                            duration: 2000,
                            type: 'success'
                        });
                        _self.butt=false
                    } else {
                        // _self.$message("新建成功!");
                        _self.$message({
                            showClose: true,
                            message: _self.$t('message.operationSuccess'),
                            duration: 2000,
                            type: 'success'
                        });
                        _self.butt=false
                    }
                    _self.propertyVisible = false;
                    _self.propertyrela=false;
                    this.butt=false
                },
                
            },
        
    }
</script>
<style scoped>

</style>