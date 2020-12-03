<template>
    <el-container>
        <el-main>
            <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" :close-on-click-modal="false" :append-to-body="true">
                <el-form size="mini" :label-width="formLabelWidth" v-loading='uploading'>
                    <div style="height:150px;overflow-y:scroll; overflow-x:scroll;">
                    <el-upload
                        :limit="100"
                        :file-list="fileList"
                        action
                        :on-change="handleChange"
                        :auto-upload="false"
                        :multiple="true"
                    >
                        <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
                    </el-upload>
                    </div>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="importdialogVisible = false">{{$t('application.cancel')}}</el-button>
                    <el-button type="primary" @click="uploadData()">{{$t('application.start')+$t('application.Import')}}</el-button>
                </div>
            </el-dialog>
            <ShowProperty
                    ref="ShowProperty"
                    @onSaved="onSaved"
                    width="100%"
                    folderPath=""
                    :showUploadFile="showUploadFile"
                    v-bind:typeName="typeName"
                >
                </ShowProperty>
                <BorrowFile ref="workflowFile"
                    allowEdit
                    :isShowPage="false"
                    v-model="workflowFileList"
                    :workflowObj="workflowObj"
                >
                </BorrowFile>
                <el-dialog
                title="档案利用承诺书"
                :visible.sync="dialogVisible"
                :show-close="true"
                width="50%"
                :append-to-body="true"
                modal-append-to-body="false" 
                >
                <span>
                中国核电工程有限公司</br>
                档案利用承诺书  </br>

                根据《中华人民共和国档案法》以及中国核电工程有限公司（以下简称“公司”）档案利用工作有关规定，作为公司员工，本人对档案利用作以下承诺：</br>
                一、自觉遵守国家档案法律、法规及公司的档案利用规章制度；</br>
                二、所借阅档案不得做任何涂改、抽取、替换或添加信息；</br>
                三、所借阅档案应妥善保管、不得损坏、丢失，已装订成册的不得拆散；</br>
                四、所借阅档案不得扩大知悉范围，不得拍照、复制或转借他人；</br>
                五、所借阅档案应在规定时间内归还；</br>
                六、本《承诺书》未尽事宜按国家有关法律法规和公司规定执行。</span>

                </el-dialog>

            <el-radio v-model="accept" label="接受">接受档案承诺利用书</el-radio>
            <el-button type="success" @click="open">档案利用承诺书</el-button>

                <div class="dialog-footer" style="float:right">
                    <slot name="footerButton">
                        <el-button @click="startUpWorkflow(workflowObj)" :loading="butt">{{$t('application.StartUpWorkflow')}}</el-button>
                        
                        <el-button  v-on:click="saveItem" :loading="saveButt" >{{$t('application.SaveTo')+$t('application.Drafts')}}</el-button>
                        <el-button @click="closePage()">{{$t('application.cancel')}}</el-button>
                    </slot>
                    
                </div>

        </el-main>
                    
    </el-container>
    
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
    import BorrowFile from "@/views/workflow/CancelFile.vue"
    import UserSelectInput from '@/components/controls/BorrowUserSelectInput'
    export default {
        name: "StartupWorkflow",
        permit: 1,
        components: {
            ShowProperty:ShowProperty,
            DataGrid:DataGrid,
            AddCondition:AddCondition,
            DataSelect:DataSelect,
            RejectButton:RejectButton,
            DataLayout:DataLayout,
            AttachmentFile:AttachmentFile,
            BorrowFile:BorrowFile,
            UserSelectInput:UserSelectInput
        },
        data() {
                return {
      // 本地存储高度名称
      topStorageName: "ReceivedDCHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 120,
      dialogName: "",
      propertyVisible: false,
      typeName: "设计文件作废单",
      isOnly: false,
      butt: false,
      workflowFileList: [],
      saveButt: false,
      departmentLeader:'部门领导',
      companyLeader:'公司领导',
      reviewer1:'',
      reviewer2:'',
      reviewer3:'',
      accept:"不接受",
      importdialogVisible:false,
      dialogVisible:false,
      parentId:'',
      GUID:'',
       fileList: [],
       uploading:false,
       selectedAttachment:[],
       uploadUrl:'',
        
                }
            },
            props:{
                workflowObj:{type:Object,default:{}},
                showUploadFile:{type:Boolean,default:true},
                workflowFileList:{type:Array,default:[]}
            },
            mounted(){
                this.getGUID()
            },
            methods:{
            beforeUploadFile(uploadpath){
            let _self=this;
            _self.parentId = _self.GUID
            console.log(_self.parentId)
            if(_self.parentId==undefined||_self.parentId==''){
                _self.$message({
                        showClose: true,
                        message: _self.$t('message.PleaseSelectOneFile'),
                        duration: 2000,
                        type: "warning"
                    });
                return;
            }
            _self.uploadUrl=uploadpath;
            _self.fileList=[];
            _self.importdialogVisible=true;
        },
            uploadData() {
            let _self = this;
            let formdata = _self.getFormData();
            _self.uploading=true;
            console.log(formdata)
            _self
                .axios({
                headers: {
                    "Content-Type": "application/json;charset=UTF-8"
                },
                datatype: "json",
                method: "post",
                data: formdata,
                url: _self.uploadUrl
                })
                .then(function(response) {
                console.log(response)
                _self.importdialogVisible = false;
                _self.uploading=false;
                //_self.$refs.attachmentDoc.loadGridData();
                _self.$message({
                        showClose: true,
                        message: _self.$t('application.Import')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'success'
                    });
                })
                .catch(function(error) {
                _self.uploading=false;
                console.log(error);
                });
            },
            getFormData() {
            let _self = this;
            let formdata = new FormData();
            var data = {};
            data["parentDocId"] = _self.parentId;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
            data["relationName"]='附件';
            data["TYPE_NAME"]='附件';
            formdata.append("metaData", JSON.stringify(data));
            _self.fileList.forEach(function(file) {
                formdata.append("uploadFile", file.raw, file.name);
            });
            return formdata;
            },
            handleChange(file, fileList) {
            this.fileList = fileList;
            console.log(this.fileList)
        },
            getGUID(){
             let _self = this
             if(this.GUID==''){
             axios.get("/dc/GUID").then(function(response) {
            _self.GUID = response.data.data
            console.log(_self.GUID)
            })}},
            open(){
                this.dialogVisible=true
            },
                loadFormInfo(){
                    this.$refs.ShowProperty.myTypeName=this.typeName;
                    this.$refs.ShowProperty.loadFormInfo();
                },
                startUpWorkflow(workflow){
                  if(this.accept!="接受"){
                      this.$message("请接受档案利用承诺书!")
                      return
                  }
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

              
                    _self.butt = false
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
                        m.set("ID",_self.$refs.workflowFile.GUID)
                        console.log(m)
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
                            axios.post("/dc/createWorkflowFormData4Copy",formdata,{
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
                                    _self.closePage();
                                   
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
                closePage(pv){
                    this.$emit("close");
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
                    m.set("saveType","1");
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