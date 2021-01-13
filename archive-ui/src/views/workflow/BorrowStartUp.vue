<template>
    <el-container>
        <el-main>
            <ShowProperty
                    ref="ShowProperty"
                    @onSaved="onSaved"
                    width="100%"
                    folderPath=""
                    :showUploadFile="false"
                    v-bind:typeName="typeName"
                >
                </ShowProperty>
        <el-form :inline="true">
        <el-form-item label="本单位/部门领导:" label-width="150px">
        <UserSelectInput :roleName='departmentLeader' v-model="reviewer1" v-bind:inputValue="reviewer1" ></UserSelectInput>
        </el-form-item>
        <el-form-item label="文件形成单位/部门领导:" label-width="170px">
        <UserSelectInput :roleName='departmentLeader' v-model="reviewer2" v-bind:inputValue="reviewer2" :isRepeat="true"></UserSelectInput>
        </el-form-item>
        <el-form-item label="公司主管领导:" label-width="130px">
        <UserSelectInput :roleName='companyLeader' v-model="reviewer3" v-bind:inputValue="reviewer3" ></UserSelectInput>                
        </el-form-item>
        </el-form>
                <BorrowFile ref="workflowFile"
                    allowEdit
                    :isShowPage="false"
                    v-model="workflowFileList"
                    :workflowObj="workflowObj"
                    :type="borrowType"
                    @getType="getTypeResult"
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
                二、借阅档案时本人签字确认。特殊原因代签的，代签人承担同等责任；</br>
                三、借阅者不得扩大知悉范围，不得拍照、复制或转借他人；</br>
                四、借阅者不得做任何涂改、抽取、替换或添加信息；</br>
                五、借阅者应妥善保管。不得损坏、丢失，已装订成册的不得拆散；</br>
                六、借阅者应在规定时间内归还，超期应办理续借手续；</br>
                七、所借阅档案损坏或丢失应按公司规定赔偿。</br>
                本人已仔细阅读上述“档案利用承诺书”，并承诺履行相应义务，且愿意承担因个人行为导致档案破坏、信息泄露的法律责任。
                
                </span>

                </el-dialog>
        </el-main>
         <el-footer>
            <el-radio v-model="accept" label="接受">接受档案承诺利用书</el-radio>
            <el-button type="success" @click="open">查看档案利用承诺书</el-button>
            <el-button @click="startUpWorkflow(workflowObj)" :loading="butt">{{$t('application.StartUpWorkflow')}}</el-button>
            <el-button @click="closePage()">{{$t('application.cancel')}}</el-button>
        </el-footer>
    
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
    import BorrowFile from "@/views/workflow/BorrowFile.vue"
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
      typeName: "借阅单",
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
      dialogVisible:false,
      borrowType:'',
      flag: false,
      dialogVisible:false,
      isLimited:true,
      isCurrentCompany:false
                }
            },
            props:{
                workflowObj:{type:Object,default:{}},
                showUploadFile:{type:Boolean,default:true},
                workflowFileList:{type:Array,default:[]}
            },
            mounted(){
                // this.getWorkflows();
                console.log(this.currentUser().department)
            },
            methods:{
            getBorrowType(){            //获取当前借阅类型
                let _self = this
                let ecmFormItems = this.$refs.ShowProperty.dataList[0].ecmFormItems
                    ecmFormItems.forEach(element => {
                        if(element.attrName=="SUB_TYPE"){
                          _self.borrowType = element.defaultValue
                        }
                    });
            },
            checkLevel(){
                this.getBorrowType()
                let _self = this
                _self.isCurrentCompany=true
                  this.workflowFileList.forEach(element => {
                        if(element.C_CREATE_UNIT!=_self.currentUser().department&&element.C_ARCHIVE_UNIT!=_self.currentUser().department){
                        _self.isCurrentCompany=false       //找到了，借阅文件不是当前部门的，需要形成部门领导
                        }
                    })
                if(this.borrowType=='查阅'){                //默认都要验证选人，查阅的时候先默认不选人，然后判断密级
                    this.isLimited=false//无限制，可立即发起
                    this.workflowFileList.forEach(element => {
                        if(element.C_SECURITY_LEVEL!='非密'||element.C_SECURITY_LEVEL!='内部公开'){
                            _self.isLimited=true       //找到了，借阅文件包含商密，将在下一步进行判断
                        }
                    })
                } 
            },
            getTypeResult(){
                 let _self = this
                 _self.$refs.workflowFile.setSubTypeCondition(false)
                 let ecmFormItems = this.$refs.ShowProperty.dataList[0].ecmFormItems
                    ecmFormItems.forEach(element => {
                        if(element.attrName=="SUB_TYPE"){
                          _self.borrowType = element.defaultValue
                          if(_self.borrowType=='纸质借阅'||_self.borrowType=='查阅'){
                          _self.$refs.workflowFile.setSubTypeCondition(true)}
                          else{
                          _self.$refs.workflowFile.setSubTypeCondition(false)
                          }
                        }
                    });
            },
            
            open(){
                this.dialogVisible=true
            },
                loadFormInfo(){
                    this.$refs.ShowProperty.myTypeName=this.typeName;
                    this.$refs.ShowProperty.loadFormInfo();
                },
                startUpWorkflow(workflow){
                this.checkLevel()                                   //在这里获取当前文件安全等级
                console.log(this.isCurrentCompany)
                  if(this.accept!="接受"){
                      this.$message("请接受档案利用承诺书!")
                      return
                  }
                if(this.borrowType=='查阅'&&this.isLimited==true&&this.reviewer1==''){       //查阅提醒
                    this.$message("查阅文件包含涉密和限制文件，请选择本部门领导!")
                }
                  if(this.reviewer1==''&&this.borrowType!='查阅'){
                      this.$message("请完成借阅单必填项！本部门领导为必填项!")
                      return
                    }
                   if(this.isLimited==true){                     //查阅&普通借阅提醒
                   if(this.reviewer2=='' && this.isCurrentCompany==false){
                    this.$message("请完成借阅单必填项！形成部门领导为必填项!")
                      return                }
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
                    axios.post("/dc/countDocuments", JSON.stringify(m))
                                        .then(function (response) {
                        let code = response.data.code
                        if(code==1){
                        if(_self.reviewer3==''&&_self.isLimited==true){
                            _self.$message("需要选择公司领导!")
                            _self.butt = false
                            return
                        }
                        }
                    _self.getReviewers()
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
                        // m.set("C_REVIEWER1",_self.reviewer1)
                        // m.set("C_REVIEWER2",_self.reviewer2)
                        // m.set("C_REVIEWER3",_self.reviewer3)
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
                     })
                },
                closePage(){
                    this.$emit("closedialog", this.flag);
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
                getReviewers(){
                    let _self = this
                    let ecmFormItems = this.$refs.ShowProperty.dataList[0].ecmFormItems
                    ecmFormItems.forEach(element => {
                        if(element.attrName=="C_REVIEWER1"){
                            element.defaultValue=_self.reviewer1
                        }
                        if(element.attrName=="C_REVIEWER2"){
                            element.defaultValue=_self.reviewer2
                        }
                        if(element.attrName=="C_REVIEWER3"){
                            element.defaultValue=_self.reviewer3
                        }
                    });
                    //this.$refs.ShowProperty.dataList[0].ecmFormItems[6].defaultValue = val
                    console.log(this.$refs.ShowProperty.dataList[0].ecmFormItems)
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
.el-footer {
    text-align: center;
    padding: 10px;
  }
</style>